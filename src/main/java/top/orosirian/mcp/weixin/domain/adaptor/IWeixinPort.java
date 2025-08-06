package top.orosirian.mcp.weixin.domain.adaptor;

import top.orosirian.mcp.weixin.domain.model.WeixinNoticeFunctionRequest;
import top.orosirian.mcp.weixin.domain.model.WeixinNoticeFunctionResponse;

import java.io.IOException;

public interface IWeixinPort {

    WeixinNoticeFunctionResponse weixinNotice(WeixinNoticeFunctionRequest request) throws IOException;

}
