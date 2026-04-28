# MVP v1.2 Preference And Date Ideas Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 LoveMaster MVP v1.2 的喜好清单、约会灵感模板、规则推荐、OpenAPI 注解完善和错误响应优化。

**Architecture:** 沿用当前 Spring Boot 分层结构：Controller 负责 REST 入参和 `ApiResponse` 包装，Service 负责业务规则、权限校验和推荐规则，Mapper 使用 MyBatis-Plus 访问 MySQL/H2。新增 `preference` 承载个人/双方可见偏好，新增 `date_idea` 承载内置约会灵感模板。

**Tech Stack:** Java 17、Spring Boot 3.2.0、Spring Security、JWT、MyBatis-Plus 3.5.5、MySQL 8.0、Redis、JUnit 5、H2、springdoc-openapi 2.3.0。

---

## File Structure

### Existing Files To Modify

- `src/main/resources/db/schema.sql`：新增 `preference`、`date_idea` 表和内置模板数据。
- `src/test/resources/schema.sql`：新增 H2 兼容测试表和模板数据。
- `src/main/java/com/lovemaster/exception/ErrorCode.java`：新增偏好和约会灵感错误码。
- `src/main/java/com/lovemaster/exception/GlobalExceptionHandler.java`：优化 JSON 解析错误为 400 响应。
- `src/main/java/com/lovemaster/config/OpenApiConfig.java`：保持 Bearer 认证声明，必要时补充分组说明。
- `src/main/java/com/lovemaster/controller/*.java`：补充 `@Operation`、`@Tag` 等 OpenAPI 注解。
- `src/main/java/com/lovemaster/dto/**/*.java`：补充 `@Schema` 字段说明。

### New Domain Files

- `src/main/java/com/lovemaster/entity/Preference.java`：喜好清单实体。
- `src/main/java/com/lovemaster/entity/DateIdea.java`：约会灵感模板实体。
- `src/main/java/com/lovemaster/mapper/PreferenceMapper.java`：喜好清单 Mapper。
- `src/main/java/com/lovemaster/mapper/DateIdeaMapper.java`：约会灵感 Mapper。

### New DTO Files

- `src/main/java/com/lovemaster/dto/request/CreatePreferenceRequest.java`
- `src/main/java/com/lovemaster/dto/request/UpdatePreferenceRequest.java`
- `src/main/java/com/lovemaster/dto/request/DateIdeaQueryRequest.java`
- `src/main/java/com/lovemaster/dto/response/PreferenceResponse.java`
- `src/main/java/com/lovemaster/dto/response/DateIdeaResponse.java`

### New Service Files

- `src/main/java/com/lovemaster/service/PreferenceService.java`
- `src/main/java/com/lovemaster/service/DateIdeaService.java`
- `src/main/java/com/lovemaster/service/impl/PreferenceServiceImpl.java`
- `src/main/java/com/lovemaster/service/impl/DateIdeaServiceImpl.java`

### New Controller Files

- `src/main/java/com/lovemaster/controller/PreferenceController.java`
- `src/main/java/com/lovemaster/controller/DateIdeaController.java`

### New Test Files

- `src/test/java/com/lovemaster/service/PreferenceServiceTest.java`
- `src/test/java/com/lovemaster/service/DateIdeaServiceTest.java`
- `src/test/java/com/lovemaster/controller/PreferenceControllerTest.java`
- `src/test/java/com/lovemaster/controller/DateIdeaControllerTest.java`
- `src/test/java/com/lovemaster/controller/GlobalExceptionHandlerTest.java`

---

## Shared Constants And Decisions

- 偏好可见性：`PRIVATE`、`COUPLE`，沿用纪念日可见性语义。
- 偏好目标：`SELF`、`PARTNER_OBSERVED`。
- 偏好分类：`FOOD_TABOO`、`FAVORITE_FOOD`、`HOBBY`、`GIFT`、`LIFE_BOUNDARY`、`CUSTOM`。
- 约会预算：`FREE`、`UNDER_50`、`UNDER_100`、`UNDER_300`、`UNLIMITED`。
- 约会耗时：`ONE_HOUR`、`TWO_HOURS`、`HALF_DAY`、`ONE_DAY`。
- 约会场景：`INDOOR`、`OUTDOOR`、`RAINY_DAY`、`HOME`、`REMOTE`。
- 标签字段 MVP 使用逗号字符串存储，对外使用 `List<String>`。
- MVP v1.2 不接入 AI，不接外部天气/地图/商户数据。
- 未经用户明确确认，不执行 `git commit`。

---

### Task 1: Database Schema For Preference And DateIdea

**Files:**
- Modify: `src/main/resources/db/schema.sql`
- Modify: `src/test/resources/schema.sql`
- Test: `src/test/java/com/lovemaster/service/PreferenceServiceTest.java`

- [ ] **Step 1: Add failing schema-aware test**

Create `PreferenceServiceTest` with a baseline test referencing the future service:

```java
package com.lovemaster.service;

import com.lovemaster.dto.request.CreatePreferenceRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.PreferenceResponse;
import com.lovemaster.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PreferenceServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private PreferenceService preferenceService;

    @Test
    @DisplayName("用户可以创建个人可见喜好记录")
    void createPrivatePreferenceShouldSucceed() {
        User user = register("pref_user_private");
        CreatePreferenceRequest request = new CreatePreferenceRequest();
        request.setTarget("SELF");
        request.setCategory("HOBBY");
        request.setContent("喜欢散步和拍照");
        request.setVisibility("PRIVATE");
        request.setTags(List.of("散步", "拍照"));
        request.setRemark("周末更喜欢户外");

        PreferenceResponse response = preferenceService.create(user.getId(), request);

        assertNotNull(response.getId());
        assertEquals("HOBBY", response.getCategory());
        assertEquals("PRIVATE", response.getVisibility());
        assertEquals(List.of("散步", "拍照"), response.getTags());
    }

    private User register(String username) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("password123");
        authService.register(request);
        return userService.findByUsername(username);
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PreferenceServiceTest
```

Expected: compilation failure because `PreferenceService` and DTOs do not exist.

- [ ] **Step 3: Add MySQL schema**

Append `preference` and `date_idea` tables to `src/main/resources/db/schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS `preference` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '喜好记录ID',
    `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `couple_id` BIGINT DEFAULT NULL COMMENT '情侣关系ID',
    `target` VARCHAR(30) NOT NULL COMMENT '记录对象：SELF/PARTNER_OBSERVED',
    `category` VARCHAR(30) NOT NULL COMMENT '分类',
    `content` VARCHAR(500) NOT NULL COMMENT '内容',
    `visibility` VARCHAR(20) NOT NULL COMMENT '可见性：PRIVATE/COUPLE',
    `tags` VARCHAR(255) DEFAULT NULL COMMENT '标签，逗号分隔',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_preference_creator_id` (`creator_id`),
    KEY `idx_preference_couple_id` (`couple_id`),
    KEY `idx_preference_visibility` (`visibility`),
    KEY `idx_preference_category` (`category`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='喜好清单表';

CREATE TABLE IF NOT EXISTS `date_idea` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '约会灵感ID',
    `title` VARCHAR(80) NOT NULL COMMENT '标题',
    `budget_level` VARCHAR(30) NOT NULL COMMENT '预算档位',
    `duration_level` VARCHAR(30) NOT NULL COMMENT '耗时档位',
    `scene` VARCHAR(30) NOT NULL COMMENT '场景',
    `interest_tags` VARCHAR(255) NOT NULL COMMENT '兴趣标签，逗号分隔',
    `steps` VARCHAR(1000) NOT NULL COMMENT '执行步骤',
    `tips` VARCHAR(500) DEFAULT NULL COMMENT '小提示',
    `enabled` TINYINT NOT NULL DEFAULT 1 COMMENT '是否启用',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_date_idea_budget` (`budget_level`),
    KEY `idx_date_idea_duration` (`duration_level`),
    KEY `idx_date_idea_scene` (`scene`),
    KEY `idx_date_idea_enabled` (`enabled`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='约会灵感模板表';
```

- [ ] **Step 4: Add H2 schema**

Append H2-compatible versions to `src/test/resources/schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS preference (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    couple_id BIGINT,
    target VARCHAR(30) NOT NULL,
    category VARCHAR(30) NOT NULL,
    content VARCHAR(500) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    tags VARCHAR(255),
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_preference_creator_id ON preference(creator_id);
CREATE INDEX IF NOT EXISTS idx_preference_couple_id ON preference(couple_id);
CREATE INDEX IF NOT EXISTS idx_preference_visibility ON preference(visibility);
CREATE INDEX IF NOT EXISTS idx_preference_category ON preference(category);

CREATE TABLE IF NOT EXISTS date_idea (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    title VARCHAR(80) NOT NULL,
    budget_level VARCHAR(30) NOT NULL,
    duration_level VARCHAR(30) NOT NULL,
    scene VARCHAR(30) NOT NULL,
    interest_tags VARCHAR(255) NOT NULL,
    steps VARCHAR(1000) NOT NULL,
    tips VARCHAR(500),
    enabled TINYINT DEFAULT 1 NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_date_idea_budget ON date_idea(budget_level);
CREATE INDEX IF NOT EXISTS idx_date_idea_duration ON date_idea(duration_level);
CREATE INDEX IF NOT EXISTS idx_date_idea_scene ON date_idea(scene);
CREATE INDEX IF NOT EXISTS idx_date_idea_enabled ON date_idea(enabled);
```

- [ ] **Step 5: Seed date ideas**

Add 20 initial records to MySQL schema. Use stable IDs so repeated schema application stays deterministic:

```sql
DELETE FROM `date_idea` WHERE `id` BETWEEN 1 AND 20;

INSERT INTO `date_idea`
(`id`, `title`, `budget_level`, `duration_level`, `scene`, `interest_tags`, `steps`, `tips`, `enabled`)
VALUES
(1, '傍晚散步拍照', 'FREE', 'ONE_HOUR', 'OUTDOOR', '散步,拍照', '选一条熟悉路线;各自拍三张喜欢的画面;结束后互相选一张收藏', '适合低成本恢复连接感', 1),
(2, '居家电影夜', 'UNDER_50', 'TWO_HOURS', 'HOME', '电影,零食', '提前选两部候选电影;准备饮料零食;看完互相说一个喜欢的片段', '不要临时争选片，先定候选池', 1),
(3, '雨天咖啡聊天', 'UNDER_100', 'TWO_HOURS', 'RAINY_DAY', '咖啡,聊天', '选择安静咖啡店;各自带一个近况话题;最后一起定下周小计划', '适合不想奔波的雨天', 1),
(4, '公园野餐', 'UNDER_100', 'HALF_DAY', 'OUTDOOR', '美食,散步,拍照', '准备简单食物;找一片草地;饭后散步拍照', '注意天气和垃圾带走', 1),
(5, '一起做晚饭', 'UNDER_100', 'TWO_HOURS', 'HOME', '美食,手作', '一起选一道菜;分工采购和处理;吃饭时互夸一个环节', '避免复杂菜式，优先成功率', 1),
(6, '书店交换推荐', 'UNDER_50', 'ONE_HOUR', 'INDOOR', '阅读,聊天', '各自挑一本想推荐给对方的书;说明推荐理由;拍下书名以后再读', '不一定要购买', 1),
(7, '手作小物', 'UNDER_100', 'HALF_DAY', 'INDOOR', '手作,礼物', '选择简单手作材料;一起完成;写下赠送小卡片', '成品不完美也可以保留为回忆', 1),
(8, '城市漫游', 'UNDER_50', 'HALF_DAY', 'OUTDOOR', '散步,拍照,探索', '选一个没认真逛过的街区;随机进一家小店;记录三个新发现', '适合周末半天', 1),
(9, '线上同步观影', 'UNDER_50', 'TWO_HOURS', 'REMOTE', '电影,异地', '约定开始时间;语音同步观看;结束后分享三句话感受', '异地时保持轻量陪伴', 1),
(10, '一起运动拉伸', 'FREE', 'ONE_HOUR', 'HOME', '运动,健康', '选 20 分钟低强度视频;一起完成;结束后喝水休息', '不要互相评价动作标准', 1),
(11, '博物馆半日', 'UNDER_100', 'HALF_DAY', 'INDOOR', '展览,学习,拍照', '提前预约;各自选一个最喜欢展品;结束后吃简餐', '适合安静型约会', 1),
(12, '夜市小吃挑战', 'UNDER_100', 'TWO_HOURS', 'OUTDOOR', '美食,探索', '每人选两样小吃;共同打分;最后选今晚第一名', '注意饮食禁忌', 1),
(13, '共同歌单整理', 'FREE', 'ONE_HOUR', 'HOME', '音乐,回忆', '各自选五首歌;讲一个相关回忆;生成共同歌单', '适合睡前轻互动', 1),
(14, '拼图或桌游夜', 'UNDER_100', 'TWO_HOURS', 'HOME', '桌游,陪伴', '选择轻规则游戏;准备饮料;玩后复盘最好笑瞬间', '避免胜负压力太强的游戏', 1),
(15, '短途骑行', 'UNDER_50', 'HALF_DAY', 'OUTDOOR', '运动,探索', '选安全路线;中途休息拍照;结束后补水', '安全优先，天气不好就取消', 1),
(16, '共同整理相册', 'FREE', 'ONE_HOUR', 'HOME', '回忆,拍照', '各自选十张照片;按时间排序;挑三张做纪念日素材', '适合为回忆功能预热', 1),
(17, '早餐约会', 'UNDER_50', 'ONE_HOUR', 'OUTDOOR', '美食,日常', '约一家早餐店;吃完散步十分钟;互相说当天期待', '适合工作日前的小仪式', 1),
(18, '学习陪伴局', 'FREE', 'TWO_HOURS', 'INDOOR', '学习,陪伴', '各自定一个学习目标;番茄钟 2 轮;结束后分享成果', '适合需要自律但想陪伴时', 1),
(19, '礼物灵感清单', 'FREE', 'ONE_HOUR', 'HOME', '礼物,聊天', '各自写五个想收到的小东西;标预算;保存到喜好清单', '不要变成索要礼物', 1),
(20, '异地明信片计划', 'UNDER_50', 'TWO_HOURS', 'REMOTE', '异地,礼物,文字', '各自写一段想说的话;约定寄出日期;收到后拍照记录', '适合异地情侣制造期待', 1);
```

Add H2 seed data with the same IDs using:

```sql
DELETE FROM date_idea WHERE id BETWEEN 1 AND 20;
INSERT INTO date_idea (id, title, budget_level, duration_level, scene, interest_tags, steps, tips, enabled)
VALUES (1, '傍晚散步拍照', 'FREE', 'ONE_HOUR', 'OUTDOOR', '散步,拍照', '选一条熟悉路线;各自拍三张喜欢的画面;结束后互相选一张收藏', '适合低成本恢复连接感', 1);
```

Then repeat IDs 2-20 with the same values as the MySQL insert block.

- [ ] **Step 6: Run all tests**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test
```

Expected: existing tests keep passing; `PreferenceServiceTest` still fails only because implementation does not exist.

---

### Task 2: Preference Domain And Service

**Files:**
- Create: `Preference.java`, `PreferenceMapper.java`
- Create: preference request/response DTOs
- Create: `PreferenceService.java`, `PreferenceServiceImpl.java`
- Modify: `ErrorCode.java`
- Test: `PreferenceServiceTest.java`

- [ ] **Step 1: Expand service tests**

Add tests for:

- 未配对用户不能创建双方可见偏好。
- 配对后伴侣可以查看双方可见偏好。
- 伴侣不能编辑或删除对方创建的偏好。
- 非关系内用户无法查看偏好。

- [ ] **Step 2: Run test to verify failures**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PreferenceServiceTest
```

Expected: compilation or unsupported-method failures.

- [ ] **Step 3: Implement entity, mapper, DTOs and service**

Implementation must mirror `AnniversaryServiceImpl` permission style:

- `PRIVATE` records require `creator_id == currentUserId`.
- `COUPLE` records require active `couple_id`.
- Only creator can update/delete.
- List returns own private records plus active couple visible records.
- Tags are formatted as sorted distinct comma strings and returned as `List<String>`.

- [ ] **Step 4: Run preference tests**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PreferenceServiceTest
```

Expected: PASS.

---

### Task 3: Preference Controller

**Files:**
- Create: `PreferenceController.java`
- Test: `PreferenceControllerTest.java`

- [ ] **Step 1: Write MockMvc tests**

Cover:

- `POST /api/v1/preferences`
- `GET /api/v1/preferences`
- `GET /api/v1/preferences/{id}`
- `PUT /api/v1/preferences/{id}`
- `DELETE /api/v1/preferences/{id}`

- [ ] **Step 2: Run test to verify failure**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PreferenceControllerTest
```

Expected: compile failure because controller does not exist.

- [ ] **Step 3: Implement controller**

Follow `AnniversaryController` style:

- `@RestController`
- `@RequestMapping("/api/v1/preferences")`
- `@AuthenticationPrincipal User user`
- `ApiResponse<T>` wrapping
- `@Valid @RequestBody` for create/update

- [ ] **Step 4: Run controller tests**

Expected: PASS.

---

### Task 4: Date Idea Domain, Query And Recommendation

**Files:**
- Create: `DateIdea.java`, `DateIdeaMapper.java`
- Create: date idea DTOs
- Create: `DateIdeaService.java`, `DateIdeaServiceImpl.java`
- Test: `DateIdeaServiceTest.java`

- [ ] **Step 1: Write failing service tests**

Cover:

- 查询启用的约会灵感模板。
- 按预算、耗时、场景筛选。
- 按兴趣标签筛选。
- 推荐接口优先匹配用户偏好标签。

- [ ] **Step 2: Run test to verify failure**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=DateIdeaServiceTest
```

Expected: compilation failure because date idea service does not exist.

- [ ] **Step 3: Implement query service**

Rules:

- Always filter `enabled=1`.
- Optional filters: `budgetLevel`、`durationLevel`、`scene`、`interestTags`。
- Interest tag matching can be done in Java after DB query for MVP.
- Sort exact condition matches before partial matches.

- [ ] **Step 4: Implement recommendation service**

Rules:

- Load visible preference tags for current user.
- If request contains interest tags, merge them with preference tags.
- Return best matched enabled templates.
- If no preference tags exist, fall back to plain filter query.

- [ ] **Step 5: Run service tests**

Expected: PASS.

---

### Task 5: Date Idea Controller

**Files:**
- Create: `DateIdeaController.java`
- Test: `DateIdeaControllerTest.java`

- [ ] **Step 1: Write MockMvc tests**

Cover:

- `GET /api/v1/date-ideas`
- `GET /api/v1/date-ideas/recommendations`
- Auth required for both endpoints.

- [ ] **Step 2: Implement controller**

Use query parameters:

- `budgetLevel`
- `durationLevel`
- `scene`
- `interestTags`

For `interestTags`, accept repeated params or comma-separated string, then normalize in service.

- [ ] **Step 3: Run controller tests**

Expected: PASS.

---

### Task 6: OpenAPI Annotation Polish

**Files:**
- Modify: all controller classes
- Modify: request/response DTO classes
- Test: `OpenApiControllerTest.java`

- [ ] **Step 1: Expand OpenAPI test**

Assert:

- API title is `LoveMaster API`.
- `bearerAuth` exists.
- `/api/v1/preferences` exists.
- `/api/v1/date-ideas/recommendations` exists.

- [ ] **Step 2: Add annotations**

Add:

- `@Tag` on controllers.
- `@Operation` on endpoint methods.
- `@Schema` on request/response DTO fields.

- [ ] **Step 3: Run OpenAPI test**

Expected: PASS.

---

### Task 7: Error Response Optimization

**Files:**
- Modify: `GlobalExceptionHandler.java`
- Test: `GlobalExceptionHandlerTest.java`

- [ ] **Step 1: Write failing test for malformed JSON**

Use MockMvc to call `POST /api/v1/auth/register` with malformed JSON:

```json
{
  username: "bad_json"
}
```

Expected response:

- HTTP status: 400
- `code`: 400
- `message`: contains `请求体 JSON 格式错误`

- [ ] **Step 2: Implement handler**

Add `@ExceptionHandler(HttpMessageNotReadableException.class)` in `GlobalExceptionHandler` and return `ApiResponse.error(400, "请求体 JSON 格式错误")` with HTTP 400.

- [ ] **Step 3: Run test**

Expected: PASS.

---

### Task 8: Full Verification With Docker

**Files:**
- No source edits unless verification exposes defects.

- [ ] **Step 1: Run all automated tests**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test
```

Expected: BUILD SUCCESS.

- [ ] **Step 2: Apply schema to Docker MySQL**

Run:

```powershell
Get-Content -Raw src\main\resources\db\schema.sql | docker exec -i lovemaster-mysql mysql -uroot -p123456 lovemaster
```

Expected: `preference` and `date_idea` exist.

- [ ] **Step 3: Run Spring Boot app**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' spring-boot:run
```

Expected: `Tomcat started on port 8080`.

- [ ] **Step 4: Exercise API manually**

Verify:

- Register and login.
- Create private preference.
- Create couple preference after pairing.
- Query date ideas.
- Query recommendations.
- Import `/v3/api-docs` in Apifox.

- [ ] **Step 5: Stop local Spring Boot process**

Run:

```powershell
Get-CimInstance Win32_Process |
  Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*lovemaster-server*' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

Expected: no Java process remains for `lovemaster-server`.

---

## Self-Review Checklist

- [x] PRD MVP v1.2 scope is covered: preference CRUD, date idea query/recommendation, OpenAPI polish, error response optimization.
- [x] AI, memory, communication templates, file upload, and push notification are excluded from this plan.
- [x] No git commit steps are required because repository instructions forbid committing without explicit user confirmation.
- [x] Test-first flow is included for each implementation task.
- [x] Docker verification is included.
