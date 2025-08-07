package top.orosirian.mcp.model.music;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class MusicResponse {

    @JsonProperty(required = true, value = "code")
    @JsonPropertyDescription("响应状态码")
    private Integer code;

    @JsonProperty(required = true, value = "data")
    @JsonPropertyDescription("歌曲详细信息")
    private MusicData data;

    // 嵌套的静态内部类处理data对象
    @Data
    @JsonInclude(JsonInclude.Include.NON_NULL)
    public static class MusicData {
        @JsonProperty(required = true, value = "name")
        @JsonPropertyDescription("歌曲名称")
        private String name;

        @JsonProperty(required = true, value = "artist")
        @JsonPropertyDescription("歌手姓名")
        private String artist;

        @JsonProperty(required = true, value = "cover")
        @JsonPropertyDescription("封面图片URL")
        private String cover;

        @JsonProperty("detail_page")
        @JsonPropertyDescription("详情页URL")
        private String detailPage;

        @JsonProperty("play_url")
        @JsonPropertyDescription("播放地址URL")
        private String playUrl;

        @JsonProperty(required = true, value = "lrc")
        @JsonPropertyDescription("歌词内容")
        private String lrc;
    }

}
