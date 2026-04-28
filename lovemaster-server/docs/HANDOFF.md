# LoveMaster Server Handoff

**更新时间**：2026-04-28  
**当前阶段**：MVP v1.5 已完成  
**工作目录**：`C:\Users\91414\Desktop\project\lovemaster-server`

## 1. 当前项目状态

LoveMaster Server 是一个 Spring Boot 后端项目，定位为情侣关系管理平台的 API 服务。

当前已经完成：

1. 账号认证：注册、登录、刷新 token、退出登录。
2. 当前用户信息：`GET /api/v1/users/me`。
3. 情侣配对：邀请码配对、解除配对、查看配对状态。
4. 纪念日管理：创建、列表、详情、更新、删除、提醒查询。
5. 喜好清单：创建、列表、详情、更新、删除。
6. 约会灵感：模板查询、按预算/耗时/场景/标签筛选。
7. 约会灵感推荐：基于用户可见喜好标签的确定性规则推荐。
8. 回忆收藏夹：创建、列表、详情、更新、删除。
9. 图片上传：本地单图上传，静态 URL 访问。
10. AI 沟通建议：通义千问 / 阿里云百炼 OpenAI 兼容接口封装，默认关闭，已支持结构化 JSON 优先解析。
11. 权限隔离：个人可见、双方可见、创建者可编辑删除。
12. Docker 开发依赖：MySQL 8.0、Redis 7.2。
13. OpenAPI / Swagger UI：可被 Apifox 一键导入。
14. JSON 解析错误已返回统一 400 响应。

当前没有执行 git commit；仓库目录当前也未检测到 `.git`。

## 2. 技术栈

- Java 17
- Spring Boot 3.2.0
- Spring Security + JWT
- MyBatis-Plus 3.5.5
- MySQL 8.0
- Redis
- JUnit 5 + H2
- springdoc-openapi 2.3.0
- Docker Compose
- 通义千问 / 阿里云百炼 OpenAI 兼容 Chat Completions API

## 3. 关键文件

产品与计划：

- `docs/PRD-LoveMaster.md`
- `docs/superpowers/specs/2026-04-27-mvp-v1-4-qwen-ai-advice-design.md`
- `docs/superpowers/plans/2026-04-27-mvp-v1-4-qwen-ai-advice.md`
- `docs/superpowers/specs/2026-04-28-mvp-v1-5-ai-structured-advice-design.md`
- `docs/superpowers/plans/2026-04-28-mvp-v1-5-ai-structured-advice.md`
- `docs/HANDOFF.md`

AI 核心代码：

- `src/main/java/com/lovemaster/controller/AiController.java`
- `src/main/java/com/lovemaster/service/AiAdviceService.java`
- `src/main/java/com/lovemaster/service/impl/AiAdviceServiceImpl.java`
- `src/main/java/com/lovemaster/ai/AiClient.java`
- `src/main/java/com/lovemaster/ai/QwenAiClient.java`
- `src/main/java/com/lovemaster/ai/QwenChatRequest.java`
- `src/main/java/com/lovemaster/ai/QwenChatResponse.java`
- `src/main/java/com/lovemaster/config/AiProperties.java`
- `src/main/java/com/lovemaster/config/AiClientConfig.java`

运行与配置：

- `src/main/resources/application.yml`
- `src/main/java/com/lovemaster/config/SecurityConfig.java`
- `src/main/java/com/lovemaster/exception/ErrorCode.java`

## 4. 已实现接口

认证：

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

用户与配对：

- `GET /api/v1/users/me`
- `GET /api/v1/pairings/me`
- `POST /api/v1/pairings/bind`
- `POST /api/v1/pairings/unbind`

纪念日：

- `POST /api/v1/anniversaries`
- `GET /api/v1/anniversaries`
- `GET /api/v1/anniversaries/{id}`
- `PUT /api/v1/anniversaries/{id}`
- `DELETE /api/v1/anniversaries/{id}`
- `GET /api/v1/anniversaries/reminders`

喜好与约会灵感：

- `POST /api/v1/preferences`
- `GET /api/v1/preferences`
- `GET /api/v1/preferences/{id}`
- `PUT /api/v1/preferences/{id}`
- `DELETE /api/v1/preferences/{id}`
- `GET /api/v1/date-ideas`
- `GET /api/v1/date-ideas/recommend`

回忆与文件：

- `POST /api/v1/memories`
- `GET /api/v1/memories`
- `GET /api/v1/memories/{id}`
- `PUT /api/v1/memories/{id}`
- `DELETE /api/v1/memories/{id}`
- `POST /api/v1/files/images`
- `GET /uploads/images/{filename}`

AI：

- `POST /api/v1/ai/communication-advice`

接口文档：

- `GET /v3/api-docs`
- `GET /swagger-ui.html`

Apifox 导入地址：

```text
http://localhost:8080/v3/api-docs
```

## 5. AI 配置

默认配置在 `src/main/resources/application.yml`：

```yaml
lovemaster:
  ai:
    enabled: false
    provider: qwen
    base-url: https://dashscope.aliyuncs.com/compatible-mode/v1
    model: qwen-plus
    timeout-seconds: 20
    api-key-env-name: DASHSCOPE_API_KEY
```

重要规则：

1. 默认 `enabled=false`，不会真实调用通义千问。
2. API Key 只从环境变量 `DASHSCOPE_API_KEY` 读取。
3. 真实 API Key 禁止写入代码、配置文件、文档、测试夹具和日志。
4. 之前聊天中出现过一串 key，建议在阿里云百炼控制台轮换后再用于真实联调。
5. v1.5 要求模型优先返回 JSON，服务端会解析 `advice`、`messageTemplate`、`riskLevel`，并固定覆盖 `reminder`。

本地真实调用示例：

```powershell
$env:DASHSCOPE_API_KEY="新的通义千问key"
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" spring-boot:run -Dspring-boot.run.arguments="--lovemaster.ai.enabled=true"
```

未启用 AI 时调用 `POST /api/v1/ai/communication-advice` 会返回：

```json
{
  "code": 1801,
  "message": "AI服务未启用",
  "data": null
}
```

## 6. 本地运行方式

启动 MySQL 和 Redis：

```powershell
docker compose up -d
docker compose ps
```

如果 Docker MySQL 已经存在旧 volume，新增表可能不会自动初始化。可以手动应用 schema：

```powershell
cmd /c "docker exec -i lovemaster-mysql mysql --default-character-set=utf8mb4 -uroot -p123456 lovemaster < src\main\resources\db\schema.sql"
```

启动后端：

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" spring-boot:run
```

运行测试：

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test
```

上次完整验证结果：

- `mvn test` 通过。
- 测试数量：`59 tests`。
- Docker MySQL / Redis 已验证健康。
- 真实 HTTP smoke 已验证：注册、登录、调用 AI 沟通建议接口，默认关闭时返回 `AI服务未启用`。
- `/v3/api-docs` 已验证可访问，并包含 `/api/v1/ai/communication-advice`。
- 安全扫描未发现真实 `sk-...` API Key 写入文件。

## 7. 当前运行进程提示

最后一次验证后，本地 Spring Boot 进程仍在运行，端口 `8080` 可能已被占用。

查看当前项目 Java 进程：

```powershell
Get-CimInstance Win32_Process |
  Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*lovemaster-server*' } |
  Select-Object ProcessId, CommandLine
```

停止当前项目 Java 进程：

```powershell
Get-CimInstance Win32_Process |
  Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*lovemaster-server*' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

关闭 Docker 依赖：

```powershell
docker compose down
```

## 8. 数据库与上传文件

开发环境数据存储在 Docker MySQL：

- 容器名：`lovemaster-mysql`
- 数据库：`lovemaster`
- 用户：`root`
- 密码：`123456`
- 端口：`3306`

Redis：

- 容器名：`lovemaster-redis`
- 端口：`6379`

图片本地存储：

- 目录：`uploads/images`
- URL 前缀：`/uploads/images`
- 默认最大大小：5MB
- 支持类型：JPEG、PNG、WebP、GIF

## 9. 重要约束

1. 始终使用中文回复用户。
2. 代码注释默认使用中文。
3. 未经用户明确确认，禁止自动执行 git commit。
4. 手动编辑文件优先使用 `apply_patch`。
5. 修改功能前优先写测试，保持 TDD。
6. 声称完成前必须运行验证命令。
7. 不要回滚用户已有改动。
8. 开发环境优先使用 Docker MySQL/Redis。
9. 真实 AI API Key 不允许落盘。

## 10. 已知注意事项

1. `rg` 在当前环境可能被拒绝访问，可以使用 PowerShell `Get-ChildItem`、`Select-String` 替代。
2. Maven 不在 PATH，需使用完整 `mvn.cmd` 路径。
3. springdoc-openapi 使用 `2.3.0`，不要直接升级到最新版；最新版曾因 Spring Boot 3.2 / Spring Framework 6.1 兼容问题启动失败。
4. JSON 请求体字段名必须使用双引号；格式错误时后端会返回 `400` 和 `请求 JSON 格式错误`。
5. Windows PowerShell 直接 `Get-Content | docker exec mysql` 可能导致中文种子数据乱码，导入 schema 优先使用上文的 `cmd /c "... < schema.sql"` 写法。
6. Docker smoke 会在 MySQL 中留下测试用户。

## 11. AI 接口测试样例

登录后加 Header：

```text
Authorization: Bearer <accessToken>
```

请求：

```json
{
  "scenario": "因为晚回消息吵架了",
  "myFeeling": "委屈、生气",
  "partnerFeeling": "可能觉得我管太多",
  "goal": "想好好表达，不想继续吵"
}
```

接口：

```text
POST /api/v1/ai/communication-advice
```

成功响应中的 `data`：

```json
{
  "advice": "先表达自己的感受，再提出一个具体、可协商的请求。",
  "messageTemplate": "我感到有些委屈，是因为晚回消息让我担心。我希望下次很忙时可以简单说一声。",
  "riskLevel": "low",
  "reminder": "建议仅供参考，具体做法需要结合你们双方情况协商。"
}
```

## 12. 下次继续时的建议第一步

1. 先读本文件。
2. 再读 `docs/PRD-LoveMaster.md`。
3. 再读 `docs/superpowers/plans/2026-04-28-mvp-v1-5-ai-structured-advice.md`。
4. 运行：

```powershell
docker compose ps
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test
```

5. 如果要做真实 AI 联调，先轮换 API Key，再设置 `DASHSCOPE_API_KEY` 并显式启用 `lovemaster.ai.enabled=true`。
