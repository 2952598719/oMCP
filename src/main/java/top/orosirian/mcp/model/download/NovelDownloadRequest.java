package top.orosirian.mcp.model.download;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class NovelDownloadRequest {

    @JsonProperty(required = true, value = "novelTitle")
    @JsonPropertyDescription("书名")
    private String novelTitle;

    @JsonProperty(required = true, value = "novelIndexUrl")
    @JsonPropertyDescription("小说目录页url")
    private String novelIndexUrl;

}
