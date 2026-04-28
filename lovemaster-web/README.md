# LoveMaster Mobile Web

LoveMaster 移动端 H5 前端，使用 React + Vite + TypeScript 开发，默认连接本地后端：

```text
http://localhost:8080/api/v1
```

## 本地启动

```powershell
npm install
npm run dev
```

## 验证

```powershell
npm run build
npm run test
```

如果后端地址不是默认值，可复制 `.env.example` 为 `.env` 并修改：

```text
VITE_API_BASE_URL=http://localhost:8080/api/v1
```
