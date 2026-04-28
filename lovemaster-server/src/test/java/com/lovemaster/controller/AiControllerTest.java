package com.lovemaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.AiAdviceService;
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

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AiControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AiAdviceService aiAdviceService;

    @Test
    @DisplayName("POST /api/v1/ai/communication-advice - 生成成功")
    void generateAdviceShouldReturnSuccess() throws Exception {
        when(aiAdviceService.generate(any())).thenReturn(new AiCommunicationAdviceResponse(
                "建议先表达感受，再提出具体请求。",
                "我感到有些委屈，是因为晚回消息让我担心。我希望下次可以简单说一声。",
                "low",
                "建议仅供参考，具体做法需要结合你们双方情况协商。"
        ));

        mockMvc.perform(post("/api/v1/ai/communication-advice")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("生成成功"))
                .andExpect(jsonPath("$.data.advice").value("建议先表达感受，再提出具体请求。"))
                .andExpect(jsonPath("$.data.riskLevel").value("low"));
    }

    @Test
    @DisplayName("POST /api/v1/ai/communication-advice - 场景为空返回400")
    void emptyScenarioShouldReturnBadRequest() throws Exception {
        AiCommunicationAdviceRequest request = createRequest();
        request.setScenario("");

        mockMvc.perform(post("/api/v1/ai/communication-advice")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.code").value(400));
    }

    @Test
    @DisplayName("POST /api/v1/ai/communication-advice - 未登录返回401")
    void unauthenticatedShouldReturnUnauthorized() throws Exception {
        mockMvc.perform(post("/api/v1/ai/communication-advice")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isUnauthorized());
    }

    private AiCommunicationAdviceRequest createRequest() {
        AiCommunicationAdviceRequest request = new AiCommunicationAdviceRequest();
        request.setScenario("因为晚回消息吵架了");
        request.setMyFeeling("委屈、生气");
        request.setPartnerFeeling("可能觉得我管太多");
        request.setGoal("想好好表达，不想继续吵");
        return request;
    }

    private UsernamePasswordAuthenticationToken authToken() {
        User principal = new User();
        principal.setId(1L);
        return new UsernamePasswordAuthenticationToken(principal, null, Collections.emptyList());
    }
}
