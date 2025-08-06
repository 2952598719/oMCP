package top.orosirian.mcp.weixin.infrastructure.adaptor;

import com.google.common.cache.Cache;
import jakarta.annotation.Resource;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;
import retrofit2.Call;
import top.orosirian.mcp.weixin.domain.adaptor.IWeixinPort;
import top.orosirian.mcp.weixin.domain.model.WeixinNoticeFunctionRequest;
import top.orosirian.mcp.weixin.domain.model.WeixinNoticeFunctionResponse;
import top.orosirian.mcp.weixin.infrastructure.gateway.IWeixinApiService;
import top.orosirian.mcp.weixin.infrastructure.gateway.dto.WeixinTemplateMessageDTO;
import top.orosirian.mcp.weixin.infrastructure.gateway.dto.WeixinTokenResponseDTO;
import top.orosirian.mcp.weixin.types.WeiXinApiProperties;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@Component
public class WeixinPort implements IWeixinPort {

    @Resource
    private WeiXinApiProperties properties;

    @Resource
    private IWeixinApiService weixinApiService;

    @Resource
    private Cache<String, String> weixinAccessToken;

    @Override
    public WeixinNoticeFunctionResponse weixinNotice(WeixinNoticeFunctionRequest request) throws IOException {
        // 1.获取access token
        String accessToken = weixinAccessToken.getIfPresent(properties.getAppid());
        if (accessToken == null) {
            Call<WeixinTokenResponseDTO> call = weixinApiService.getToken("client_credential", properties.getAppid(), properties.getAppsecret());
            WeixinTokenResponseDTO weixinTokenResponseDTO = call.execute().body();
            assert weixinTokenResponseDTO != null;
            accessToken = weixinTokenResponseDTO.getAccess_token();
            weixinAccessToken.put(properties.getAppid(), accessToken);
        }

        // 2.发送模版消息
        Map<String, Map<String, String>> data = new HashMap<>();
        WeixinTemplateMessageDTO.put(data, WeixinTemplateMessageDTO.TemplateKey.PLATFORM, request.getPlatform());
        WeixinTemplateMessageDTO.put(data, WeixinTemplateMessageDTO.TemplateKey.SUBJECT, request.getSubject());
        WeixinTemplateMessageDTO.put(data, WeixinTemplateMessageDTO.TemplateKey.DESCRIPTION, request.getDescription());

        WeixinTemplateMessageDTO templateMessageDTO = new WeixinTemplateMessageDTO(properties.getTouser(), properties.getTemplate_id());
        templateMessageDTO.setUrl(request.getJumpUrl());
        templateMessageDTO.setData(data);

        Call<Void> call = weixinApiService.sendMessage(accessToken, templateMessageDTO);
        call.execute();

        WeixinNoticeFunctionResponse weixinNoticeFunctionResponse = new WeixinNoticeFunctionResponse();
        weixinNoticeFunctionResponse.setSuccess(true);

        return weixinNoticeFunctionResponse;
    }

}
