package top.orosirian.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import top.orosirian.mcp.service.ComputerService;
import top.orosirian.mcp.service.DownloadService;
import top.orosirian.mcp.service.MusicService;

@Slf4j
@SpringBootApplication
public class MCPServerApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCPServerApplication.class, args);
    }

    // 基础工具
    @Bean
    public ToolCallbackProvider downloadTools(DownloadService downloadService) {
        return MethodToolCallbackProvider.builder().toolObjects(downloadService).build();
    }

    // 工具1：电脑配置检查
    @Bean
    public ToolCallbackProvider computerTools(ComputerService computerService) {
        return MethodToolCallbackProvider.builder().toolObjects(computerService).build();
    }

    // 工具2：音乐地址获取
    @Bean
    public ToolCallbackProvider musicTools(MusicService musicService) {
        return MethodToolCallbackProvider.builder().toolObjects(musicService).build();
    }

}
