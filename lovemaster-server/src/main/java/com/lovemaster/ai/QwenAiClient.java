package com.lovemaster.ai;

import com.lovemaster.config.AiProperties;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Component;
import org.springframework.util.StringUtils;
import org.springframework.web.client.RestClient;

import java.util.List;

@Component
@RequiredArgsConstructor
public class QwenAiClient implements AiClient {

    private final AiProperties aiProperties;
    private final RestClient qwenRestClient;

    @Override
    public String generate(String systemPrompt, String userPrompt) {
        String apiKey = System.getenv(aiProperties.getApiKeyEnvName());
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ErrorCode.AI_API_KEY_MISSING);
        }

        QwenChatRequest request = new QwenChatRequest(
                aiProperties.getModel(),
                List.of(
                        new QwenChatRequest.Message("system", systemPrompt),
                        new QwenChatRequest.Message("user", userPrompt)
                ),
                0.4
        );

        try {
            QwenChatResponse response = qwenRestClient.post()
                    .uri("/chat/completions")
                    .header("Authorization", "Bearer " + apiKey)
                    .body(request)
                    .retrieve()
                    .body(QwenChatResponse.class);
            return extractContent(response);
        } catch (BusinessException e) {
            throw e;
        } catch (Exception e) {
            throw new BusinessException(ErrorCode.AI_PROVIDER_ERROR);
        }
    }

    private String extractContent(QwenChatResponse response) {
        if (response == null || response.getChoices() == null || response.getChoices().isEmpty()) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
        QwenChatResponse.Message message = response.getChoices().get(0).getMessage();
        if (message == null || !StringUtils.hasText(message.getContent())) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
        return message.getContent();
    }
}
