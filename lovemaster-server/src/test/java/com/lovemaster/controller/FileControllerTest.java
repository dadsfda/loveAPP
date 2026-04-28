package com.lovemaster.controller;

import com.lovemaster.dto.response.ImageUploadResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.FileStorageService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.mock.web.MockMultipartFile;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.multipart;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class FileControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private FileStorageService fileStorageService;

    @Test
    @DisplayName("POST /api/v1/files/images - 上传成功")
    void uploadImageShouldReturnSuccess() throws Exception {
        MockMultipartFile file = new MockMultipartFile("file", "memory.jpg", "image/jpeg", new byte[]{1, 2, 3});
        when(fileStorageService.storeImage(any())).thenReturn(
                new ImageUploadResponse("/uploads/images/test.jpg", "test.jpg", "image/jpeg", 3L));

        mockMvc.perform(multipart("/api/v1/files/images")
                        .file(file)
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.url").value("/uploads/images/test.jpg"));
    }

    private UsernamePasswordAuthenticationToken authToken() {
        User principal = new User();
        principal.setId(1L);
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }
}
