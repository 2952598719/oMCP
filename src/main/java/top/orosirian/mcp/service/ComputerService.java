package top.orosirian.mcp.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.ai.tool.annotation.Tool;
import org.springframework.stereotype.Service;
import top.orosirian.mcp.model.computer.ComputerRequest;
import top.orosirian.mcp.model.computer.ComputerResponse;

import java.io.BufferedReader;
import java.io.IOException;
import java.io.InputStreamReader;
import java.util.Properties;

@Slf4j
@Service
public class ComputerService {

    @Tool(description = "获取电脑配置")
    public ComputerResponse queryConfig(ComputerRequest request) {
        log.info("正在获取 {} 配置信息", request.getComputer());

        Properties properties = System.getProperties();     // 获取系统属性
        String osName = properties.getProperty("os.name");              // 操作系统名称
        String osVersion = properties.getProperty("os.version");        // 操作系统版本
        String osArch = properties.getProperty("os.arch");              // 操作系统架构
        String userName = properties.getProperty("user.name");          // 用户的账户名称
        String userHome = properties.getProperty("user.home");          // 用户的主目录
        String userDir = properties.getProperty("user.dir");            // 用户的当前工作目录
        String javaVersion = properties.getProperty("java.version");    // Java 运行时环境版本
        String osInfo = getSpecificInfo(osName);     // 根据操作系统执行特定的命令来获取更多信息

        ComputerResponse response = new ComputerResponse();
        response.setOsName(osName);
        response.setOsVersion(osVersion);
        response.setOsArch(osArch);
        response.setUserName(userName);
        response.setUserHome(userHome);
        response.setUserDir(userDir);
        response.setJavaVersion(javaVersion);
        response.setOsInfo(osInfo);

        return response;
    }

    private String getSpecificInfo(String osName) {
        StringBuilder cache = new StringBuilder();
        try {
            Process process = null;
            if (osName.toLowerCase().contains("win")) {     // Windows特定的代码
                process = Runtime.getRuntime().exec("systeminfo");
            } else if (osName.toLowerCase().contains("mac")) {  // macOS特定的代码
                process = Runtime.getRuntime().exec("system_profiler SPHardwareDataType");
            } else if (osName.toLowerCase().contains("nix") || osName.toLowerCase().contains("nux")) {  // Linux特定的代码
                process = Runtime.getRuntime().exec("lshw -short");
            }
            if (process != null) {
                BufferedReader reader = new BufferedReader(new InputStreamReader(process.getInputStream()));
                String line;
                while ((line = reader.readLine()) != null) {
                    cache.append(line);
                }
            }
        } catch (IOException e) {
            log.error("获取信息失败, {}", e.getMessage());
        }
        return cache.toString();
    }

}
