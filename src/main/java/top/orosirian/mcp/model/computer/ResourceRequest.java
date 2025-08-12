package top.orosirian.mcp.model.computer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ResourceRequest {

    @JsonProperty(required = true, value = "path")
    @JsonPropertyDescription("资源路径")
    private String path;

    @JsonProperty(required = true, value = "name")
    @JsonPropertyDescription("资源文件名")
    private String name;

}
