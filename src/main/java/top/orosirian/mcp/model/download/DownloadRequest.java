package top.orosirian.mcp.model.download;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class DownloadRequest {

    @JsonProperty(required = true, value = "fileUrl")
    @JsonPropertyDescription("文件url")
    private String fileUrl;

    @JsonProperty(required = true, value = "savePath")
    @JsonPropertyDescription("本地保存位置")
    private String savePath;

    @JsonProperty(required = true, value = "fileName")
    @JsonPropertyDescription("预计采用的文件名")
    private String fileName;

}
