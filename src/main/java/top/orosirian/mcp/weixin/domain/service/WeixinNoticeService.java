package top.orosirian.mcp.weixin.domain.service;

import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import top.orosirian.mcp.weixin.domain.adaptor.IWeixinPort;
import top.orosirian.mcp.weixin.domain.model.WeixinNoticeFunctionRequest;
import top.orosirian.mcp.weixin.domain.model.WeixinNoticeFunctionResponse;

import java.io.IOException;

@Slf4j
@Service
public class WeixinNoticeService {

    @Resource
    private IWeixinPort weixinPort;

    @Tool(description = "微信公众号消息通知")
    public WeixinNoticeFunctionResponse weixinNotice(WeixinNoticeFunctionRequest request) throws IOException {
        log.info("微信消息通知：平台: {} 主题: {} 描述: {}", request.getPlatform(), request.getSubject(), request.getDescription());
        return weixinPort.weixinNotice(request);
    }


}
