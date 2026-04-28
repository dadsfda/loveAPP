package com.lovemaster.service.impl;

import com.fasterxml.jackson.databind.JsonNode;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.ai.AiClient;
import com.lovemaster.config.AiProperties;
import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.service.AiAdviceService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

@Service
@RequiredArgsConstructor
public class AiAdviceServiceImpl implements AiAdviceService {

    private static final String REMINDER = "建议仅供参考，具体做法需要结合你们双方情况协商。";
    private static final String FALLBACK_TEMPLATE = "我感到有些难受，是因为这件事让我产生了压力。我希望我们可以先冷静一下，再一起商量一个双方都能接受的做法。";

    private final AiProperties aiProperties;
    private final AiClient aiClient;
    private final ObjectMapper objectMapper;

    @Override
    public AiCommunicationAdviceResponse generate(AiCommunicationAdviceRequest request) {
        validateEnabled();
        String text = aiClient.generate(buildSystemPrompt(), buildUserPrompt(request));
        if (!StringUtils.hasText(text)) {
            throw new BusinessException(ErrorCode.AI_RESPONSE_INVALID);
        }
        return parseResponse(text);
    }

    private void validateEnabled() {
        if (!aiProperties.isEnabled()) {
            throw new BusinessException(ErrorCode.AI_DISABLED);
        }
        String apiKey = System.getenv(aiProperties.getApiKeyEnvName());
        if (!StringUtils.hasText(apiKey)) {
            throw new BusinessException(ErrorCode.AI_API_KEY_MISSING);
        }
    }

    private String buildSystemPrompt() {
        return """
                你是 LoveMaster 的关系沟通建议助手。请始终使用中文回答。
                你的任务是帮助用户用更温和、非指责、可协商的方式表达感受和请求。
                不判断情侣关系谁对谁错，不鼓励控制、试探、报复、冷暴力或道德绑架。
                不提供医疗、法律、心理诊断建议。
                如果涉及自伤、暴力威胁或人身安全风险，优先建议用户寻求现实帮助和安全支持。
                只输出一个合法 JSON 对象，不要使用 Markdown 代码块，不要输出额外解释。
                JSON 字段：
                - advice：一段简短沟通建议。
                - messageTemplate：一段可直接参考的话术，尽量使用“我感到...因为...我希望...”结构。
                - riskLevel：只能是 low、medium、high。普通矛盾为 low，明显升级或强烈冲突为 medium，涉及自伤、暴力威胁或人身安全为 high。
                - reminder：一句提醒。服务端会统一替换为固定免责声明。
                """;
    }

    private String buildUserPrompt(AiCommunicationAdviceRequest request) {
        return """
                场景：%s
                我的感受：%s
                对方可能的感受：%s
                沟通目标：%s
                """.formatted(
                valueOrDefault(request.getScenario()),
                valueOrDefault(request.getMyFeeling()),
                valueOrDefault(request.getPartnerFeeling()),
                valueOrDefault(request.getGoal())
        );
    }

    private String valueOrDefault(String value) {
        return StringUtils.hasText(value) ? value.trim() : "未提供";
    }

    private AiCommunicationAdviceResponse parseResponse(String text) {
        AiCommunicationAdviceResponse jsonResponse = parseJsonResponse(text);
        if (jsonResponse != null) {
            return jsonResponse;
        }
        return parsePlainTextResponse(text);
    }

    private AiCommunicationAdviceResponse parseJsonResponse(String text) {
        try {
            JsonNode root = objectMapper.readTree(stripMarkdownFence(text));
            if (root == null || !root.isObject()) {
                return null;
            }
            String advice = getText(root, "advice");
            String template = getText(root, "messageTemplate");
            if (!StringUtils.hasText(advice) && !StringUtils.hasText(template)) {
                return null;
            }
            return new AiCommunicationAdviceResponse(
                    StringUtils.hasText(advice) ? advice : "建议先放慢沟通节奏，表达自己的感受，再提出一个具体、可协商的请求。",
                    StringUtils.hasText(template) ? template : FALLBACK_TEMPLATE,
                    sanitizeRiskLevel(getText(root, "riskLevel")),
                    REMINDER
            );
        } catch (Exception e) {
            return null;
        }
    }

    private String stripMarkdownFence(String text) {
        String trimmed = text.trim();
        if (!trimmed.startsWith("```")) {
            return trimmed;
        }
        String withoutStart = trimmed.replaceFirst("^```(?:json)?\\s*", "");
        return withoutStart.replaceFirst("\\s*```$", "").trim();
    }

    private String getText(JsonNode root, String fieldName) {
        JsonNode value = root.get(fieldName);
        return value != null && value.isTextual() ? value.asText().trim() : "";
    }

    private String sanitizeRiskLevel(String riskLevel) {
        if ("medium".equals(riskLevel) || "high".equals(riskLevel)) {
            return riskLevel;
        }
        return "low";
    }

    private AiCommunicationAdviceResponse parsePlainTextResponse(String text) {
        String advice = text.trim();
        String template = FALLBACK_TEMPLATE;
        String[] lines = advice.split("\\R");
        StringBuilder adviceBuilder = new StringBuilder();
        for (String line : lines) {
            String trimmed = line.trim();
            if (!StringUtils.hasText(trimmed)) {
                continue;
            }
            if (trimmed.startsWith("模板：")) {
                template = trimmed.substring("模板：".length()).trim();
            } else if (trimmed.startsWith("建议：")) {
                adviceBuilder.append(trimmed.substring("建议：".length()).trim());
            } else {
                if (!adviceBuilder.isEmpty()) {
                    adviceBuilder.append("\n");
                }
                adviceBuilder.append(trimmed);
            }
        }
        String parsedAdvice = StringUtils.hasText(adviceBuilder.toString()) ? adviceBuilder.toString() : advice;
        return new AiCommunicationAdviceResponse(parsedAdvice, template, "low", REMINDER);
    }
}
