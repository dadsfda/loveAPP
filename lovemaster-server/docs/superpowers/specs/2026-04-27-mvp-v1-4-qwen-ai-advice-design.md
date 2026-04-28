# MVP v1.4 通义千问 AI 沟通建议设计文档

**日期**：2026-04-27  
**状态**：待评审  
**阶段**：MVP v1.4  
**供应商**：通义千问 / 阿里云百炼  

## 1. 背景

LoveMaster v1.1-v1.3 已完成账号认证、情侣配对、纪念日、喜好清单、约会灵感、回忆收藏夹和图片上传。当前产品已经具备“记录”和“行动灵感”能力，但在用户出现表达困难或矛盾升级前，仍缺少轻量的沟通辅助。

v1.4 引入通义千问 API，但只做边界明确的一次性“AI 沟通建议”，不做长对话 Agent、不做关系裁判、不保存用户输入。

## 2. 目标

1. 提供一个登录后可调用的 AI 沟通建议接口。
2. 根据用户输入的场景、感受和目标，生成温和、非指责式沟通建议。
3. 使用通义千问 OpenAI 兼容接口，便于后续替换供应商。
4. API Key、模型、base URL、超时时间通过配置管理。
5. 自动化测试不依赖真实外部 AI 调用。

## 3. 非目标

1. 不做情绪记录或冲突复盘的数据库持久化。
2. 不读取用户历史纪念日、喜好、回忆等隐私数据。
3. 不做多轮对话、上下文记忆或 Agent 工具调用。
4. 不判断谁对谁错。
5. 不提供医疗、法律、心理诊断建议。
6. 不把 API Key 写入代码、配置文件、文档、测试夹具或日志。

## 4. 用户故事

1. 作为不擅表达的一方，我希望输入“因为晚回消息吵架了”和我的感受，得到一段更温和的表达方式。
2. 作为正在降温的一方，我希望系统提醒我不要使用绝对化、攻击性表达。
3. 作为开发者，我希望没有配置 API Key 时接口返回明确错误，而不是 500。
4. 作为开发者，我希望测试时可以 mock AI 客户端，不真实消耗模型调用。

## 5. 接口设计

### 5.1 生成沟通建议

```text
POST /api/v1/ai/communication-advice
Authorization: Bearer <accessToken>
Content-Type: application/json
```

请求体：

```json
{
  "scenario": "因为晚回消息吵架了",
  "myFeeling": "委屈、生气",
  "partnerFeeling": "可能觉得我管太多",
  "goal": "想好好表达，不想继续吵"
}
```

字段规则：

- `scenario`：必填，1 到 500 字。
- `myFeeling`：选填，最多 200 字。
- `partnerFeeling`：选填，最多 200 字。
- `goal`：选填，最多 200 字。

响应体：

```json
{
  "code": 200,
  "message": "生成成功",
  "data": {
    "advice": "建议先承认自己的感受，再用具体事件表达需求，避免把问题扩大为对方的人格评价。",
    "messageTemplate": "我刚才有点委屈，是因为晚回消息让我担心自己不被重视。我想和你商量一下，下次如果很忙，能不能简单告诉我一声？",
    "reminder": "建议仅供参考，具体做法需要结合你们双方情况协商。"
  }
}
```

## 6. 安全边界

系统提示词必须约束 AI：

1. 使用中文输出。
2. 不判断情侣关系谁对谁错。
3. 不鼓励控制、试探、报复、冷暴力或道德绑架。
4. 不建议用户忍受暴力、羞辱、威胁或控制。
5. 不做医疗、法律、心理诊断。
6. 输出应包含可协商、可执行、低攻击性的表达。
7. 如果用户输入涉及自伤、暴力威胁或人身安全风险，优先给出安全提醒和寻求现实帮助的建议。
8. 输出固定为三段含义：沟通建议、表达模板、安全提醒。

## 7. 技术设计

### 7.1 分层

- `AiController`：接收 REST 请求，做参数校验和统一响应包装。
- `AiAdviceService`：组织业务 prompt，处理启用状态、异常转换和返回结构。
- `AiClient`：抽象外部模型调用接口，便于 mock 和替换供应商。
- `QwenAiClient`：调用通义千问 OpenAI 兼容接口。
- `AiProperties`：绑定 `lovemaster.ai` 配置。

### 7.2 配置

配置文件只保存非敏感默认值：

```yaml
lovemaster:
  ai:
    enabled: false
    provider: qwen
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    model: qwen-plus
    timeout-seconds: 20
```

API Key 只从环境变量读取：

```text
DASHSCOPE_API_KEY
```

本地运行示例：

```powershell
$env:DASHSCOPE_API_KEY="你的新key"
```

### 7.3 通义千问调用

使用 OpenAI 兼容 Chat Completions 接口：

```text
POST https://dashscope.aliyuncs.com/compatible-mode/v1/chat/completions
Authorization: Bearer ${DASHSCOPE_API_KEY}
Content-Type: application/json
```

请求体采用：

```json
{
  "model": "qwen-plus",
  "messages": [
    {
      "role": "system",
      "content": "系统安全提示词"
    },
    {
      "role": "user",
      "content": "用户场景和目标"
    }
  ],
  "temperature": 0.4
}
```

### 7.4 返回解析

MVP 不强依赖模型输出 JSON，避免因为模型偶发格式问题导致接口不可用。服务端将模型返回文本解析为：

- `advice`：模型主建议。
- `messageTemplate`：如果模型输出中无法稳定拆分，使用完整文本作为建议，并返回一条保守模板。
- `reminder`：服务端固定写入，不依赖模型输出。

后续可以再升级为严格 JSON 输出或结构化输出。

## 8. 错误处理

新增业务错误码：

- `AI_DISABLED`：AI 服务未启用。
- `AI_API_KEY_MISSING`：AI API Key 未配置。
- `AI_PROVIDER_ERROR`：AI 服务调用失败。
- `AI_RESPONSE_INVALID`：AI 响应格式异常。

错误响应遵循现有 `ApiResponse<T>` 结构。

## 9. 测试策略

1. `AiAdviceServiceTest`
   - AI 启用且 mock 客户端返回成功时，服务返回建议、模板和固定提醒。
   - AI 未启用时抛出业务异常。
   - API Key 缺失时抛出业务异常。
   - 客户端调用失败时转换为业务异常。

2. `AiControllerTest`
   - 登录用户可以调用 `POST /api/v1/ai/communication-advice`。
   - 未登录用户被拒绝。
   - `scenario` 为空返回参数错误。

3. `OpenApiControllerTest`
   - `/v3/api-docs` 包含 `/api/v1/ai/communication-advice`。

完整测试仍运行：

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test
```

## 10. 验收标准

1. 已登录用户可以调用 AI 沟通建议接口。
2. 接口返回沟通建议、表达模板和固定安全提醒。
3. 未登录用户不能调用。
4. 未启用 AI 时返回明确业务错误。
5. 未配置 `DASHSCOPE_API_KEY` 时返回明确业务错误。
6. 外部 AI API 失败时返回明确业务错误。
7. 自动化测试不调用真实通义千问 API。
8. OpenAPI 文档包含新接口。
9. 文档和代码中不包含真实 API Key。

## 11. 自检

- 无 `TBD` / `TODO` 占位。
- 范围聚焦在一次性 AI 沟通建议。
- 不保存用户输入，隐私边界清晰。
- API Key 不落盘。
- 测试策略覆盖成功、未启用、未配置、外部失败和鉴权。
