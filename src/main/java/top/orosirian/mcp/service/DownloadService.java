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
import top.orosirian.mcp.model.download.NovelDownloadRequest;
import top.orosirian.mcp.model.download.NovelDownloadResponse;
import top.orosirian.mcp.utils.NovelFinder;

import java.io.File;
import java.io.IOException;
import java.net.URL;
import java.util.ArrayList;
import java.util.List;

@Slf4j
@Service
public class DownloadService {

    @Autowired
    private NovelFinder novelFinder;

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

    @Tool(description = "输入网址获取小说")
    public NovelDownloadResponse downloadNovel(NovelDownloadRequest request) throws IOException, InterruptedException {
        List<ChapterInfo> chapterList = fetchChapterList(request.getNovelTitle(), request.getNovelIndexUrl());
        downloadChapters();


//        downloadChapters(bookBaseUrl, request.getNovelTitle(), chapterList);
//        zipResult(request.getNovelTitle());

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
        String novelUrlName = novelFinder.getNovelUrlName(novelTitle);

        chapterElements.forEach(element -> {
            chapters.add(new ChapterInfo(
                    element.text().trim(),
                    novelFinder.getAPI_URL() + "/books/" + novelUrlName + "/" + element.attr("href")
            ));
        });
        return chapters;
    }

    public void downloadChapters() throws IOException {

    }

    @Data
    @AllArgsConstructor
    private static class ChapterInfo {
        private String title;
        private String href;
    }


}
