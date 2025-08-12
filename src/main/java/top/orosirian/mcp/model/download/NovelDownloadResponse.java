package top.orosirian.mcp.model.download;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NovelDownloadResponse {

    @JsonProperty(required = true, value = "success")
    @JsonPropertyDescription("是否爬取成功")
    private Boolean success;

}
