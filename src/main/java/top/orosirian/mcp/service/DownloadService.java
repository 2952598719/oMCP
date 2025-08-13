package top.orosirian.mcp.service;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.io.FileUtils;
import org.jsoup.Connection;
import org.jsoup.Jsoup;
import org.jsoup.select.Elements;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import top.orosirian.mcp.model.download.DownloadRequest;
import top.orosirian.mcp.model.download.DownloadResponse;
import top.orosirian.mcp.model.download.MusicDownloadRequest;
import top.orosirian.mcp.model.download.MusicDownloadResponse;
import top.orosirian.mcp.model.download.NovelDownloadRequest;
import top.orosirian.mcp.model.download.NovelDownloadResponse;
import top.orosirian.mcp.utils.FileProcessor;

import java.io.BufferedWriter;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileWriter;
import java.io.IOException;
import java.net.URL;
import java.nio.file.Files;
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
public class DownloadService {

    @Autowired
    private FileProcessor FIleProcessor;
    @Autowired
    private FileProcessor fileProcessor;

    @Tool(description = "下载文件到路径")
    public DownloadResponse downloadFile(DownloadRequest request) {
        try {
            // 验证保存路径
            File targetFile = new File(request.getSavePath() + "/" + request.getFileName());
            if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
                throw new IOException("无法创建目录: " + request.getSavePath());
            }

            // 下载文件
            FileUtils.copyURLToFile(
                    new URL(request.getFileUrl()),
                    targetFile,
                    5000, // 连接超时 5 秒
                    30000 // 读取超时 30 秒
            );
            DownloadResponse response = new DownloadResponse();
            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            DownloadResponse response = new DownloadResponse();
            response.setSuccess(false);
            response.setInfo(e.getMessage());
            return response;
        }
    }

    @Tool(description = "输入url下载音乐")
    public MusicDownloadResponse downloadMusic(MusicDownloadRequest request) {
        File music = new File(fileProcessor.getDownloadPath() + "/music/" + request.getMusicName() + ".mp3");
        if(!music.exists()){
            downloadFile(new DownloadRequest(request.getMusicUrl(), fileProcessor.getDownloadPath() + "/music", request.getMusicName() + ".mp3"));
        }
        return new MusicDownloadResponse(true);
    }

    @Tool(description = "输入url下载小说")
    public NovelDownloadResponse downloadNovel(NovelDownloadRequest request) throws IOException, InterruptedException {
        File novel = new File(fileProcessor.getDownloadPath() + "/novel/" + request.getNovelTitle() + ".zip");
        if(!novel.exists()){
            List<ChapterInfo> chapterList = fetchChapterList(request.getNovelTitle(), request.getNovelIndexUrl());
            downloadChapters(request.getNovelTitle(), chapterList);
            makeNovelZip(request.getNovelTitle());
        }
        return new NovelDownloadResponse(true);
    }

    private List<ChapterInfo> fetchChapterList(String novelTitle, String novelIndexUrl) throws IOException {
        Connection conn = Jsoup.connect(novelIndexUrl)
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                .ignoreHttpErrors(true)
                .timeout(10000);
        Connection.Response response = conn.execute();

        List<ChapterInfo> chapters = new ArrayList<>();
        Elements chapterElements = response.parse().body().select("#main div table tbody tr td a");
        String novelUrlName = FIleProcessor.getNovelUrlName(novelTitle);

        chapterElements.forEach(element -> {
            chapters.add(new ChapterInfo(
                    element.text().trim(),
                    element.attr("href").split("\\.")[0],
                    FIleProcessor.getNovelBaseUrl() + "/books/" + novelUrlName + "/" + element.attr("href")
            ));
        });
        return chapters;
    }

    private void downloadChapters(String novelName, List<ChapterInfo> chapterList) throws InterruptedException {
        String dir = FIleProcessor.getDownloadPath() + "/novel/" + novelName;
        File dirFile = new File(dir);
        dirFile.mkdirs();
        CountDownLatch latch = new CountDownLatch(chapterList.size());
        ExecutorService executor = Executors.newFixedThreadPool(10);

        for (ChapterInfo chapter : chapterList) {
            executor.submit(() -> {
                try {
                    downloadChapter(chapter, FIleProcessor.getDownloadPath() + "/novel/" + novelName);
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

    private void downloadChapter(ChapterInfo chapter, String dir) throws IOException {
        Elements contentElement = Jsoup.connect(chapter.href)
                .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                .timeout(10000)
                .get()
                .selectXpath("//*[@id=\"main\"]/div[2]");
        String htmlContent = contentElement.html()
                .replaceAll("<p>", "")
                .replaceAll("</p>", "")
                .replaceAll("<br\\s*/?>", "\n");
        String fileName = String.format("%s/%s %s.txt", dir, chapter.index, chapter.title);
        try (BufferedWriter writer = new BufferedWriter(new FileWriter(fileName))) {
            writer.write(htmlContent);
        }
    }

    private void makeNovelZip(String novelName) throws IOException {
        String novelDir = FIleProcessor.getDownloadPath() + "/novel/" + novelName;
        String zipPath = FIleProcessor.getDownloadPath() + "/novel/" + novelName + ".zip";
        Files.deleteIfExists(Paths.get(zipPath));
        try (ZipOutputStream zos = new ZipOutputStream(new FileOutputStream(zipPath))) {
            Files.walk(Paths.get(novelDir))
                    .filter(path -> !Files.isDirectory(path))
                    .forEach(path -> {
                        ZipEntry entry = new ZipEntry(path.getFileName().toString());
                        try {
                            zos.putNextEntry(entry);
                            Files.copy(path, zos);
                            zos.closeEntry();
                        } catch (IOException e) {
                            System.err.println(e.getMessage());
                        }
                    });
        }
        System.err.println("压缩成功");
        // 删除原始文件夹
//        FileUtils.deleteDirectory(new File(bookPath));
    }

    @Data
    @AllArgsConstructor
    public static class ChapterInfo {
        private String title;
        private String index;
        private String href;
    }

}
