package com.lovemaster.service;

import com.lovemaster.exception.BusinessException;
import com.lovemaster.service.impl.CosFileStorageServiceImpl;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.test.context.ActiveProfiles;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest(properties = "lovemaster.upload.storage-type=cos")
@ActiveProfiles("test")
class CosFileStorageServiceTest {

    @Autowired
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("配置 COS 存储时加载云存储实现")
    void cosStorageTypeShouldLoadCosService() {
        assertInstanceOf(CosFileStorageServiceImpl.class, fileStorageService);
    }

    @Test
    @DisplayName("COS 配置缺失时上传失败")
    void missingCosConfigShouldFail() {
        MockMultipartFile file = new MockMultipartFile(
                "file", "memory.jpg", "image/jpeg", new byte[]{1, 2, 3});

        assertThrows(BusinessException.class, () -> fileStorageService.storeImage(file));
    }
}
