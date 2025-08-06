package top.orosirian.mcp.weixin.infrastructure.gateway.dto;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.Getter;

import java.io.Serial;
import java.util.HashMap;
import java.util.Map;

@Data
public class WeixinTemplateMessageDTO {

    private String toUser;

    private String templateId;

    private String url = "https://weixin.qq.com";

    private Map<String, Map<String, String>> data = new HashMap<>();

    public WeixinTemplateMessageDTO(String toUser, String templateId) {
        this.toUser = toUser;
        this.templateId = templateId;
    }

    public static void put(Map<String, Map<String, String>> data, TemplateKey key, String value) {
        data.put(key.getCode(), new HashMap<>() {
            @Serial
            private static final long serialVersionUID = 7092338402387318563L;
            {
                put("value", value);
            }
        });
    }

    @Getter
    @AllArgsConstructor
    public enum TemplateKey {

        PLATFORM("platform_name", "平台"),
        SUBJECT("subject_name", "主题"),
        DESCRIPTION("description_name", "简述"),
        ;

        private final String code;

        private final String desc;

    }

}
