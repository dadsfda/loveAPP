# LoveMaster / loveAPP

LoveMaster 是一个情侣关系管理平台，包含 Spring Boot 后端和移动端 H5 前端。

## 目录

```text
lovemaster-server/  # Spring Boot 后端
lovemaster-web/     # React + Vite 移动端 H5 前端
```

## 后端启动

```powershell
cd lovemaster-server
docker compose up -d
& "C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd" spring-boot:run
```

后端地址：

```text
http://localhost:8080
```

## 前端启动

如果当前终端找不到 npm，先临时加入 Node 路径：

```powershell
$env:Path = 'D:\nodejs;' + $env:Path
```

启动前端：

```powershell
cd lovemaster-web
npm install
npm run dev
```

前端地址：

```text
http://localhost:5173
```

## 当前状态

移动端 H5 已完成注册、登录、首页、配对、纪念日、喜好、约会灵感、回忆上传、AI 沟通建议和我的页的第一轮真实接口联调。
