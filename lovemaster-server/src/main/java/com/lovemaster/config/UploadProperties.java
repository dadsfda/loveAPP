package com.lovemaster.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "lovemaster.upload")
public class UploadProperties {

    private String imageDir = "uploads/images";

    private String imageUrlPrefix = "/uploads/images";

    private long maxImageSize = 5 * 1024 * 1024;
}
