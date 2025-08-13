package top.orosirian.mcp.model.download;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MusicDownloadRequest {

    @JsonProperty(required = true, value = "musicName")
    @JsonPropertyDescription("歌名")
    private String musicName;

    @JsonProperty(required = true, value = "musicUrl")
    @JsonPropertyDescription("歌曲url")
    private String musicUrl;

}
