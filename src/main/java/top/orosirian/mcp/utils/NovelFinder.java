package top.orosirian.mcp.utils;

import com.google.gson.Gson;
import com.google.gson.reflect.TypeToken;
import jakarta.annotation.PostConstruct;
import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;
import org.jsoup.Jsoup;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;

import java.io.File;
import java.nio.file.Files;
import java.nio.file.Paths;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Data
@Component
public class NovelFinder {

    @Value("${app.download.path}")
    private String downloadPath;

    @Value("${app.download.base}")
    private String baseUrl;

    private final String API_URL = baseUrl + "/json/data.json";

    private final String NOVEL_INDEX_PATH = downloadPath + "/novel/books.json";

    private final Map<String, NovelInfo> novelMap = new HashMap<>();

    private final Gson gson = new Gson();

    @PostConstruct
    public void init() {
        try {
            File dir = new File(downloadPath + "/novel");
            dir.mkdirs();
            if (Files.exists(Paths.get(NOVEL_INDEX_PATH))) {
                String jsonData = new String(Files.readAllBytes(Paths.get(NOVEL_INDEX_PATH)));
                List<NovelInfo> novels = gson.fromJson(jsonData, new TypeToken<List<NovelInfo>>(){}.getType());
                novels.forEach(novel -> novelMap.put(novel.getBookName(), novel));
            } else {
                updateNovelIndex();
            }
        } catch (Exception e) {
            updateNovelIndex();
        }
    }

    @Scheduled(fixedRate = 24 * 60 * 60 * 1000) // 每24小时自动更新
    public synchronized void updateNovelIndex() {
        try {
            String jsonData = Jsoup.connect(API_URL)
                    .header("user-agent", "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_15_7) AppleWebKit/537.36 (KHTML, like Gecko) Chrome/139.0.0.0 Safari/537.36")
                    .ignoreContentType(true)
                    .timeout(15000)
                    .execute()
                    .body();
            List<NovelInfo> novels = gson.fromJson(jsonData, new TypeToken<List<NovelInfo>>(){}.getType());
            novelMap.clear();
            novels.forEach(novel -> novelMap.put(novel.getBookName(), novel));
            Files.write(Paths.get(NOVEL_INDEX_PATH), jsonData.getBytes());
        } catch (Exception e) {
            // 保持原有数据
        }
    }

    public boolean isNovelExists(String novelTitle) {
        return novelMap.containsKey(novelTitle);
    }

    public String getIndexPage(String novelTitle) {
        return baseUrl + "/books/" + novelTitle + "/" + novelMap.get(novelTitle).BookStartUrl;
    }

    public String getNovelUrlName(String novelTitle) {
        return novelMap.get(novelTitle).BookFolderName;
    }

    @Getter
    @AllArgsConstructor
    private static class NovelInfo {
        private String BookName;
        private String BookDescription;
        private String BookAuthor;
        private String BookType;
        private String BookFolderName;
        private String BookStartUrl;
    }

}
