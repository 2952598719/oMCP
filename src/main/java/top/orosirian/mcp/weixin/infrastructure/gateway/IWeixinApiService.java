package top.orosirian.mcp.weixin.infrastructure.gateway;

import retrofit2.Call;
import retrofit2.http.Body;
import retrofit2.http.GET;
import retrofit2.http.POST;
import retrofit2.http.Query;
import top.orosirian.mcp.weixin.infrastructure.gateway.dto.WeixinTemplateMessageDTO;
import top.orosirian.mcp.weixin.infrastructure.gateway.dto.WeixinTokenResponseDTO;

public interface IWeixinApiService {

    // 获取access token
    @GET("cgi-bin/token")
    Call<WeixinTokenResponseDTO> getToken(
            @Query("grant_type") String grantType,  // 获取access_token时，填写client_credential
            @Query("appid") String appId,
            @Query("secret") String appSecret
    );

    // 发送微信公众号模版消息
    // 文档：https://mp.weixin.qq.com/debug/cgi-bin/readtmpl?t=tmplmsg/faq_tmpl
    @POST("cgi-bin/message/template/send")
    Call<Void> sendMessage(@Query("access_token") String accessToken, @Body WeixinTemplateMessageDTO weixinTemplateMessageDTO);

}
