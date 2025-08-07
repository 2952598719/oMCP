package top.orosirian.mcp.model.computer;

import com.fasterxml.jackson.annotation.JsonInclude;
import com.fasterxml.jackson.annotation.JsonProperty;
import com.fasterxml.jackson.annotation.JsonPropertyDescription;
import lombok.Data;

@Data
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ComputerRequest {

    @JsonProperty(required = true, value = "computer")
    @JsonPropertyDescription("电脑名称")
    private String computer;

}

