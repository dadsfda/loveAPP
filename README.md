# 云成朝暮记 / LoveMaster

情侣关系管理工具，帮助情侣记录纪念日、偏好、约会灵感、共同回忆，并提供克制的 AI 沟通建议。

当前项目包含：

```text
lovemaster-server/  # Spring Boot 后端
lovemaster-web/     # React + Vite 移动端 H5 前端
```

## 当前状态

H5 MVP 已完成并上线验证：

1. 注册、登录、退出登录。
2. 邀请码配对和关系信息展示。
3. 首页、恋爱天数、纪念日新增 / 编辑 / 删除。
4. 喜好新增 / 编辑 / 删除。
5. 约会灵感列表、筛选和详情展示。
6. 回忆新增、图片上传、编辑、删除。
7. AI 沟通建议页，生产环境未启用时显示友好降级。
8. 我的页、伴侣信息和退出登录。

线上环境：

```text
CloudBase 环境 ID：lovemaster-d8gho9ywm7bf5ce52
H5 地址：https://lovemaster-d8gho9ywm7bf5ce52-1418276225.tcloudbaseapp.com/#/login
后端：CloudBase Run
数据库：CloudBase MySQL
```

## 本地启动

### 1. 启动后端依赖

需要本地 Docker。后端开发依赖 MySQL / Redis：

```powershell
cd lovemaster-server
docker compose up -d
```

### 2. 启动后端

```powershell
cd lovemaster-server
mvn spring-boot:run
```

后端默认地址：

```text
http://localhost:8080
```

### 3. 启动前端

如果当前终端找不到 npm，可以先把本机 Node 路径加入当前 PowerShell：

```powershell
$env:Path = 'D:\nodejs;' + $env:Path
```

启动前端：

```powershell
cd lovemaster-web
npm install
npm run dev
```

前端默认地址：

```text
http://localhost:5173
```

## 常用命令

后端测试：

```powershell
cd lovemaster-server
mvn test
```

前端测试：

```powershell
cd lovemaster-web
npm run test
```

前端构建：

```powershell
cd lovemaster-web
npm run build
```

## 配置说明

前端生产环境默认使用 CloudBase Run API，并使用 HashRouter 兼容静态托管刷新：

```text
/#/login
/#/register
```

后端图片存储支持两种模式：

```text
local  # 默认，本地开发使用
cos    # CloudBase Storage / Tencent COS，线上可配置启用
```

线上如需启用 COS，需要在 CloudRun 配置：

```text
LOVEMASTER_STORAGE_TYPE=cos
CLOUDBASE_STORAGE_REGION=<云存储地域>
CLOUDBASE_STORAGE_BUCKET=<存储桶名称>
CLOUDBASE_STORAGE_KEY_PREFIX=uploads/images
TENCENTCLOUD_SECRET_ID=<SecretId>
TENCENTCLOUD_SECRET_KEY=<SecretKey>
```

当前暂未配置线上 CloudBase Storage。

## 关键文档

```text
lovemaster-server/docs/PRD-LoveMaster.md
lovemaster-server/docs/FRONTEND-PRD-LoveMaster-Mobile.md
lovemaster-server/docs/FRONTEND-HANDOFF.md
lovemaster-server/docs/CLOUDBASE-DEPLOYMENT.md
```

## 下一步

当前不再优先补 CRUD，建议进入微信小程序上线前准备：

1. 确定小程序实现方式：原生小程序重写，还是 H5 / 云托管过渡。
2. 整理小程序页面清单和底部 Tab。
3. 确定登录方式。
4. 确认图片上传是否正式启用 CloudBase Storage。
5. 做小程序真机预览与发布前检查。
