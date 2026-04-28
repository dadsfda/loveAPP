# AGENTS.md

This file provides guidance to Codex (Codex.ai/code) when working with code in this repository.

## Project Overview

LoveMaster 是一个情侣关系管理平台，定位为"关系增效工具"，帮助情侣减少日常琐碎矛盾、增加情感连接仪式感。

**项目状态**: MVP v1.0 开发阶段

## Technology Stack

- **后端**: Spring Boot 3.2.0 + Java 17
- **安全**: Spring Security + JWT (jjwt 0.12.3)
- **ORM**: MyBatis-Plus 3.5.5
- **数据库**: MySQL 8.0
- **缓存**: Redis
- **测试**: JUnit 5 + H2 (内存数据库)

## Development Commands

### Build and Run

```bash
# 编译项目
./mvnw clean compile

# 运行开发环境（需要本地 MySQL 和 Redis）
./mvnw spring-boot:run

# 打包
./mvnw clean package

# 跳过测试打包
./mvnw clean package -DskipTests
```

### Testing

```bash
# 运行所有测试
./mvnw test

# 运行单个测试类
./mvnw test -Dtest=AuthServiceTest

# 运行单个测试方法
./mvnw test -Dtest=AuthServiceTest#testRegisterSuccess
```

### Database

```bash
# 启动本地 MySQL (Docker)
docker run -d -p 3306:3306 -e MYSQL_ROOT_PASSWORD=123456 -e MYSQL_DATABASE=lovemaster mysql:8.0

# 启动本地 Redis (Docker)
docker run -d -p 6379:6379 redis:latest

# 初始化数据库表结构
# 执行 src/main/resources/db/schema.sql
```

## Architecture

### System Design

```
React SPA (前端) ◄──► Spring Boot REST API ◄──► MySQL
                          │
                          ▼
                       Redis (Session/Cache)
```

### Layer Structure

```
controller/    # REST API 接口层
├── AuthController          # /api/v1/auth/* 认证相关

service/       # 业务逻辑层
├── AuthService             # 认证业务
├── UserService             # 用户业务
└── impl/                   # 实现类

entity/        # 数据实体
├── User                    # 用户实体

dto/           # 数据传输对象
├── request/                # 请求 DTO
├── response/               # 响应 DTO
└── ApiResponse<T>          # 统一响应包装类

mapper/        # MyBatis-Plus 数据访问层
├── UserMapper

security/      # 安全相关
├── JwtFilter               # JWT 认证过滤器
├── JwtUtil                 # JWT 工具类
└── UserDetailsServiceImpl  # 用户详情服务

config/        # 配置类
├── SecurityConfig          # Spring Security 配置
├── RedisConfig             # Redis 配置
└── MyBatisConfig           # MyBatis-Plus 配置

exception/     # 异常处理
├── BusinessException       # 业务异常
├── ErrorCode               # 错误码定义
└── GlobalExceptionHandler  # 全局异常处理器
```

### Key Architectural Decisions

1. **认证机制**: JWT Token (Access Token 2小时 / Refresh Token 7天)，存储在 Redis 中支持登出
2. **配对机制**: 用户通过 8 位邀请码 (`pair_code`) 完成双向配对，配对后 `partner_id` 互相引用
3. **API 规范**: 统一响应格式 `ApiResponse<T>`，路径前缀 `/api/v1`
4. **数据隔离**: Service 层校验数据权限，未配对用户只能访问个人数据
5. **逻辑删除**: 所有实体使用 MyBatis-Plus 逻辑删除 (`deleted` 字段)

### Security Configuration

- `/api/v1/auth/**` 路径无需认证
- 其他所有请求需要 JWT 认证
- 使用 BCrypt 进行密码加密
- 无状态会话管理 (STATELESS)

## Configuration

### Profiles

- `application.yml` - 主配置（默认激活 dev）
- `application-dev.yml` - 开发环境（连接本地 MySQL/Redis）
- `application-test.yml` - 测试环境（使用 H2 内存数据库）

### Key Properties

```yaml
# JWT 配置
jwt:
  secret: lovemaster-jwt-secret-key-must-be-at-least-256-bits-long-for-hs256
  access-token-expiration: 7200000    # 2小时
  refresh-token-expiration: 604800000 # 7天
```

## API Endpoints

| 模块 | 接口 | 说明 |
|------|------|------|
| Auth | POST `/api/v1/auth/register` | 用户注册 |
| Auth | POST `/api/v1/auth/login` | 用户登录 |
| Auth | POST `/api/v1/auth/refresh` | 刷新 Token |
| Auth | POST `/api/v1/auth/logout` | 退出登录 |

## Testing

- 测试类使用 `@ActiveProfiles("test")` 激活测试配置
- 使用 `@Transactional` 确保测试数据回滚
- 测试数据库使用 H2，与生产 MySQL 隔离
