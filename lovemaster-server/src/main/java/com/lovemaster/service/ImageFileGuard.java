package com.lovemaster.service;

import com.lovemaster.config.UploadProperties;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import org.springframework.stereotype.Component;
import org.springframework.web.multipart.MultipartFile;

import java.util.Map;

@Component
public class ImageFileGuard {

    private static final Map<String, String> ALLOWED_TYPES = Map.of(
            "image/jpeg", ".jpg",
            "image/png", ".png",
            "image/webp", ".webp",
            "image/gif", ".gif"
    );

    private final UploadProperties uploadProperties;

    public ImageFileGuard(UploadProperties uploadProperties) {
        this.uploadProperties = uploadProperties;
    }

    public String validateAndGetExtension(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new BusinessException(ErrorCode.FILE_EMPTY);
        }
        if (file.getSize() > uploadProperties.getMaxImageSize()) {
            throw new BusinessException(ErrorCode.FILE_SIZE_EXCEEDED);
        }

        String extension = ALLOWED_TYPES.get(file.getContentType());
        if (extension == null) {
            throw new BusinessException(ErrorCode.FILE_TYPE_NOT_ALLOWED);
        }
        return extension;
    }
}
