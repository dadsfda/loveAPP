package com.lovemaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.AnniversaryService;
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
class AnniversaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnniversaryService anniversaryService;

    @Test
    @DisplayName("POST /api/v1/anniversaries - 成功")
    void createShouldReturnSuccess() throws Exception {
        CreateAnniversaryRequest request = createRequest();
        AnniversaryResponse response = response(1L, "第一次约会");
        when(anniversaryService.create(eq(1L), any(CreateAnniversaryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/anniversaries")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(1))
                .andExpect(jsonPath("$.data.title").value("第一次约会"));
    }

    @Test
    @DisplayName("GET /api/v1/anniversaries - 成功")
    void listShouldReturnSuccess() throws Exception {
        when(anniversaryService.list(1L, "CUSTOM", "PRIVATE"))
                .thenReturn(List.of(response(1L, "第一次约会")));

        mockMvc.perform(get("/api/v1/anniversaries")
                        .param("type", "CUSTOM")
                        .param("visibility", "PRIVATE")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("第一次约会"));
    }

    @Test
    @DisplayName("PUT /api/v1/anniversaries/{id} - 成功")
    void updateShouldReturnSuccess() throws Exception {
        UpdateAnniversaryRequest request = updateRequest();
        when(anniversaryService.update(eq(1L), eq(9L), any(UpdateAnniversaryRequest.class)))
                .thenReturn(response(9L, "更新后的纪念日"));

        mockMvc.perform(put("/api/v1/anniversaries/9")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.title").value("更新后的纪念日"));
    }

    @Test
    @DisplayName("DELETE /api/v1/anniversaries/{id} - 成功")
    void deleteShouldReturnSuccess() throws Exception {
        doNothing().when(anniversaryService).delete(1L, 9L);

        mockMvc.perform(delete("/api/v1/anniversaries/9")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }

    @Test
    @DisplayName("GET /api/v1/anniversaries/reminders - 成功")
    void remindersShouldReturnSuccess() throws Exception {
        when(anniversaryService.reminders(1L, 7)).thenReturn(List.of(response(1L, "七天后提醒")));

        mockMvc.perform(get("/api/v1/anniversaries/reminders")
                        .param("days", "7")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("七天后提醒"));
    }

    private UsernamePasswordAuthenticationToken authToken() {
        User principal = new User();
        principal.setId(1L);
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    private CreateAnniversaryRequest createRequest() {
        CreateAnniversaryRequest request = new CreateAnniversaryRequest();
        request.setTitle("第一次约会");
        request.setDate(LocalDate.of(2026, 5, 20));
        request.setType("CUSTOM");
        request.setVisibility("PRIVATE");
        request.setRemindDays(List.of(0, 3));
        request.setSurpriseMode(false);
        return request;
    }

    private UpdateAnniversaryRequest updateRequest() {
        UpdateAnniversaryRequest request = new UpdateAnniversaryRequest();
        request.setTitle("更新后的纪念日");
        request.setDate(LocalDate.of(2026, 5, 21));
        request.setType("CUSTOM");
        request.setVisibility("PRIVATE");
        request.setRemindDays(List.of(0, 1));
        request.setSurpriseMode(true);
        return request;
    }

    private AnniversaryResponse response(Long id, String title) {
        AnniversaryResponse response = new AnniversaryResponse();
        response.setId(id);
        response.setCreatorId(1L);
        response.setTitle(title);
        response.setDate(LocalDate.of(2026, 5, 20));
        response.setType("CUSTOM");
        response.setVisibility("PRIVATE");
        response.setRemindDays(List.of(0, 3));
        response.setSurpriseMode(false);
        return response;
    }
}
