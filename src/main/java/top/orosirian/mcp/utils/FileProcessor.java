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
public class FileProcessor {

    @Value("${app.download.path}")
    private String downloadPath;

    @Value("${app.download.base}")
    private String novelBaseUrl;

    private String NOVEL_LIST_URL;

    private String NOVEL_INDEX_PATH;

    private final Map<String, NovelInfo> novelMap = new HashMap<>();

    private final Gson gson = new Gson();

    public static void main(String[] args) {
        new FileProcessor().init();
    }

    @PostConstruct
    public void init() {
        try {
            NOVEL_LIST_URL = novelBaseUrl + "/json/data.json";
            NOVEL_INDEX_PATH = downloadPath + "/novel/books.json";
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
            String jsonData = Jsoup.connect(NOVEL_LIST_URL)
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
            System.err.println("更新书籍index失败");
        }
    }

    public boolean isNovelExists(String novelTitle) {
        return novelMap.containsKey(novelTitle);
    }

    public String getIndexPage(String novelTitle) {
        NovelInfo novelInfo = novelMap.get(novelTitle);
        return novelBaseUrl + "/books/" + novelInfo.BookFolderName + "/" + novelInfo.BookStartUrl;
    }

    public String getNovelUrlName(String novelTitle) {
        return novelMap.get(novelTitle).BookFolderName;
    }

    public NovelInfo getFirstBook() {
        if(novelMap.isEmpty()) {
            return new NovelInfo("不存在", null, null, null, null, null);
        } else {
            return novelMap.values().stream().findFirst().get();
        }
    }

    @Getter
    @AllArgsConstructor
    public static class NovelInfo {
        private String BookName;
        private String BookDescription;
        private String BookAuthor;
        private String BookType;
        private String BookFolderName;
        private String BookStartUrl;
    }

}
