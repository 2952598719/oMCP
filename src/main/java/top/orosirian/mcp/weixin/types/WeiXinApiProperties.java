package top.orosirian.mcp.weixin.types;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "weixin.api")
public class WeiXinApiProperties {

    private String original_id;

    private String appid;

    private String appsecret;

    private String template_id;

    private String touser;

}
