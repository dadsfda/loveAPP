package com.lovemaster.service;

import com.lovemaster.dto.response.ImageUploadResponse;
import com.lovemaster.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
class LocalFileStorageServiceTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("可以上传图片并返回URL")
    void uploadImageShouldReturnUrl() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "memory.jpg", "image/jpeg", new byte[]{1, 2, 3});

        ImageUploadResponse response = fileStorageService.storeImage(file);

        assertNotNull(response.getUrl());
        assertTrue(response.getUrl().startsWith("/uploads/images/"));
        assertEquals("image/jpeg", response.getContentType());
        assertEquals(3L, response.getSize());
    }

    @Test
    @DisplayName("空文件上传失败")
    void emptyFileShouldFail() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "empty.jpg", "image/jpeg", new byte[]{});

        assertThrows(BusinessException.class, () -> fileStorageService.storeImage(file));
    }

    @Test
    @DisplayName("非图片类型上传失败")
    void nonImageFileShouldFail() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "note.txt", "text/plain", new byte[]{1, 2, 3});

        assertThrows(BusinessException.class, () -> fileStorageService.storeImage(file));
    }
}
