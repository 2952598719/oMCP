package top.orosirian.mcp;

import com.google.common.cache.Cache;
import com.google.common.cache.CacheBuilder;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.ToolCallbackProvider;
import org.springframework.ai.tool.method.MethodToolCallbackProvider;
import org.springframework.boot.CommandLineRunner;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.Bean;
import retrofit2.Retrofit;
import retrofit2.converter.jackson.JacksonConverterFactory;
import top.orosirian.mcp.computer.domain.service.ComputerService;
import top.orosirian.mcp.weixin.domain.service.WeixinNoticeService;
import top.orosirian.mcp.weixin.infrastructure.gateway.IWeixinApiService;
import top.orosirian.mcp.weixin.types.WeiXinApiProperties;

import java.util.concurrent.TimeUnit;

@Slf4j
@SpringBootApplication
public class MCPServerApplication implements CommandLineRunner {

    @Resource
    private WeiXinApiProperties properties;

    public static void main(String[] args) {
        SpringApplication.run(MCPServerApplication.class, args);
    }

    @Override
    public void run(String... args) {
        log.info("mcp server 'computer' started");

        log.info("check properties ...");
        if (properties.getAppid() == null || properties.getAppsecret() == null || properties.getTouser() == null || properties.getTemplate_id() == null || properties.getOriginal_id() == null) {
            log.warn("weixin properties key is null, please set it in application.yml");
        } else {
            log.info("weixin properties key {}", properties.getAppid());
        }
    }

    /**
     * 借助
     */
    @Bean
    public IWeixinApiService weixinApiService() {
        Retrofit retrofit = new Retrofit.Builder()
                .baseUrl("https://api.weixin.qq.com/")
                .addConverterFactory(JacksonConverterFactory.create())
                .build();
        return retrofit.create(IWeixinApiService.class);
    }

    @Bean
    public Cache<String, String> weixinAccessToken() {
        return CacheBuilder.newBuilder()
                .expireAfterWrite(2, TimeUnit.HOURS)
                .build();
    }

    /**
     * 工具
     */
    // 工具1：电脑配置检查
    @Bean
    public ToolCallbackProvider computerTools(ComputerService computerService) {
        return MethodToolCallbackProvider.builder().toolObjects(computerService).build();
    }

    // 工具2：公众号文章
    @Bean
    public ToolCallbackProvider weixinPublisher(WeixinNoticeService weixinNoticeService) {
        return MethodToolCallbackProvider.builder().toolObjects(weixinNoticeService).build();
    }

}
