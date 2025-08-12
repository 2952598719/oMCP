package top.orosirian.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import top.orosirian.mcp.api.MusicApi;
import top.orosirian.mcp.model.resource.MusicRequest;
import top.orosirian.mcp.model.resource.MusicResponse;
import top.orosirian.mcp.model.resource.NovelRequest;
import top.orosirian.mcp.model.resource.NovelResponse;
import top.orosirian.mcp.utils.NovelFinder;

import java.io.IOException;

@Slf4j
@Service
public class QueryService {

    @Autowired
    private MusicApi musicApi;

    @Autowired
    private NovelFinder novelFinder;

    @Tool(description = "获取音乐地址")
    public MusicResponse getMusic(MusicRequest request) throws IOException {
        log.info("正在获取 {} 歌曲", request.getMsg());
        Call<MusicResponse> call = musicApi.getMusic(request.getMsg(), "1", "json");
        return call.execute().body();
    }

    @Tool(description = "获取书籍地址")
    public NovelResponse getNovel(NovelRequest request) throws IOException {
        if (!novelFinder.isNovelExists(request.getNovelTitle())) {
            throw new IllegalArgumentException("书籍不存在: " + request.getNovelTitle());
        }
        return new NovelResponse(
                request.getNovelTitle(),
                novelFinder.getIndexPage(request.getNovelTitle())
        );
    }

}
