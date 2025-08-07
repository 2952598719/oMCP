package top.orosirian.mcp.model.music;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MusicRequest {

    @JsonProperty(required = true, value = "msg")
    @JsonPropertyDescription("歌曲名字或歌曲作者")
    private String msg;

}
