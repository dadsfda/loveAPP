package com.lovemaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.dto.request.CreateMemoryRequest;
import com.lovemaster.dto.request.UpdateMemoryRequest;
import com.lovemaster.dto.response.MemoryResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.MemoryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class MemoryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private MemoryService memoryService;

    @Test
    @DisplayName("POST /api/v1/memories - 成功")
    void createShouldReturnSuccess() throws Exception {
        CreateMemoryRequest request = createRequest();
        when(memoryService.create(eq(1L), any(CreateMemoryRequest.class)))
                .thenReturn(response(9L, "江边散步"));

        mockMvc.perform(post("/api/v1/memories")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(9))
                .andExpect(jsonPath("$.data.title").value("江边散步"));
    }

    @Test
    @DisplayName("GET /api/v1/memories - 成功")
    void listShouldReturnSuccess() throws Exception {
        when(memoryService.list(eq(1L), eq("PRIVATE"), eq(LocalDate.of(2026, 1, 1)),
                eq(LocalDate.of(2026, 12, 31)), eq("散步")))
                .thenReturn(List.of(response(9L, "江边散步")));

        mockMvc.perform(get("/api/v1/memories")
                        .param("visibility", "PRIVATE")
                        .param("startDate", "2026-01-01")
                        .param("endDate", "2026-12-31")
                        .param("tag", "散步")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("江边散步"));
    }

    @Test
    @DisplayName("GET /api/v1/memories/{id} - 成功")
    void getShouldReturnSuccess() throws Exception {
        when(memoryService.get(1L, 9L)).thenReturn(response(9L, "江边散步"));

        mockMvc.perform(get("/api/v1/memories/9")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("江边散步"));
    }

    @Test
    @DisplayName("PUT /api/v1/memories/{id} - 成功")
    void updateShouldReturnSuccess() throws Exception {
        UpdateMemoryRequest request = updateRequest();
        when(memoryService.update(eq(1L), eq(9L), any(UpdateMemoryRequest.class)))
                .thenReturn(response(9L, "更新后的回忆"));

        mockMvc.perform(put("/api/v1/memories/9")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("更新后的回忆"));
    }

    @Test
    @DisplayName("DELETE /api/v1/memories/{id} - 成功")
    void deleteShouldReturnSuccess() throws Exception {
        doNothing().when(memoryService).delete(1L, 9L);

        mockMvc.perform(delete("/api/v1/memories/9")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private UsernamePasswordAuthenticationToken authToken() {
        User principal = new User();
        principal.setId(1L);
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    private CreateMemoryRequest createRequest() {
        CreateMemoryRequest request = new CreateMemoryRequest();
        request.setTitle("江边散步");
        request.setMemoryDate(LocalDate.of(2026, 5, 20));
        request.setLocation("江边公园");
        request.setContent("一起散步拍了很多照片。");
        request.setImageUrl("/uploads/images/test.jpg");
        request.setVisibility("PRIVATE");
        request.setTags(List.of("散步", "拍照"));
        request.setRemark("测试回忆");
        return request;
    }

    private UpdateMemoryRequest updateRequest() {
        UpdateMemoryRequest request = new UpdateMemoryRequest();
        request.setTitle("更新后的回忆");
        request.setMemoryDate(LocalDate.of(2026, 5, 22));
        request.setLocation("城市书店");
        request.setContent("更新后的内容。");
        request.setImageUrl("/uploads/images/updated.jpg");
        request.setVisibility("PRIVATE");
        request.setTags(List.of("阅读", "聊天"));
        request.setRemark("更新后的备注");
        return request;
    }

    private MemoryResponse response(Long id, String title) {
        MemoryResponse response = new MemoryResponse();
        response.setId(id);
        response.setCreatorId(1L);
        response.setTitle(title);
        response.setMemoryDate(LocalDate.of(2026, 5, 20));
        response.setLocation("江边公园");
        response.setContent("一起散步拍了很多照片。");
        response.setImageUrl("/uploads/images/test.jpg");
        response.setVisibility("PRIVATE");
        response.setTags(List.of("散步", "拍照"));
        response.setRemark("测试回忆");
        return response;
    }
}
