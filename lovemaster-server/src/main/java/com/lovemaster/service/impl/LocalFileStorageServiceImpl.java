package com.lovemaster.service.impl;

import com.lovemaster.config.UploadProperties;
import com.lovemaster.dto.response.ImageUploadResponse;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.service.FileStorageService;
import com.lovemaster.service.ImageFileGuard;
import com.lovemaster.service.ImageObjectNameGenerator;
import lombok.RequiredArgsConstructor;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;

@Service
@RequiredArgsConstructor
@ConditionalOnProperty(prefix = "lovemaster.upload", name = "storage-type", havingValue = "local", matchIfMissing = true)
public class LocalFileStorageServiceImpl implements FileStorageService {

    private final UploadProperties uploadProperties;
    private final ImageFileGuard imageFileGuard;
    private final ImageObjectNameGenerator imageObjectNameGenerator;

    @Override
    public ImageUploadResponse storeImage(MultipartFile file) {
        String filename = imageObjectNameGenerator.generate(imageFileGuard.validateAndGetExtension(file));
        Path targetDir = Path.of(uploadProperties.getImageDir()).toAbsolutePath().normalize();
        Path targetFile = targetDir.resolve(filename).normalize();
        if (!targetFile.startsWith(targetDir)) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }

        try {
            Files.createDirectories(targetDir);
            file.transferTo(targetFile);
        } catch (IOException e) {
            throw new BusinessException(ErrorCode.FILE_SAVE_FAILED);
        }

        String url = uploadProperties.getImageUrlPrefix() + "/" + filename;
        return new ImageUploadResponse(url, filename, file.getContentType(), file.getSize());
    }
}
