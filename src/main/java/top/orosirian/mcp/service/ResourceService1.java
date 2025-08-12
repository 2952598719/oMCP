package top.orosirian.mcp.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.safety.Safelist;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import top.orosirian.mcp.api.MusicApi;
import top.orosirian.mcp.model.resource.MusicRequest;
import top.orosirian.mcp.model.resource.MusicResponse;
import top.orosirian.mcp.model.resource.NovelRequest;
import top.orosirian.mcp.model.resource.NovelResponse;
import top.orosirian.mcp.utils.BookFinder1;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.TimeUnit;
import java.util.zip.ZipEntry;
import java.util.zip.ZipOutputStream;

@Slf4j
@Service
public class ResourceService1 {

    @Autowired
    MusicApi musicApi;

    @Value("${app.download.path}")
    private String downloadPath;



    @Tool(description = "获取小说")
    public NovelResponse crawlNovel(NovelRequest request) throws Exception {
        // 检查书籍是否存在
        if (!bookFinder.bookExists(request.getNovelTitle())) {
            throw new IllegalArgumentException("书籍不存在: " + request.getNovelTitle());
        }

        // 获取基础URL和起始页
        String bookBaseUrl = bookFinder.getBookBaseUrl(request.getNovelTitle());
        String startPage = bookFinder.getBookStartPage(request.getNovelTitle());

        if (bookBaseUrl == null || startPage == null) {
            throw new IllegalStateException("无法获取书籍地址信息");
        }

        // 构建正确的章节列表URL
        String chapterListUrl = bookBaseUrl + "/" + startPage;

        // 执行爬虫逻辑
        Document listDocument = fetchChapterList(chapterListUrl);
        List<ChapterInfo> chapterList = parseChapterList(listDocument);
        downloadChapters(bookBaseUrl, request.getNovelTitle(), chapterList);
        zipResult(request.getNovelTitle());

        return new NovelResponse(true);
    }

    /**
     * 获取章节列表页（带重试机制）
     */
    private Document fetchChapterList(String chapterListUrl) throws IOException, InterruptedException {
        Document document = null;
        int retryCount = 0;

        while (document == null && retryCount < 3) {
            try {
                Connection conn = Jsoup.connect(chapterListUrl)
                        .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                        .ignoreHttpErrors(true)
                        .timeout(10000);

                Connection.Response response = conn.execute();

                if (response.statusCode() == 200) {
                    return response.parse();
                }

                // 智能切换起始页格式
                if (response.statusCode() == 403) {
                    if (chapterListUrl.contains("0000.html")) {
                        chapterListUrl = chapterListUrl.replace("0000.html", "000.html");
                    } else {
                        chapterListUrl = chapterListUrl.replace("000.html", "0000.html");
                    }
                }
            } catch (IOException e) {
                // 重试时使用备用URL
                chapterListUrl = getFallbackUrl(chapterListUrl);
            }

            retryCount++;
            Thread.sleep(1000 * retryCount); // 每次重试间隔增加
        }

        throw new IOException("无法获取章节列表页，重试失败");
    }

    /**
     * 获取备用章节列表URL
     */
    private String getFallbackUrl(String originalUrl) {
        if (originalUrl.contains("0000.html")) {
            return originalUrl.replace("0000.html", "000.html");
        } else {
            return originalUrl.replace("000.html", "0000.html");
        }
    }

    /**
     * 解析章节列表
     */
    private List<ChapterInfo> parseChapterList(Document document) {
        List<ChapterInfo> chapters = new ArrayList<>();
        Elements chapterElements = document.body().select("#main div table tbody tr td a");

        chapterElements.forEach(element -> {
            chapters.add(new ChapterInfo(
                    element.text().trim(),
                    element.attr("href")
            ));
        });

        return chapters;
    }

    /**
     * 下载所有章节
     */
    private void downloadChapters(String bookBaseUrl, String novelTitle, List<ChapterInfo> chapters)
            throws InterruptedException, IOException {
        String bookPath = getBookPath(novelTitle);
        createDirectory(bookPath);

        CountDownLatch latch = new CountDownLatch(chapters.size());
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (ChapterInfo chapter : chapters) {
            executor.submit(() -> {
                try {
                    downloadChapter(bookBaseUrl, bookPath, chapter);
                } catch (Exception e) {
                    System.err.println("章节下载失败: " + chapter.title + " - " + e.getMessage());
                } finally {
                    latch.countDown();
                }
            });
        }

        latch.await(30, TimeUnit.MINUTES); // 最长等待30分钟
        executor.shutdown();
    }

    /**
     * 下载单个章节
     */
    private void downloadChapter(String bookBaseUrl, String bookPath, ChapterInfo chapter)
            throws IOException, InterruptedException {
        // 构建正确的章节URL
        String chapterUrl = bookBaseUrl + "/" + chapter.href;

        // 获取章节内容（带重试机制）
        String content = fetchChapterContent(chapterUrl, 3);

        // 保存章节文件
        String index = chapter.href.split("\\.")[0];
        String fileName = String.format("%s/%s %s.txt", bookPath, index, chapter.title.replaceAll("[\\\\/:*?\"<>|]", ""));
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(content);
        }
    }

    /**
     * 获取章节内容（带重试）
     */
    private String fetchChapterContent(String url, int maxRetries) throws IOException, InterruptedException {
        for (int i = 0; i < maxRetries; i++) {
            try {
                Document doc = Jsoup.connect(url)
                        .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                        .timeout(10000)
                        .get();

                return extractChapterContent(doc);
            } catch (IOException e) {
                if (i == maxRetries - 1) {
                    throw e;
                }
                Thread.sleep(2000); // 等待2秒重试
            }
        }
        return ""; // 不应该执行到这里
    }

    /**
     * 提取章节内容
     */
    private String extractChapterContent(Document doc) {
        // 使用更健壮的内容定位方式
        Element contentElement = doc.selectFirst("#main .content");

        if (contentElement == null) {
            // 备用选择器
            contentElement = doc.selectFirst("#main > div:not(.header)");
        }

        if (contentElement == null) {
            return "内容解析失败";
        }

        // 清理内容格式
        String htmlContent = contentElement.html()
                .replaceAll("<div[^>]*>", "\n")
                .replaceAll("</div>", "")
                .replaceAll("<p>", "")
                .replaceAll("</p>", "\n\n")
                .replaceAll("<br\\s*/?>", "\n");

        return Jsoup.clean(htmlContent, Safelist.none());
    }

    /**
     * 压缩最终结果
     */
    private void zipResult(String novelTitle) throws IOException {
        String bookPath = getBookPath(novelTitle);
        String zipPath = getZipPath(novelTitle);

        // 删除旧的ZIP文件
        Files.deleteIfExists(Paths.get(zipPath));

        // 压缩文件夹
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            Files.walk(Paths.get(bookPath))
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry entry = new ZipEntry(path.getFileName().toString());
                        try {
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            // 处理异常
                        }
                    });
        }

        // 删除原始文件夹
//        FileUtils.deleteDirectory(new File(bookPath));
    }

    private String getBookPath(String novelTitle) {
        return downloadPath + File.separator + "documents" + File.separator + novelTitle;
    }

    private String getZipPath(String novelTitle) {
        return downloadPath + File.separator + novelTitle + ".zip";
    }

    private void createDirectory(String path) throws IOException {
        Path directory = Paths.get(path);
        if (!Files.exists(directory)) {
            Files.createDirectories(directory);
        }
    }

    @Data
    @AllArgsConstructor
    private static class ChapterInfo {
        private String title;
        private String href;
    }

}
