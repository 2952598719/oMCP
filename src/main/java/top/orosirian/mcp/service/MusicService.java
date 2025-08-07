package top.orosirian.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import retrofit2.Call;
import top.orosirian.mcp.api.MusicApi;
import top.orosirian.mcp.model.music.MusicRequest;
import top.orosirian.mcp.model.music.MusicResponse;

import java.io.IOException;

@Slf4j
@Service
public class MusicService {

    @Autowired
    MusicApi musicApi;

    @Tool(description = "获取音乐地址")
    public MusicResponse getMusic(MusicRequest request) throws IOException {
        log.info("正在获取 {} 歌曲", request.getMsg());
        Call<MusicResponse> call = musicApi.getMusic(request.getMsg(), "1", "json");
        return call.execute().body();
    }

}
