package top.orosirian.mcp.model.download;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloadResponse {

    @JsonProperty(required = true, value = "success")
    @JsonPropertyDescription("下载是否成功")
    private boolean success;

    @JsonProperty(value = "info")
    @JsonPropertyDescription("其他信息")
    private String info;

}
