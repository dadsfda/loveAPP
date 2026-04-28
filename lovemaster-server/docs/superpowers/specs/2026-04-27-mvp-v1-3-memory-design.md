# MVP v1.3 回忆收藏夹与图片上传设计

## 1. 背景

LoveMaster v1.1 已完成账号、配对、纪念日，v1.2 已完成喜好清单与约会灵感。v1.3 需要把“提醒”和“行动灵感”沉淀成共同回忆，让产品从事务型工具进一步形成情侣关系的时间线资产。

本阶段新增“回忆收藏夹”和“单图上传”。回忆用于记录一次约会、一个纪念日、一个日常瞬间或一段共同经历；图片上传用于支持回忆封面或配图。MVP 先采用本地文件存储，不接云对象存储。

## 2. 目标

1. 用户可以创建、查询、查看、更新、删除回忆。
2. 回忆支持个人可见和双方可见。
3. 回忆支持日期、地点、正文、标签、备注和单张图片 URL。
4. 用户可以上传一张图片，获得可公开访问的本地 URL。
5. 图片 URL 可保存到回忆记录中。
6. 权限模型复用现有 `PRIVATE` / `COUPLE` 规则。
7. OpenAPI 可以导出新接口，方便 Apifox 导入。

## 3. 非目标

1. 不做多图相册。
2. 不做图片裁剪、压缩、水印、审核。
3. 不接入 OSS、S3、COS 等云对象存储。
4. 不做评论、点赞、分享、公开动态。
5. 不做前端页面。
6. 不做 AI 回忆总结。

## 4. 用户故事

1. 作为用户，我可以上传一张约会照片，并拿到图片 URL。
2. 作为用户，我可以创建一条仅自己可见的回忆。
3. 作为已配对用户，我可以创建一条双方可见的回忆。
4. 作为伴侣，我可以看到对方创建的双方可见回忆。
5. 作为伴侣，我不能编辑或删除对方创建的回忆。
6. 作为用户，我可以按日期范围、可见性和标签筛选回忆时间线。

## 5. 数据模型

新增 `memory` 表：

| 字段 | 类型 | 说明 |
|---|---|---|
| `id` | BIGINT | 主键 |
| `creator_id` | BIGINT | 创建者用户 ID |
| `couple_id` | BIGINT | 情侣关系 ID，个人可见时为空 |
| `title` | VARCHAR(80) | 标题 |
| `memory_date` | DATE | 回忆发生日期 |
| `location` | VARCHAR(120) | 地点文本 |
| `content` | VARCHAR(2000) | 正文 |
| `image_url` | VARCHAR(500) | 图片 URL |
| `visibility` | VARCHAR(20) | `PRIVATE` / `COUPLE` |
| `tags` | VARCHAR(255) | 标签，逗号分隔 |
| `remark` | VARCHAR(500) | 备注 |
| `created_at` | DATETIME | 创建时间 |
| `updated_at` | DATETIME | 更新时间 |
| `deleted` | TINYINT | 逻辑删除 |

索引：

- `idx_memory_creator_id`
- `idx_memory_couple_id`
- `idx_memory_visibility`
- `idx_memory_memory_date`

## 6. 文件存储设计

新增本地上传目录：

```text
uploads/images/
```

配置项建议：

```yaml
lovemaster:
  upload:
    image-dir: uploads/images
    image-url-prefix: /uploads/images
    max-image-size: 5242880
```

上传规则：

1. 只允许 `image/jpeg`、`image/png`、`image/webp`、`image/gif`。
2. 文件大小不得超过 5MB。
3. 文件名由服务端生成，格式建议：`yyyyMMddHHmmssSSS_uuid.ext`。
4. 返回相对 URL，例如 `/uploads/images/20260427223000123_abcd.jpg`。
5. Spring MVC 配置静态资源映射，让 `/uploads/**` 可访问。
6. JWT 鉴权：上传接口需要登录；图片静态访问路径可以匿名访问。

## 7. API 设计

### 7.1 上传图片

```text
POST /api/v1/files/images
Content-Type: multipart/form-data
Authorization: Bearer <accessToken>
```

请求字段：

- `file`：图片文件。

成功响应：

```json
{
  "code": 200,
  "message": "上传成功",
  "data": {
    "url": "/uploads/images/20260427223000123_abcd.jpg",
    "filename": "20260427223000123_abcd.jpg",
    "contentType": "image/jpeg",
    "size": 123456
  }
}
```

### 7.2 创建回忆

```text
POST /api/v1/memories
```

请求：

```json
{
  "title": "傍晚散步",
  "memoryDate": "2026-05-20",
  "location": "江边公园",
  "content": "今天一起散步拍了很多照片。",
  "imageUrl": "/uploads/images/20260427223000123_abcd.jpg",
  "visibility": "PRIVATE",
  "tags": ["散步", "拍照"],
  "remark": "测试回忆"
}
```

规则：

1. `title` 必填，1 到 80 字。
2. `memoryDate` 必填。
3. `content` 选填，最多 2000 字。
4. `imageUrl` 选填，最多 500 字。
5. `visibility` 必填，只允许 `PRIVATE` / `COUPLE`。
6. `COUPLE` 必须已配对。

### 7.3 查询回忆列表

```text
GET /api/v1/memories?visibility=PRIVATE&startDate=2026-01-01&endDate=2026-12-31&tag=散步
```

筛选：

- `visibility`：可选。
- `startDate`：可选。
- `endDate`：可选。
- `tag`：可选，单标签筛选。

排序：

1. `memoryDate` 倒序。
2. `createdAt` 倒序。
3. `id` 倒序。

### 7.4 回忆详情

```text
GET /api/v1/memories/{id}
```

### 7.5 更新回忆

```text
PUT /api/v1/memories/{id}
```

规则：

1. 只有创建者可更新。
2. 更新可见性时重新校验配对关系。
3. 从 `COUPLE` 改为 `PRIVATE` 时必须清空 `couple_id`。

### 7.6 删除回忆

```text
DELETE /api/v1/memories/{id}
```

规则：

1. 只有创建者可删除。
2. 使用逻辑删除。

## 8. 权限规则

读取权限：

1. 创建者可以读取自己创建的所有回忆。
2. 当前有效配对伴侣可以读取 `COUPLE` 回忆。
3. 其他用户不可读取。
4. 解除配对后，伴侣不再读取历史 `COUPLE` 回忆；创建者仍可读取和管理。

写入权限：

1. 创建者可更新、删除。
2. 伴侣只读，不可更新、删除。
3. 未配对用户不能创建或更新为 `COUPLE`。

## 9. 后端组件

新增文件：

- `entity/Memory.java`
- `mapper/MemoryMapper.java`
- `dto/request/CreateMemoryRequest.java`
- `dto/request/UpdateMemoryRequest.java`
- `dto/response/MemoryResponse.java`
- `dto/response/ImageUploadResponse.java`
- `service/MemoryService.java`
- `service/impl/MemoryServiceImpl.java`
- `service/FileStorageService.java`
- `service/impl/LocalFileStorageServiceImpl.java`
- `controller/MemoryController.java`
- `controller/FileController.java`
- `config/WebMvcConfig.java`

修改文件：

- `src/main/resources/db/schema.sql`
- `src/test/resources/schema.sql`
- `src/main/resources/application.yml` 或 `application-dev.yml`
- `SecurityConfig.java`
- `ErrorCode.java`
- `OpenApiControllerTest.java`

## 10. 错误码

建议新增：

- `MEMORY_NOT_FOUND(1601, "回忆不存在")`
- `MEMORY_ACCESS_DENIED(1602, "无权访问该回忆")`
- `MEMORY_VISIBILITY_INVALID(1603, "回忆可见性不合法")`
- `FILE_EMPTY(1701, "上传文件不能为空")`
- `FILE_TYPE_NOT_ALLOWED(1702, "不支持的文件类型")`
- `FILE_SIZE_EXCEEDED(1703, "上传文件大小超出限制")`
- `FILE_SAVE_FAILED(1704, "文件保存失败")`

## 11. 测试策略

Service 测试：

1. 未配对用户可以创建个人回忆。
2. 未配对用户不能创建双方可见回忆。
3. 配对用户可以创建双方可见回忆。
4. 伴侣可以查看双方可见回忆，不能查看个人回忆。
5. 只有创建者可以更新和删除回忆。
6. 列表按 `memoryDate` 倒序返回。

Controller 测试：

1. 回忆 CRUD 接口响应格式正确。
2. 图片上传成功返回 URL。
3. 空文件返回 400。
4. 非图片类型返回业务错误。

OpenAPI 测试：

1. `/v3/api-docs` 包含 `/api/v1/memories`。
2. `/v3/api-docs` 包含 `/api/v1/files/images`。

Docker E2E：

1. 注册并登录。
2. 上传一张测试图片。
3. 用返回的 `url` 创建回忆。
4. 查询回忆列表和详情。

## 12. 验收标准

1. 用户可以上传图片并获得 URL。
2. 用户可以创建带图片 URL 的个人回忆。
3. 已配对用户可以创建双方可见回忆。
4. 未配对用户创建双方可见回忆失败。
5. 伴侣可以查看双方可见回忆。
6. 伴侣不能编辑或删除对方回忆。
7. 个人回忆不被伴侣看到。
8. 回忆列表按时间线倒序。
9. OpenAPI 可以导入新接口。
10. 全量测试通过。

## 13. 后续演进

1. 多图上传和相册分组。
2. 图片压缩、缩略图、EXIF 清理。
3. 对接对象存储。
4. 回忆关联纪念日或约会灵感。
5. 基于回忆生成年度总结。
