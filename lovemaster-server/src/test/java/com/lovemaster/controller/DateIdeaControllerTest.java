package com.lovemaster.controller;

import com.lovemaster.dto.request.DateIdeaQueryRequest;
import com.lovemaster.dto.response.DateIdeaResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.DateIdeaService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.security.authentication.UsernamePasswordAuthenticationToken;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.util.Collections;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class DateIdeaControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @MockBean
    private DateIdeaService dateIdeaService;

    @Test
    @DisplayName("GET /api/v1/date-ideas - 成功")
    void listShouldReturnSuccess() throws Exception {
        when(dateIdeaService.list(any(DateIdeaQueryRequest.class)))
                .thenReturn(List.of(response(1L, "傍晚散步拍照", 0)));

        mockMvc.perform(get("/api/v1/date-ideas")
                        .param("budgetLevel", "FREE")
                        .param("durationLevel", "ONE_HOUR")
                        .param("scene", "OUTDOOR")
                        .param("tags", "散步,拍照")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].title").value("傍晚散步拍照"));
    }

    @Test
    @DisplayName("GET /api/v1/date-ideas/recommend - 成功")
    void recommendShouldReturnSuccess() throws Exception {
        when(dateIdeaService.recommend(eq(1L), any(DateIdeaQueryRequest.class)))
                .thenReturn(List.of(response(1L, "傍晚散步拍照", 2)));

        mockMvc.perform(get("/api/v1/date-ideas/recommend")
                        .param("scene", "OUTDOOR")
                        .with(authentication(authToken())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data[0].matchScore").value(2));
    }

    private UsernamePasswordAuthenticationToken authToken() {
        User principal = new User();
        principal.setId(1L);
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }

    private DateIdeaResponse response(Long id, String title, Integer matchScore) {
        DateIdeaResponse response = new DateIdeaResponse();
        response.setId(id);
        response.setTitle(title);
        response.setBudgetLevel("FREE");
        response.setDurationLevel("ONE_HOUR");
        response.setScene("OUTDOOR");
        response.setInterestTags(List.of("散步", "拍照"));
        response.setSteps(List.of("选一条熟悉路线"));
        response.setTips("适合低成本恢复连接感");
        response.setMatchScore(matchScore);
        return response;
    }
}
