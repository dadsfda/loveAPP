package com.lovemaster.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "lovemaster.ai")
public class AiProperties {

    private boolean enabled = false;

    private String provider = "qwen";

    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String model = "qwen-plus";

    private long timeoutSeconds = 20;

    private String apiKeyEnvName = "DASHSCOPE_API_KEY";
}
