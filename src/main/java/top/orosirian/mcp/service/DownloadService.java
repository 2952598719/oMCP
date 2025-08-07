package top.orosirian.mcp.service;

import org.apache.commons.io.FileUtils;
import java.io.File;
import java.io.IOException;
import java.net.URL;
import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import top.orosirian.mcp.model.download.DownloadRequest;
import top.orosirian.mcp.model.download.DownloadResponse;

@Slf4j
@Service
public class DownloadService {

    @Tool(description = "下载文件到路径")
    public DownloadResponse downloadFile(DownloadRequest request) {
        try {
            // 验证保存路径
            File targetFile = new File(request.getSavePath() + "/" + request.getFileName());
            if (!targetFile.getParentFile().exists() && !targetFile.getParentFile().mkdirs()) {
                throw new IOException("无法创建目录: " + request.getSavePath());
            }

            // 下载文件
            FileUtils.copyURLToFile(
                    new URL(request.getFileUrl()),
                    targetFile,
                    5000, // 连接超时 5 秒
                    30000 // 读取超时 30 秒
            );
            DownloadResponse response = new DownloadResponse();
            response.setSuccess(true);
            return response;
        } catch (Exception e) {
            DownloadResponse response = new DownloadResponse();
            response.setSuccess(false);
            response.setInfo(e.getMessage());
            return response;
        }

    }

}
