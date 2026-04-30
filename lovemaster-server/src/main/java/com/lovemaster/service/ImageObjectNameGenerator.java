package com.lovemaster.service;

import org.springframework.stereotype.Component;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.Locale;
import java.util.UUID;

@Component
public class ImageObjectNameGenerator {

    private static final DateTimeFormatter FILENAME_TIME_FORMAT = DateTimeFormatter.ofPattern("yyyyMMddHHmmssSSS");

    public String generate(String extension) {
        return LocalDateTime.now().format(FILENAME_TIME_FORMAT)
                + "_"
                + UUID.randomUUID().toString().replace("-", "").toLowerCase(Locale.ROOT)
                + extension;
    }
}
