# MVP v1.5 AI 沟通建议结构化实施计划

**日期**：2026-04-28  
**状态**：已完成  

## 目标

让 `POST /api/v1/ai/communication-advice` 在真实模型输出波动时仍返回稳定结构，解决 v1.4 中 `messageTemplate` 可能为空或无法拆分的问题。

## 实施步骤

1. 测试先行
   - 增加标准 JSON 返回解析测试。
   - 增加普通文本返回兜底测试。
   - Controller 测试断言 `riskLevel` 字段。

2. DTO 调整
   - `AiCommunicationAdviceResponse` 增加 `riskLevel`。
   - 保留旧的三参数构造方法，降低已有调用改动面。

3. Prompt 调整
   - 系统提示词要求模型只返回合法 JSON。
   - 明确字段为 `advice`、`messageTemplate`、`riskLevel`、`reminder`。
   - 明确 `riskLevel` 枚举范围。

4. 服务端解析
   - 注入 `ObjectMapper`。
   - 优先解析 JSON。
   - 兼容 Markdown 代码块包裹的 JSON。
   - 非 JSON 返回走旧文本拆分。
   - 缺字段时使用保守兜底。
   - `reminder` 固定由服务端返回。

5. 文档同步
   - 更新 PRD 到 v1.5。
   - 更新 handoff，方便下次继续。

6. 验证
   - 先运行 `AiAdviceServiceTest`。
   - 再运行完整 `mvn test`。

## 不包含

- 不新增 AI 调用日志表。
- 不持久化用户输入或 AI 输出。
- 不做多轮 Agent。
- 不做前端页面。
