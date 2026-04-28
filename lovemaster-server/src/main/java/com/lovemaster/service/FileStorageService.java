package com.lovemaster.service;

import com.lovemaster.dto.response.ImageUploadResponse;
import org.springframework.web.multipart.MultipartFile;

public interface FileStorageService {

    ImageUploadResponse storeImage(MultipartFile file);
}
