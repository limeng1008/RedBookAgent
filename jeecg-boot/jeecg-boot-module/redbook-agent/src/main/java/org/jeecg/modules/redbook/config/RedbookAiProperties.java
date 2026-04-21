package org.jeecg.modules.redbook.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.stereotype.Component;

@Data
@Component
@ConfigurationProperties(prefix = "redbook.ai")
public class RedbookAiProperties {
    private boolean enabled = false;

    /**
     * 支持：dify / fastgpt / local
     */
    private String provider = "local";

    /**
     * Dify 示例： https://your-dify-host/v1
     * FastGPT 示例： https://your-fastgpt-host/api
     */
    private String baseUrl;

    private String apiKey;

    private Integer timeoutSeconds = 60;

    /**
     * 远程 AI 失败后的额外重试次数，不含首次请求。
     */
    private Integer retryTimes = 1;

    private String userPrefix = "redbook-agent";
}
