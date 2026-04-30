# CloudBase 部署准备说明

本文记录 LoveMaster / 云成朝暮记部署到 CloudBase 的推荐路径。

## 推荐架构

```text
CloudBase 静态托管：lovemaster-web/dist
        |
        v
CloudBase 云托管 CloudRun：lovemaster-server Spring Boot
        |
        +-- 云上 MySQL
        +-- CloudBase 云存储 / COS
        +-- DashScope / 通义千问
```

当前项目仍是 H5 + Spring Boot 架构，不是原生微信小程序。建议先部署 H5 在线版，确认主流程跑通后，再做小程序端适配。

## 后端云托管

后端目录：

```powershell
C:\Users\91414\Desktop\project\loveAPP\lovemaster-server
```

已新增：

```text
Dockerfile
.dockerignore
src/main/resources/application-prod.yml
```

CloudRun 部署时建议选择容器模式，运行时通过环境变量启用生产配置：

```text
SPRING_PROFILES_ACTIVE=prod
```

当前 CloudBase 环境 `lovemaster-d8gho9ywm7bf5ce52` 的 MySQL 已创建并初始化；CloudRun 后端已完成部署并验证接口可用。后续重新部署时，需要保持 VPC、MySQL、CORS、JWT 和图片存储环境变量配置一致。

CloudBase / CloudRun 环境变量建议：

```text
SPRING_PROFILES_ACTIVE=prod
MYSQL_HOST=<云上 MySQL 地址>
MYSQL_PORT=3306
MYSQL_DATABASE=lovemaster
MYSQL_USERNAME=<MySQL 用户名>
MYSQL_PASSWORD=<MySQL 密码>
JWT_SECRET=<至少 32 字节的随机密钥>
LOVEMASTER_CORS_ALLOWED_ORIGINS=<前端静态托管域名，例如 https://xxx.tcloudbaseapp.com>
LOVEMASTER_AI_ENABLED=true
DASHSCOPE_API_KEY=<通义千问 DashScope API Key>
LOVEMASTER_STORAGE_TYPE=cos
CLOUDBASE_STORAGE_REGION=<云存储地域，例如 ap-guangzhou>
CLOUDBASE_STORAGE_BUCKET=<存储桶名称，例如 xxx-1250000000>
CLOUDBASE_STORAGE_KEY_PREFIX=uploads/images
CLOUDBASE_STORAGE_PUBLIC_BASE_URL=<可选，自定义 CDN/访问域名>
TENCENTCLOUD_SECRET_ID=<腾讯云 SecretId>
TENCENTCLOUD_SECRET_KEY=<腾讯云 SecretKey>
```

也可以直接使用完整 JDBC URL：

```text
SPRING_DATASOURCE_URL=jdbc:mysql://<host>:3306/lovemaster?useUnicode=true&characterEncoding=utf-8&serverTimezone=Asia/Shanghai
```

注意：后端已支持 `local` 和 `cos` 两种图片存储。开发环境默认 `local`，线上建议设置 `LOVEMASTER_STORAGE_TYPE=cos`，否则图片仍会写入 CloudRun 本地目录，不适合长期保存。

## 前端静态托管

前端目录：

```powershell
C:\Users\91414\Desktop\project\loveAPP\lovemaster-web
```

已新增示例：

```text
.env.production.example
```

部署前复制并填写：

```powershell
Copy-Item .env.production.example .env.production
```

内容示例：

```text
VITE_API_BASE_URL=https://<后端云托管域名>/api/v1
```

构建：

```powershell
$env:Path = 'D:\nodejs;' + $env:Path
npm run build
```

将 `dist` 目录部署到 CloudBase 静态托管。

## 数据库初始化

云上 MySQL 创建后，需要执行：

```text
src/main/resources/db/schema.sql
```

该脚本会创建用户、配对、纪念日、喜好、约会灵感、回忆等表，并初始化约会灵感模板。

认证的 Refresh Token 已存储在 MySQL 的 `refresh_token` 表中，后端不再依赖 Redis。

## 部署顺序

1. 创建 / 准备云上 MySQL。
2. 执行 `schema.sql` 初始化 MySQL。
3. 部署后端 CloudRun，并配置环境变量。
4. 获取后端访问域名。
5. 设置前端 `.env.production` 的 `VITE_API_BASE_URL`。
6. 构建并部署前端静态托管。
7. 把前端静态托管域名写入后端 `LOVEMASTER_CORS_ALLOWED_ORIGINS`。
8. 验证注册、登录、配对、纪念日、喜好、回忆、AI 沟通建议。

## CloudBase MCP

管理 CloudBase 资源前，先检查 MCP：

```powershell
npx mcporter describe cloudbase --all-parameters
npx mcporter call cloudbase.auth action=status --output json
```

如果未登录，使用设备码登录：

```powershell
npx mcporter call cloudbase.auth action=start_auth authMode=device --output json
```

绑定环境时必须使用完整 `EnvId`，不要使用简称。
