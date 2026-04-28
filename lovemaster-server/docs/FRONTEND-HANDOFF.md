# LoveMaster Mobile Frontend Handoff

**更新时间**：2026-04-28  
**当前阶段**：移动端 H5 前端 MVP 已创建，P0/P1 主链路已跑通  
**后端目录**：`C:\Users\91414\Desktop\project\lovemaster-server`  
**前端目录**：`C:\Users\91414\Desktop\project\lovemaster-web`

## 1. 交接目标

本文件用于新开对话后直接继续 LoveMaster 移动端前端开发。前端已创建并完成第一轮真实接口联调，后续不要重新脚手架项目。

当前优先目标：

1. 继续完善统一加载态、空状态、错误态、成功态。
2. 做更完整的移动端小屏视觉走查和真机交互检查。
3. 补充更多前端组件级测试或核心页面集成测试。
4. 评估任务 / 共同目标等后端未实现模块是否进入下一阶段。

## 2. 必读文件

新对话开始后先读：

1. `C:\Users\91414\Desktop\project\lovemaster-server\docs\FRONTEND-PRD-LoveMaster-Mobile.md`
2. `C:\Users\91414\Desktop\project\lovemaster-server\docs\PRD-LoveMaster.md`
3. `C:\Users\91414\Desktop\project\lovemaster-server\docs\HANDOFF.md`
4. `C:\Users\91414\Desktop\project\lovemaster-server\docs\images\产品原型图.png`

## 3. 当前后端状态

后端项目：`lovemaster-server`

技术栈：

- Spring Boot 3.2.0
- Java 17
- Spring Security + JWT
- MyBatis-Plus
- MySQL 8.0
- Redis
- OpenAPI / Swagger UI

当前后端已完成：

1. 注册、登录、刷新 token、退出登录。
2. 当前用户信息。
3. 情侣配对。
4. 纪念日管理。
5. 喜好清单。
6. 约会灵感与规则推荐。
7. 回忆收藏夹。
8. 图片上传。
9. AI 沟通建议，默认关闭，已支持结构化 JSON 返回。
10. CORS 已允许本地前端 `http://localhost:5173` 调用后端接口。

当前后端没有完整实现：

1. 任务 / 家务分工。
2. 共同目标。
3. 推送通知。
4. 手机号验证码登录。
5. 多图回忆。

这些未实现模块前端先占位或隐藏，不要假装已经接入真实接口。

## 4. 原型图说明

最新原型图已保存：

```text
C:\Users\91414\Desktop\project\lovemaster-server\docs\images\产品原型图.png
```

原型图包含以下移动端页面方向：

1. 登录 / 注册。
2. 首页 / 纪念日。
3. 配对 / 我们的关系。
4. 喜好档案。
5. 约会灵感。
6. 专属回忆。
7. 沟通建议 / AI。
8. 我的 / 设置。

实现时优先保持统一移动端 App 体验：底部 Tab、白色卡片、柔和粉色主色、轻阴影、清晰中文 UI。

## 5. 建议前端技术栈

当前前端已采用：

- React
- Vite
- TypeScript
- React Router
- Axios
- Zustand
- 自定义轻量移动端组件
- lucide-react 图标

不要切换技术栈，继续沿用现有结构。

前端目录：

```text
C:\Users\91414\Desktop\project\lovemaster-web
```

不要把前端项目直接塞进后端 `src/main` 目录。

当前前端关键文件：

```text
C:\Users\91414\Desktop\project\lovemaster-web\src\api\client.ts
C:\Users\91414\Desktop\project\lovemaster-web\src\stores\authStore.ts
C:\Users\91414\Desktop\project\lovemaster-web\src\router\index.tsx
C:\Users\91414\Desktop\project\lovemaster-web\src\components\BottomTabBar.tsx
C:\Users\91414\Desktop\project\lovemaster-web\src\pages\HomePage.tsx
C:\Users\91414\Desktop\project\lovemaster-web\src\pages\PreferencePage.tsx
C:\Users\91414\Desktop\project\lovemaster-web\src\pages\MemoriesPage.tsx
C:\Users\91414\Desktop\project\lovemaster-web\src\pages\AiAdvicePage.tsx
C:\Users\91414\Desktop\project\lovemaster-web\src\styles\global.css
```

## 6. 后端运行方式

进入后端目录：

```powershell
cd C:\Users\91414\Desktop\project\lovemaster-server
```

启动 Docker 依赖：

```powershell
docker compose up -d
docker compose ps
```

启动后端：

```powershell
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

API 前缀：

```text
http://localhost:8080/api/v1
```

OpenAPI：

```text
http://localhost:8080/v3/api-docs
```

Swagger UI：

```text
http://localhost:8080/swagger-ui.html
```

注意：当前环境里 `mvn` 可能不在 PATH，优先使用上面的完整 Maven 路径。

本机 Node 已安装在：

```text
D:\nodejs
```

如果当前 PowerShell 找不到 `npm`，先执行：

```powershell
$env:Path = 'D:\nodejs;' + $env:Path
```

前端启动：

```powershell
cd C:\Users\91414\Desktop\project\lovemaster-web
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

## 7. 前端接口清单

### 7.1 认证

- `POST /api/v1/auth/register`
- `POST /api/v1/auth/login`
- `POST /api/v1/auth/refresh`
- `POST /api/v1/auth/logout`

登录后保存：

- `accessToken`
- `refreshToken`
- `tokenType`

请求头：

```text
Authorization: Bearer <accessToken>
```

### 7.2 用户与配对

- `GET /api/v1/users/me`
- `GET /api/v1/pairings/me`
- `POST /api/v1/pairings/bind`
- `POST /api/v1/pairings/unbind`

### 7.3 纪念日

- `POST /api/v1/anniversaries`
- `GET /api/v1/anniversaries`
- `GET /api/v1/anniversaries/{id}`
- `PUT /api/v1/anniversaries/{id}`
- `DELETE /api/v1/anniversaries/{id}`
- `GET /api/v1/anniversaries/reminders`

### 7.4 喜好

- `POST /api/v1/preferences`
- `GET /api/v1/preferences`
- `GET /api/v1/preferences/{id}`
- `PUT /api/v1/preferences/{id}`
- `DELETE /api/v1/preferences/{id}`

### 7.5 约会灵感

- `GET /api/v1/date-ideas`
- `GET /api/v1/date-ideas/recommend`

### 7.6 回忆与图片

- `POST /api/v1/memories`
- `GET /api/v1/memories`
- `GET /api/v1/memories/{id}`
- `PUT /api/v1/memories/{id}`
- `DELETE /api/v1/memories/{id}`
- `POST /api/v1/files/images`
- `GET /uploads/images/{filename}`

上传图片使用 `multipart/form-data`。

### 7.7 AI 沟通建议

- `POST /api/v1/ai/communication-advice`

请求：

```json
{
  "scenario": "因为晚回消息吵架了",
  "myFeeling": "委屈、生气",
  "partnerFeeling": "可能觉得我管太多",
  "goal": "想好好表达，不想继续吵"
}
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

AI 默认关闭时返回：

```json
{
  "code": 1801,
  "message": "AI服务未启用",
  "data": null
}
```

前端需要把它显示成友好提示，例如“AI 服务暂未启用，可以稍后再试”。

## 8. 统一响应与错误处理

后端统一响应结构：

```json
{
  "code": 200,
  "message": "成功",
  "data": {}
}
```

前端 Axios 封装建议：

1. 设置 `baseURL=http://localhost:8080/api/v1`。
2. 请求拦截器自动加 `Authorization`。
3. 响应拦截器统一拆 `data`。
4. `code !== 200` 时抛出业务错误。
5. HTTP 401 时清 token 并跳登录页。

常见业务码：

- `1801`：AI 服务未启用。
- `1802`：AI API Key 未配置。
- `1803`：AI 服务调用失败。
- `1804`：AI 响应格式异常。

## 9. 第一阶段建议开发顺序

第一阶段 P0 已完成：

1. 创建 `lovemaster-web`。
2. 配置 Vite + React + TypeScript。
3. 建立目录结构：

```text
src/
├── api/
├── assets/
├── components/
├── pages/
├── router/
├── stores/
├── styles/
├── types/
└── utils/
```

4. 实现 API 客户端：

```text
src/api/client.ts
src/api/auth.ts
src/api/user.ts
src/api/pairing.ts
```

5. 实现页面：

```text
src/pages/LoginPage.tsx
src/pages/RegisterPage.tsx
src/pages/HomePage.tsx
src/pages/ProfilePage.tsx
src/pages/PairingPage.tsx
```

6. 实现移动端 App Shell：

```text
src/components/AppShell.tsx
src/components/BottomTabBar.tsx
```

7. 启动前端：

```powershell
npm install
npm run dev
```

8. 用浏览器打开 Vite 地址，验证登录和 Tab 切换。

已手动走通真实链路：

```text
注册账号 -> 登录 -> 首页 -> 复制邀请码 -> 再注册第二个账号 -> 配对 -> 新增纪念日 -> 看我的页
```

当前底部 Tab：

```text
首页 / 喜好 / 灵感 / 回忆 / 沟通 / 我的
```

“我们的关系 / 配对”已放到“我的”页面菜单第一项，不再占用底部 Tab。

## 10. 第二阶段建议开发顺序

第二阶段接入核心真实业务：

1. 纪念日列表和新增。
2. 回忆列表、图片上传、新增回忆。
3. AI 沟通建议页面。
4. 喜好档案列表和新增。
5. 约会灵感列表和筛选。

当前完成情况：

1. 首页已接入纪念日列表、新增、编辑、删除，表单默认收起，点击加号或编辑按钮展开。
2. 回忆页已接入列表、图片上传、本地预览、新增、编辑、删除、地点、标签、可见性，表单默认收起。
3. AI 沟通建议页已接入真实接口，默认关闭时展示友好提示；本地可用 `--lovemaster.ai.enabled=true` 启用真实 DashScope 调用。
4. 喜好页已接入列表、新增、编辑、删除、分类、目标对象、可见性、标签，表单默认收起。
5. 约会灵感页已接入推荐/全部、场景、预算、耗时、兴趣标签筛选和详情弹窗。
6. 已做一轮移动端小屏视觉修正：隐藏横向筛选滚动条、放大卡片编辑/删除按钮、优化回忆卡片窄屏网格和图片区背景。

重要交互约定：

1. 首页、喜好、回忆等页面默认先展示内容，不直接露出编辑表单。
2. 点击右上角加号或页面内“记录...”入口后，再展开新增表单。
3. 保存成功后自动收起表单并刷新列表。

## 11. 页面与接口映射

| 页面 | 后端接口 | 状态 |
|---|---|---|
| 登录 | `/auth/login` | 前端已接入并验证 |
| 注册 | `/auth/register` | 前端已接入并验证 |
| 首页 | `/users/me`, `/pairings/me`, `/anniversaries` | 前端已接入 |
| 配对 | `/pairings/me`, `/pairings/bind` | 前端已接入，入口在我的页 |
| 喜好 | `/preferences` | 前端已接入新增/列表/分类/标签，待编辑删除 |
| 约会灵感 | `/date-ideas`, `/date-ideas/recommend` | 前端已接入基础列表，待补筛选和详情 |
| 回忆 | `/memories`, `/files/images` | 前端已接入上传/预览/新增/列表，待编辑删除 |
| 沟通建议 | `/ai/communication-advice` | 前端已接入，默认关闭提示已处理 |
| 我的 | `/users/me`, `/pairings/me`, `/auth/logout` | 前端已接入 |
| 任务 / 家务 | 无 | 先占位 |
| 共同目标 | 无 | 先占位 |

## 12. 设计实现注意事项

1. 不要做桌面后台风格，优先手机 App 体验。
2. 表单输入要大，按钮高度不小于 44px。
3. 不要让文字在小屏幕溢出。
4. 底部导航固定在底部，页面内容要给底部留出空间。
5. 空状态要友好，例如“还没有纪念日，先记录一个重要日子吧”。
6. 图片上传失败时要提示，不要静默失败。
7. AI 建议不要显示成“权威结论”，只显示为“参考建议”。
8. 不要在前端代码里写真实 AI API Key。

## 13. 验收命令

前端当前验证命令：

```powershell
cd C:\Users\91414\Desktop\project\lovemaster-web
$env:Path = 'D:\nodejs;' + $env:Path
npm run test
npm run build
```

最近一次验证结果：

```text
npm run test：5 个测试文件，12 个测试通过
npm run build：构建成功
```

后端 CORS 回归测试：

```powershell
cd C:\Users\91414\Desktop\project\lovemaster-server
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" test -Dtest=AuthControllerTest#testRegisterCorsPreflightShouldPass
```

## 14. 新对话可直接使用的提示词

新开对话后，可以复制下面这段：

```text
请继续 LoveMaster 移动端 H5 前端开发。

后端目录是 C:\Users\91414\Desktop\project\lovemaster-server。
前端目录是 C:\Users\91414\Desktop\project\lovemaster-web。
请先阅读：
1. docs/FRONTEND-PRD-LoveMaster-Mobile.md
2. docs/FRONTEND-HANDOFF.md
3. docs/HANDOFF.md
4. docs/images/产品原型图.png

当前前端已完成注册、登录、token、底部 Tab、首页、我的、配对、纪念日、喜好、约会灵感、回忆上传和 AI 沟通建议。
首页、喜好、回忆页面约定为默认展示，点击加号后展开新增表单。
请优先补齐编辑/删除能力、约会灵感筛选详情、统一反馈组件和小屏体验走查。
```

## 15. 关键提醒

1. 当前目录不是 git 仓库，不要自动提交。
2. 真实 API Key 不能写入前端或文档。
3. 后端 AI 默认关闭，前端必须处理 `AI服务未启用`。
4. 后端运行依赖 Docker MySQL 和 Redis。
5. 用户是移动端小白，解释时要用简单语言，不要一次塞太多术语。
6. Node 在 `D:\nodejs`，当前 shell 找不到 npm 时先临时追加 PATH。
7. 前端使用真实后端接口，不要做脱离后端的假数据页面。
8. 页面交互优先遵循“先展示，点加号再编辑”。
