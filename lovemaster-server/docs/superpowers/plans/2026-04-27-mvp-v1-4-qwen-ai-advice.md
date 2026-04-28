# MVP v1.4 Qwen AI Advice Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 接入通义千问 / 阿里云百炼，提供一个登录后可调用的一次性 AI 沟通建议接口。

**Architecture:** Controller 只负责 REST 入参与统一响应；`AiAdviceService` 负责 prompt 组织、配置校验和异常转换；`AiClient` 抽象外部模型调用，测试中 mock，生产中由 `QwenAiClient` 调用通义千问 OpenAI 兼容接口。API Key 只从 `DASHSCOPE_API_KEY` 环境变量读取，不写入仓库文件。

**Tech Stack:** Java 17、Spring Boot 3.2.0、Spring Security、JWT、JUnit 5、Mockito、Jackson、Spring `RestClient`、springdoc-openapi 2.3.0、通义千问 OpenAI 兼容 Chat Completions API。

---

## File Structure

### Existing Files To Modify

- `pom.xml`：无需新增依赖；使用 Spring Boot Web 已包含的 HTTP 与 Jackson 能力。
- `src/main/resources/application.yml`：新增 `lovemaster.ai` 非敏感默认配置。
- `src/main/java/com/lovemaster/exception/ErrorCode.java`：新增 AI 错误码。
- `src/test/java/com/lovemaster/controller/OpenApiControllerTest.java`：校验新接口出现在 OpenAPI 文档。
- `docs/HANDOFF.md`：实现完成后更新 v1.4 状态。

### New Config And AI Client Files

- `src/main/java/com/lovemaster/config/AiProperties.java`：绑定 AI 配置。
- `src/main/java/com/lovemaster/config/AiClientConfig.java`：创建 `RestClient` Bean，统一配置 base URL 和超时。
- `src/main/java/com/lovemaster/ai/AiClient.java`：外部 AI 调用抽象接口。
- `src/main/java/com/lovemaster/ai/QwenAiClient.java`：通义千问 OpenAI 兼容接口实现。
- `src/main/java/com/lovemaster/ai/QwenChatRequest.java`：通义请求体内部模型。
- `src/main/java/com/lovemaster/ai/QwenChatResponse.java`：通义响应体内部模型。

### New DTO Files

- `src/main/java/com/lovemaster/dto/request/AiCommunicationAdviceRequest.java`
- `src/main/java/com/lovemaster/dto/response/AiCommunicationAdviceResponse.java`

### New Service And Controller Files

- `src/main/java/com/lovemaster/service/AiAdviceService.java`
- `src/main/java/com/lovemaster/service/impl/AiAdviceServiceImpl.java`
- `src/main/java/com/lovemaster/controller/AiController.java`

### New Test Files

- `src/test/java/com/lovemaster/service/AiAdviceServiceTest.java`
- `src/test/java/com/lovemaster/ai/QwenAiClientTest.java`
- `src/test/java/com/lovemaster/controller/AiControllerTest.java`

---

## Shared Decisions

- 默认配置 `lovemaster.ai.enabled=false`，避免开发环境无意调用真实模型。
- API Key 环境变量名固定为 `DASHSCOPE_API_KEY`。
- 默认 base URL 为 `https://dashscope.aliyuncs.com/compatible-mode/v1`。
- 默认模型为 `qwen-plus`。
- 真实调用 endpoint 为 `POST /chat/completions`。
- 测试使用 `@MockBean AiClient` 或 mock `RestClient` 相关边界，不真实调用通义千问。
- MVP 不强依赖模型返回 JSON。服务端返回固定 `reminder`，`advice` 和 `messageTemplate` 可从文本做轻量拆分；无法拆分时使用保守兜底模板。
- 不记录 prompt、用户输入、API Key、Authorization header 到日志。
- 当前目录没有 `.git`，计划不包含 git commit 步骤。

---

### Task 1: AI Config And Error Codes

**Files:**
- Modify: `src/main/resources/application.yml`
- Modify: `src/main/java/com/lovemaster/exception/ErrorCode.java`
- Create: `src/main/java/com/lovemaster/config/AiProperties.java`
- Test: `src/test/java/com/lovemaster/service/AiAdviceServiceTest.java`

- [ ] **Step 1: Add failing service test shell**

Create `src/test/java/com/lovemaster/service/AiAdviceServiceTest.java`:

```java
package com.lovemaster.service;

import com.lovemaster.ai.AiClient;
import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.context.TestPropertySource;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@TestPropertySource(properties = {
        "lovemaster.ai.enabled=true",
        "lovemaster.ai.api-key-env-name=TEST_DASHSCOPE_API_KEY"
})
class AiAdviceServiceTest {

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

        assertNotNull(response.getAdvice());
        assertTrue(response.getAdvice().contains("表达"));
        assertNotNull(response.getMessageTemplate());
        assertTrue(response.getReminder().contains("建议仅供参考"));
    }

    private AiCommunicationAdviceRequest createRequest() {
        AiCommunicationAdviceRequest request = new AiCommunicationAdviceRequest();
        request.setScenario("因为晚回消息吵架了");
        request.setMyFeeling("委屈、生气");
        request.setPartnerFeeling("可能觉得我管太多");
        request.setGoal("想好好表达，不想继续吵");
        return request;
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AiAdviceServiceTest
```

Expected: compilation failure because `AiAdviceService`、`AiClient` and AI DTOs do not exist.

- [ ] **Step 3: Add AI configuration**

Modify `src/main/resources/application.yml` and merge under existing `lovemaster:`:

```yaml
lovemaster:
  upload:
    image-dir: uploads/images
    image-url-prefix: /uploads/images
    max-image-size: 5242880
  ai:
    enabled: false
    provider: qwen
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    model: qwen-plus
    timeout-seconds: 20
    api-key-env-name: DASHSCOPE_API_KEY
```

- [ ] **Step 4: Add AI properties**

Create `src/main/java/com/lovemaster/config/AiProperties.java`:

```java
package com.lovemaster.config;

import lombok.Data;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

@Data
@Configuration
@ConfigurationProperties(prefix = "lovemaster.ai")
public class AiProperties {

    private boolean enabled = false;

    private String provider = "qwen";

    private String baseUrl = "https://dashscope.aliyuncs.com/compatible-mode/v1";

    private String model = "qwen-plus";

    private long timeoutSeconds = 20;

    private String apiKeyEnvName = "DASHSCOPE_API_KEY";
}
```

- [ ] **Step 5: Add AI error codes**

Modify `src/main/java/com/lovemaster/exception/ErrorCode.java`, changing the last file error semicolon to comma and appending:

```java

    // AI相关 1800-1899
    AI_DISABLED(1801, "AI服务未启用"),
    AI_API_KEY_MISSING(1802, "AI API Key未配置"),
    AI_PROVIDER_ERROR(1803, "AI服务调用失败"),
    AI_RESPONSE_INVALID(1804, "AI响应格式异常");
```

- [ ] **Step 6: Run test again**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AiAdviceServiceTest
```

Expected: still fails because service, client and DTO classes are not implemented yet.

---

### Task 2: AI Advice Service With Mock Client

**Files:**
- Create: `src/main/java/com/lovemaster/ai/AiClient.java`
- Create: `src/main/java/com/lovemaster/dto/request/AiCommunicationAdviceRequest.java`
- Create: `src/main/java/com/lovemaster/dto/response/AiCommunicationAdviceResponse.java`
- Create: `src/main/java/com/lovemaster/service/AiAdviceService.java`
- Create: `src/main/java/com/lovemaster/service/impl/AiAdviceServiceImpl.java`
- Test: `src/test/java/com/lovemaster/service/AiAdviceServiceTest.java`

- [ ] **Step 1: Replace service test with full coverage**

Replace `src/test/java/com/lovemaster/service/AiAdviceServiceTest.java`:

```java
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
```

- [ ] **Step 2: Run test to verify failures**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AiAdviceServiceTest
```

Expected: compilation failure because implementation classes do not exist.

- [ ] **Step 3: Create AI client interface**

Create `src/main/java/com/lovemaster/ai/AiClient.java`:

```java
package com.lovemaster.ai;

public interface AiClient {

    String generate(String systemPrompt, String userPrompt);
}
```

- [ ] **Step 4: Create request DTO**

Create `src/main/java/com/lovemaster/dto/request/AiCommunicationAdviceRequest.java`:

```java
package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiCommunicationAdviceRequest {

    @NotBlank(message = "场景描述不能为空")
    @Size(max = 500, message = "场景描述不能超过500字符")
    private String scenario;

    @Size(max = 200, message = "我的感受不能超过200字符")
    private String myFeeling;

    @Size(max = 200, message = "对方可能的感受不能超过200字符")
    private String partnerFeeling;

    @Size(max = 200, message = "沟通目标不能超过200字符")
    private String goal;
}
```

- [ ] **Step 5: Create response DTO**

Create `src/main/java/com/lovemaster/dto/response/AiCommunicationAdviceResponse.java`:

```java
package com.lovemaster.dto.response;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class AiCommunicationAdviceResponse {

    private String advice;

    private String messageTemplate;

    private String reminder;
}
```

- [ ] **Step 6: Create service interface**

Create `src/main/java/com/lovemaster/service/AiAdviceService.java`:

```java
package com.lovemaster.service;

import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;

public interface AiAdviceService {

    AiCommunicationAdviceResponse generate(AiCommunicationAdviceRequest request);
}
```

- [ ] **Step 7: Create service implementation**

Create `src/main/java/com/lovemaster/service/impl/AiAdviceServiceImpl.java`:

```java
package com.lovemaster.service.impl;

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
                输出包含两部分：
                建议：一段简短沟通建议。
                模板：一段可直接参考的话术，尽量使用“我感到...因为...我希望...”结构。
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
        return new AiCommunicationAdviceResponse(parsedAdvice, template, REMINDER);
    }
}
```

- [ ] **Step 8: Run service tests**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AiAdviceServiceTest
```

Expected: PASS.

---

### Task 3: Qwen AI Client

**Files:**
- Create: `src/main/java/com/lovemaster/config/AiClientConfig.java`
- Create: `src/main/java/com/lovemaster/ai/QwenChatRequest.java`
- Create: `src/main/java/com/lovemaster/ai/QwenChatResponse.java`
- Create: `src/main/java/com/lovemaster/ai/QwenAiClient.java`
- Test: `src/test/java/com/lovemaster/ai/QwenAiClientTest.java`

- [ ] **Step 1: Write failing Qwen client tests**

Create `src/test/java/com/lovemaster/ai/QwenAiClientTest.java`:

```java
package com.lovemaster.ai;

import com.lovemaster.config.AiProperties;
import com.lovemaster.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.web.client.RestClient;

import static org.junit.jupiter.api.Assertions.*;

class QwenAiClientTest {

    @Test
    @DisplayName("空响应内容会转换为业务异常")
    void emptyResponseShouldFail() {
        AiProperties properties = new AiProperties();
        properties.setEnabled(true);
        properties.setModel("qwen-plus");
        RestClient restClient = RestClient.builder()
                .baseUrl("http://localhost")
                .requestFactory(request -> {
                    throw new RuntimeException("blocked in unit test");
                })
                .build();
        QwenAiClient client = new QwenAiClient(properties, restClient);

        assertThrows(BusinessException.class, () -> client.generate("system", "user"));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=QwenAiClientTest
```

Expected: compilation failure because `QwenAiClient` does not exist.

- [ ] **Step 3: Create AI client config**

Create `src/main/java/com/lovemaster/config/AiClientConfig.java`:

```java
package com.lovemaster.config;

import org.springframework.boot.web.client.ClientHttpRequestFactories;
import org.springframework.boot.web.client.ClientHttpRequestFactorySettings;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.client.RestClient;

import java.time.Duration;

@Configuration
public class AiClientConfig {

    @Bean
    public RestClient qwenRestClient(AiProperties aiProperties) {
        ClientHttpRequestFactorySettings settings = ClientHttpRequestFactorySettings.DEFAULTS
                .withConnectTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()))
                .withReadTimeout(Duration.ofSeconds(aiProperties.getTimeoutSeconds()));
        return RestClient.builder()
                .baseUrl(aiProperties.getBaseUrl())
                .requestFactory(ClientHttpRequestFactories.get(settings))
                .build();
    }
}
```

- [ ] **Step 4: Create Qwen request model**

Create `src/main/java/com/lovemaster/ai/QwenChatRequest.java`:

```java
package com.lovemaster.ai;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class QwenChatRequest {

    private String model;

    private List<Message> messages;

    private Double temperature;

    @Data
    @NoArgsConstructor
    @AllArgsConstructor
    public static class Message {
        private String role;
        private String content;
    }
}
```

- [ ] **Step 5: Create Qwen response model**

Create `src/main/java/com/lovemaster/ai/QwenChatResponse.java`:

```java
package com.lovemaster.ai;

import lombok.Data;

import java.util.List;

@Data
public class QwenChatResponse {

    private List<Choice> choices;

    @Data
    public static class Choice {
        private Message message;
    }

    @Data
    public static class Message {
        private String role;
        private String content;
    }
}
```

- [ ] **Step 6: Implement Qwen client**

Create `src/main/java/com/lovemaster/ai/QwenAiClient.java`:

```java
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
```

- [ ] **Step 7: Run Qwen client test**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=QwenAiClientTest
```

Expected: PASS.

- [ ] **Step 8: Run service tests again**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AiAdviceServiceTest
```

Expected: PASS.

---

### Task 4: AI Controller And OpenAPI

**Files:**
- Create: `src/main/java/com/lovemaster/controller/AiController.java`
- Test: `src/test/java/com/lovemaster/controller/AiControllerTest.java`
- Modify: `src/test/java/com/lovemaster/controller/OpenApiControllerTest.java`

- [ ] **Step 1: Write failing controller tests**

Create `src/test/java/com/lovemaster/controller/AiControllerTest.java`:

```java
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
                "建议仅供参考，具体做法需要结合你们双方情况协商。"
        ));

        mockMvc.perform(post("/api/v1/ai/communication-advice")
                        .with(authentication(authToken()))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(createRequest())))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.message").value("生成成功"))
                .andExpect(jsonPath("$.data.advice").value("建议先表达感受，再提出具体请求。"));
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
```

- [ ] **Step 2: Run controller test to verify failure**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AiControllerTest
```

Expected: FAIL because `/api/v1/ai/communication-advice` does not exist.

- [ ] **Step 3: Implement controller**

Create `src/main/java/com/lovemaster/controller/AiController.java`:

```java
package com.lovemaster.controller;

import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.service.AiAdviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAdviceService aiAdviceService;

    @PostMapping("/communication-advice")
    public ApiResponse<AiCommunicationAdviceResponse> generateCommunicationAdvice(
            @Valid @RequestBody AiCommunicationAdviceRequest request) {
        return ApiResponse.success("生成成功", aiAdviceService.generate(request));
    }
}
```

- [ ] **Step 4: Run controller tests**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AiControllerTest
```

Expected: PASS.

- [ ] **Step 5: Expand OpenAPI test**

Modify `src/test/java/com/lovemaster/controller/OpenApiControllerTest.java` and append:

```java
.andExpect(jsonPath("$.paths", hasKey("/api/v1/ai/communication-advice")));
```

The final assertions should include the AI path after `/api/v1/files/images`.

- [ ] **Step 6: Run OpenAPI test**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=OpenApiControllerTest
```

Expected: PASS.

---

### Task 5: Full Verification And Handoff Update

**Files:**
- Modify: `docs/HANDOFF.md`
- Optional verify only: `docs/PRD-LoveMaster.md`

- [ ] **Step 1: Run all tests**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test
```

Expected: BUILD SUCCESS. Test count should increase from 51.

- [ ] **Step 2: Verify no API key was written to files**

Run:

```powershell
Get-ChildItem -Recurse -File |
  Where-Object { $_.FullName -notlike '*\.git*' } |
  Select-String -Pattern 'sk-[A-Za-z0-9]{20,}' -ErrorAction SilentlyContinue
```

Expected: no output.

Run:

```powershell
Select-String -Path src\main\java\**,src\main\resources\**,src\test\java\**,docs\** -Pattern 'DASHSCOPE_API_KEY|dashscope.aliyuncs.com|qwen-plus' -ErrorAction SilentlyContinue
```

Expected: only environment variable name, non-sensitive base URL, and model name appear.

- [ ] **Step 3: Docker dependencies check**

Run:

```powershell
docker compose up -d
docker compose ps
```

Expected: `lovemaster-mysql` and `lovemaster-redis` are healthy.

- [ ] **Step 4: Start backend without real AI key**

If port 8080 is occupied by this project, stop it first:

```powershell
Get-CimInstance Win32_Process |
  Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*lovemaster-server*' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

Start app:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" spring-boot:run
```

Expected: app starts because `lovemaster.ai.enabled=false` by default and does not require `DASHSCOPE_API_KEY` at startup.

- [ ] **Step 5: Manual HTTP smoke test without AI enabled**

Register/login and call:

```text
POST /api/v1/ai/communication-advice
Authorization: Bearer <accessToken>
```

Expected: HTTP 200 transport with business response:

```json
{
  "code": 1801,
  "message": "AI服务未启用",
  "data": null
}
```

This verifies the route, auth, validation and disabled fallback without calling real AI.

- [ ] **Step 6: Optional real Qwen smoke test**

Only run if the user has rotated the leaked key and explicitly wants a real external call.

Set environment variables in the same shell before starting Spring Boot:

```powershell
$env:DASHSCOPE_API_KEY="新的通义千问key"
```

Temporarily override AI enabled:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.arguments="--lovemaster.ai.enabled=true"
```

Call `POST /api/v1/ai/communication-advice`.

Expected: business `code=200` and `data.advice` has Chinese advice.

- [ ] **Step 7: Update handoff**

Update `docs/HANDOFF.md`:

- Current stage: `MVP v1.4 已完成` if verification passes.
- Add new interface: `POST /api/v1/ai/communication-advice`.
- Add AI configuration:
  - `lovemaster.ai.enabled`
  - `lovemaster.ai.base-url`
  - `lovemaster.ai.model`
  - `DASHSCOPE_API_KEY`
- Add warning: real API Key must not be written to files; leaked key should be rotated.
- Add latest test count.
- Add note that default `enabled=false` means production must explicitly enable AI.

- [ ] **Step 8: Final full test**

Run:

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test
```

Expected: BUILD SUCCESS.

---

## Self-Review Checklist

- [x] Spec coverage: AI advice endpoint, Qwen provider, config, API key handling, mock tests, OpenAPI and handoff are covered.
- [x] Scope control: no mood persistence, no long conversation, no Agent tools, no reading historical private data.
- [x] Type consistency: request/response DTOs, service signatures, client signatures and controller path are consistent.
- [x] Security: API Key is read only from environment variable and never written to files.
- [x] Testing: service, client, controller, OpenAPI and full suite verification are included.
- [x] No placeholders: every task has concrete file paths, commands and expected outcomes.
- [x] Git constraint: no commit step is included because the directory has no `.git` and the user has not requested a commit.
