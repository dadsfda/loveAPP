package com.lovemaster.service;

import com.lovemaster.ai.AiClient;
import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;
import com.lovemaster.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

class AiAdviceServiceTest {

    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "lovemaster.ai.enabled=true",
            "lovemaster.ai.api-key-env-name=PATH"
    })
    class Enabled {

        @Autowired
        private AiAdviceService aiAdviceService;

        @MockBean
        private AiClient aiClient;

        @Test
        @DisplayName("AI启用且客户端返回成功时生成沟通建议")
        void generateAdviceShouldSucceed() {
            when(aiClient.generate(anyString(), anyString()))
                    .thenReturn("建议：先表达自己的感受，再提出具体请求。\n模板：我感到有些委屈，是因为晚回消息让我担心。我希望下次很忙时可以简单说一声。");

            AiCommunicationAdviceResponse response = aiAdviceService.generate(createRequest());

            assertTrue(response.getAdvice().contains("表达"));
            assertTrue(response.getMessageTemplate().contains("我感到"));
            assertEquals("建议仅供参考，具体做法需要结合你们双方情况协商。", response.getReminder());
            verify(aiClient).generate(anyString(), anyString());
        }

        @Test
        @DisplayName("客户端返回结构化JSON时稳定解析全部字段")
        void structuredJsonShouldBeParsed() {
            when(aiClient.generate(anyString(), anyString()))
                    .thenReturn("""
                            {
                              "advice": "先承认情绪，再说清楚自己的期待。",
                              "messageTemplate": "我感到有点委屈，是因为晚回消息让我担心。我希望下次很忙时可以简单告诉我一声。",
                              "riskLevel": "medium",
                              "reminder": "这只是沟通参考，不代表对错判断。"
                            }
                            """);

            AiCommunicationAdviceResponse response = aiAdviceService.generate(createRequest());

            assertEquals("先承认情绪，再说清楚自己的期待。", response.getAdvice());
            assertEquals("我感到有点委屈，是因为晚回消息让我担心。我希望下次很忙时可以简单告诉我一声。", response.getMessageTemplate());
            assertEquals("medium", response.getRiskLevel());
            assertEquals("建议仅供参考，具体做法需要结合你们双方情况协商。", response.getReminder());
        }

        @Test
        @DisplayName("客户端返回非JSON文本时使用兜底结构")
        void plainTextShouldFallbackToSafeStructuredResponse() {
            when(aiClient.generate(anyString(), anyString()))
                    .thenReturn("可以先表达自己在等待时的不安，再邀请对方一起约定忙碌时的简单回应方式。");

            AiCommunicationAdviceResponse response = aiAdviceService.generate(createRequest());

            assertTrue(response.getAdvice().contains("表达"));
            assertTrue(response.getMessageTemplate().contains("我感到"));
            assertEquals("low", response.getRiskLevel());
            assertEquals("建议仅供参考，具体做法需要结合你们双方情况协商。", response.getReminder());
        }

        @Test
        @DisplayName("客户端失败时转换为业务异常")
        void clientFailureShouldBecomeBusinessException() {
            when(aiClient.generate(anyString(), anyString()))
                    .thenThrow(new BusinessException(com.lovemaster.exception.ErrorCode.AI_PROVIDER_ERROR));

            assertThrows(BusinessException.class, () -> aiAdviceService.generate(createRequest()));
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "lovemaster.ai.enabled=false",
            "lovemaster.ai.api-key-env-name=PATH"
    })
    class Disabled {

        @Autowired
        private AiAdviceService aiAdviceService;

        @MockBean
        private AiClient aiClient;

        @Test
        @DisplayName("AI未启用时返回业务异常")
        void disabledShouldFail() {
            assertThrows(BusinessException.class, () -> aiAdviceService.generate(createRequest()));
        }
    }

    @Nested
    @SpringBootTest
    @ActiveProfiles("test")
    @TestPropertySource(properties = {
            "lovemaster.ai.enabled=true",
            "lovemaster.ai.api-key-env-name=LOVEMASTER_MISSING_TEST_KEY"
    })
    class MissingKey {

        @Autowired
        private AiAdviceService aiAdviceService;

        @MockBean
        private AiClient aiClient;

        @Test
        @DisplayName("API Key缺失时返回业务异常")
        void missingApiKeyShouldFail() {
            assertThrows(BusinessException.class, () -> aiAdviceService.generate(createRequest()));
        }
    }

    private static AiCommunicationAdviceRequest createRequest() {
        AiCommunicationAdviceRequest request = new AiCommunicationAdviceRequest();
        request.setScenario("因为晚回消息吵架了");
        request.setMyFeeling("委屈、生气");
        request.setPartnerFeeling("可能觉得我管太多");
        request.setGoal("想好好表达，不想继续吵");
        return request;
    }
}
