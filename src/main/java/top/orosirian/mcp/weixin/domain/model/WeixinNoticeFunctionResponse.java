package top.orosirian.mcp.weixin.domain.model;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class WeixinNoticeFunctionResponse {

    @JsonProperty(required = true, value = "success")
    @JsonPropertyDescription("success")
    private boolean success;

}
