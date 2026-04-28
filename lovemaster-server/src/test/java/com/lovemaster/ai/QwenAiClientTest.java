package com.lovemaster.ai;

import com.lovemaster.config.AiProperties;
import com.lovemaster.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.assertThrows;

class QwenAiClientTest {

    @Test
    @DisplayName("空响应内容会转换为业务异常")
    void emptyResponseShouldFail() {
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        properties.setModel("qwen-plus");
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost")
                .build();
        QwenAiClient client = new QwenAiClient(properties, restClient);

        assertThrows(BusinessException.class, () -> client.generate("system", "user"));
    }
}
