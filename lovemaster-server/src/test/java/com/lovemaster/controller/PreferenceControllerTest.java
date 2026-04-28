package com.lovemaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.dto.request.CreatePreferenceRequest;
import com.lovemaster.dto.request.UpdatePreferenceRequest;
import com.lovemaster.dto.response.PreferenceResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.PreferenceService;
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
class PreferenceControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PreferenceService preferenceService;

    @Test
    @DisplayName("POST /api/v1/preferences - 成功")
    void createShouldReturnSuccess() throws Exception {
        CreatePreferenceRequest request = createRequest();
        PreferenceResponse response = response(1L, "喜欢散步");
        when(preferenceService.create(eq(1L), any(CreatePreferenceRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/preferences")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.content").value("喜欢散步"));
    }

    @Test
    @DisplayName("GET /api/v1/preferences - 成功")
    void listShouldReturnSuccess() throws Exception {
        when(preferenceService.list(1L, "HOBBY", "PRIVATE")).thenReturn(List.of(response(1L, "喜欢散步")));

        mockMvc.perform(get("/api/v1/preferences")
                        .param("category", "HOBBY")
                        .param("visibility", "PRIVATE")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].content").value("喜欢散步"));
    }

    @Test
    @DisplayName("GET /api/v1/preferences/{id} - 成功")
    void getShouldReturnSuccess() throws Exception {
        when(preferenceService.get(1L, 9L)).thenReturn(response(9L, "喜欢拍照"));

        mockMvc.perform(get("/api/v1/preferences/9")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("喜欢拍照"));
    }

    @Test
    @DisplayName("PUT /api/v1/preferences/{id} - 成功")
    void updateShouldReturnSuccess() throws Exception {
        UpdatePreferenceRequest request = updateRequest();
        when(preferenceService.update(eq(1L), eq(9L), any(UpdatePreferenceRequest.class)))
                .thenReturn(response(9L, "更新后的偏好"));

        mockMvc.perform(put("/api/v1/preferences/9")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.content").value("更新后的偏好"));
    }

    @Test
    @DisplayName("DELETE /api/v1/preferences/{id} - 成功")
    void deleteShouldReturnSuccess() throws Exception {
        doNothing().when(preferenceService).delete(1L, 9L);

        mockMvc.perform(delete("/api/v1/preferences/9")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    private UsernamePasswordAuthenticationToken authToken() {
        User principal = new User();
        principal.setId(1L);
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    private CreatePreferenceRequest createRequest() {
        CreatePreferenceRequest request = new CreatePreferenceRequest();
        request.setTarget("SELF");
        request.setCategory("HOBBY");
        request.setContent("喜欢散步");
        request.setVisibility("PRIVATE");
        request.setTags(List.of("散步"));
        request.setRemark("测试备注");
        return request;
    }

    private UpdatePreferenceRequest updateRequest() {
        UpdatePreferenceRequest request = new UpdatePreferenceRequest();
        request.setTarget("SELF");
        request.setCategory("CUSTOM");
        request.setContent("更新后的偏好");
        request.setVisibility("PRIVATE");
        request.setTags(List.of("散步", "拍照"));
        request.setRemark("更新备注");
        return request;
    }

    private PreferenceResponse response(Long id, String content) {
        PreferenceResponse response = new PreferenceResponse();
        response.setId(id);
        response.setCreatorId(1L);
        response.setTarget("SELF");
        response.setCategory("HOBBY");
        response.setContent(content);
        response.setVisibility("PRIVATE");
        response.setTags(List.of("散步"));
        response.setRemark("测试备注");
        return response;
    }
}
