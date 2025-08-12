package top.orosirian.mcp;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import top.orosirian.mcp.service.ComputerService;
import top.orosirian.mcp.service.DownloadService1;
import top.orosirian.mcp.service.ResourceService1;

@Slf4j
@SpringBootApplication
public class MCPServerApplication extends SpringApplication {

    public static void main(String[] args) {
        SpringApplication.run(MCPServerApplication.class, args);
    }

    // 基础工具
    @Bean
    public ToolCallbackProvider downloadTools(DownloadService1 downloadService) {
        return MethodToolCallbackProvider.builder().toolObjects(downloadService).build();
    }

    // 工具1：电脑配置检查
    @Bean
    public ToolCallbackProvider computerTools(ComputerService computerService) {
        return MethodToolCallbackProvider.builder().toolObjects(computerService).build();
    }

    // 工具2：音乐地址获取
    @Bean
    public ToolCallbackProvider resourceTools(ResourceService1 resourceService) {
        return MethodToolCallbackProvider.builder().toolObjects(resourceService).build();
    }

}
