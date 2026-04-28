# MVP v1.1 Pairing And Anniversary Implementation Plan

> **For agentic workers:** REQUIRED SUB-SKILL: Use superpowers:subagent-driven-development (recommended) or superpowers:executing-plans to implement this plan task-by-task. Steps use checkbox (`- [ ]`) syntax for tracking.

**Goal:** 实现 LoveMaster MVP v1.1 的情侣配对、用户信息查询、纪念日管理和权限校验闭环。

**Architecture:** 沿用当前 Spring Boot 分层结构：Controller 负责 REST 入参和响应包装，Service 负责业务规则和权限校验，Mapper 使用 MyBatis-Plus 访问 MySQL/H2，实体统一使用逻辑删除和自动时间填充。新增 `couple` 作为情侣关系空间，新增 `anniversary` 承载个人/双方可见纪念日。

**Tech Stack:** Java 17、Spring Boot 3.2.0、Spring Security、JWT、MyBatis-Plus 3.5.5、MySQL 8.0、Redis、JUnit 5、H2。

---

## File Structure

### Existing Files To Modify

- `src/main/resources/db/schema.sql`：新增 `couple` 和 `anniversary` 表。
- `src/test/resources/schema.sql`：新增 H2 兼容测试表。
- `src/main/java/com/lovemaster/exception/ErrorCode.java`：新增配对和纪念日业务错误码。
- `src/main/java/com/lovemaster/service/UserService.java`：新增按配对码查询、按 ID 更新等用户相关方法。
- `src/main/java/com/lovemaster/service/impl/UserServiceImpl.java`：实现新增用户查询方法。

### New Domain Files

- `src/main/java/com/lovemaster/entity/Couple.java`：情侣关系实体。
- `src/main/java/com/lovemaster/entity/Anniversary.java`：纪念日实体。
- `src/main/java/com/lovemaster/mapper/CoupleMapper.java`：情侣关系 Mapper。
- `src/main/java/com/lovemaster/mapper/AnniversaryMapper.java`：纪念日 Mapper。

### New DTO Files

- `src/main/java/com/lovemaster/dto/request/BindPairRequest.java`
- `src/main/java/com/lovemaster/dto/request/CreateAnniversaryRequest.java`
- `src/main/java/com/lovemaster/dto/request/UpdateAnniversaryRequest.java`
- `src/main/java/com/lovemaster/dto/response/PairingResponse.java`
- `src/main/java/com/lovemaster/dto/response/PartnerResponse.java`
- `src/main/java/com/lovemaster/dto/response/AnniversaryResponse.java`

### New Service Files

- `src/main/java/com/lovemaster/service/PairingService.java`
- `src/main/java/com/lovemaster/service/AnniversaryService.java`
- `src/main/java/com/lovemaster/service/impl/PairingServiceImpl.java`
- `src/main/java/com/lovemaster/service/impl/AnniversaryServiceImpl.java`

### New Controller Files

- `src/main/java/com/lovemaster/controller/UserController.java`
- `src/main/java/com/lovemaster/controller/PairingController.java`
- `src/main/java/com/lovemaster/controller/AnniversaryController.java`

### New Test Files

- `src/test/java/com/lovemaster/service/PairingServiceTest.java`
- `src/test/java/com/lovemaster/service/AnniversaryServiceTest.java`
- `src/test/java/com/lovemaster/controller/PairingControllerTest.java`
- `src/test/java/com/lovemaster/controller/AnniversaryControllerTest.java`

---

## Shared Constants And Decisions

- 配对状态：`1=ACTIVE`，`0=INACTIVE`。
- 纪念日类型：字符串枚举，允许 `LOVE_ANNIVERSARY`、`BIRTHDAY`、`CUSTOM`。
- 纪念日可见性：字符串枚举，允许 `PRIVATE`、`COUPLE`。
- 提醒天数：MVP 使用逗号字符串存储，例如 `"0,3,7"`，Service 对外使用 `List<Integer>`。
- 惊喜模式：`surpriseMode=true` 时必须 `visibility=PRIVATE`。
- 伴侣在 MVP 阶段可以查看双方可见纪念日，不能编辑或删除对方创建的纪念日。
- 未经用户明确确认，不执行 `git commit`。

---

### Task 1: Database Schema For Couple And Anniversary

**Files:**
- Modify: `src/main/resources/db/schema.sql`
- Modify: `src/test/resources/schema.sql`

- [ ] **Step 1: Add failing schema-aware service tests**

Create `src/test/java/com/lovemaster/service/PairingServiceTest.java` with the first failing test:

```java
package com.lovemaster.service;

import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.entity.User;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PairingServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Test
    @DisplayName("注册后用户应拥有可用于配对的邀请码")
    void registerShouldCreatePairCode() {
        RegisterRequest request = new RegisterRequest();
        request.setUsername("pair_user_a");
        request.setPassword("password123");

        authService.register(request);

        User user = userService.findByUsername("pair_user_a");
        assertNotNull(user.getPairCode());
        assertEquals(8, user.getPairCode().length());
    }
}
```

- [ ] **Step 2: Run test to verify current baseline**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PairingServiceTest
```

Expected: PASS. This confirms existing auth registration baseline before changing schema.

- [ ] **Step 3: Add MySQL schema**

Append to `src/main/resources/db/schema.sql`:

```sql
-- 情侣关系表
CREATE TABLE IF NOT EXISTS `couple` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '情侣关系ID',
    `user_id_1` BIGINT NOT NULL COMMENT '用户1 ID',
    `user_id_2` BIGINT NOT NULL COMMENT '用户2 ID',
    `status` TINYINT NOT NULL DEFAULT 1 COMMENT '状态：0-解除 1-有效',
    `paired_at` DATETIME NOT NULL COMMENT '配对时间',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_user_id_1` (`user_id_1`),
    KEY `idx_user_id_2` (`user_id_2`),
    KEY `idx_status` (`status`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='情侣关系表';

-- 纪念日表
CREATE TABLE IF NOT EXISTS `anniversary` (
    `id` BIGINT NOT NULL AUTO_INCREMENT COMMENT '纪念日ID',
    `creator_id` BIGINT NOT NULL COMMENT '创建者用户ID',
    `couple_id` BIGINT DEFAULT NULL COMMENT '情侣关系ID',
    `title` VARCHAR(50) NOT NULL COMMENT '标题',
    `date` DATE NOT NULL COMMENT '纪念日期',
    `type` VARCHAR(30) NOT NULL COMMENT '类型：LOVE_ANNIVERSARY/BIRTHDAY/CUSTOM',
    `visibility` VARCHAR(20) NOT NULL COMMENT '可见性：PRIVATE/COUPLE',
    `remind_days` VARCHAR(50) DEFAULT NULL COMMENT '提醒提前天数，逗号分隔',
    `surprise_mode` TINYINT NOT NULL DEFAULT 0 COMMENT '惊喜模式：0-否 1-是',
    `remark` VARCHAR(500) DEFAULT NULL COMMENT '备注',
    `created_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
    `updated_at` DATETIME NOT NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '更新时间',
    `deleted` TINYINT DEFAULT 0 COMMENT '逻辑删除：0-未删除 1-已删除',
    PRIMARY KEY (`id`),
    KEY `idx_creator_id` (`creator_id`),
    KEY `idx_couple_id` (`couple_id`),
    KEY `idx_date` (`date`),
    KEY `idx_visibility` (`visibility`)
) ENGINE=InnoDB DEFAULT CHARSET=utf8mb4 COLLATE=utf8mb4_unicode_ci COMMENT='纪念日表';
```

- [ ] **Step 4: Add H2 schema**

Append H2-compatible versions to `src/test/resources/schema.sql`:

```sql
CREATE TABLE IF NOT EXISTS couple (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    user_id_1 BIGINT NOT NULL,
    user_id_2 BIGINT NOT NULL,
    status TINYINT DEFAULT 1 NOT NULL,
    paired_at TIMESTAMP NOT NULL,
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_couple_user_id_1 ON couple(user_id_1);
CREATE INDEX IF NOT EXISTS idx_couple_user_id_2 ON couple(user_id_2);
CREATE INDEX IF NOT EXISTS idx_couple_status ON couple(status);

CREATE TABLE IF NOT EXISTS anniversary (
    id BIGINT AUTO_INCREMENT PRIMARY KEY,
    creator_id BIGINT NOT NULL,
    couple_id BIGINT,
    title VARCHAR(50) NOT NULL,
    date DATE NOT NULL,
    type VARCHAR(30) NOT NULL,
    visibility VARCHAR(20) NOT NULL,
    remind_days VARCHAR(50),
    surprise_mode TINYINT DEFAULT 0 NOT NULL,
    remark VARCHAR(500),
    created_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    updated_at TIMESTAMP DEFAULT CURRENT_TIMESTAMP NOT NULL,
    deleted TINYINT DEFAULT 0
);

CREATE INDEX IF NOT EXISTS idx_anniversary_creator_id ON anniversary(creator_id);
CREATE INDEX IF NOT EXISTS idx_anniversary_couple_id ON anniversary(couple_id);
CREATE INDEX IF NOT EXISTS idx_anniversary_date ON anniversary(date);
CREATE INDEX IF NOT EXISTS idx_anniversary_visibility ON anniversary(visibility);
```

- [ ] **Step 5: Run all tests**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test
```

Expected: BUILD SUCCESS with existing 8 tests plus the new baseline test.

---

### Task 2: Pairing Domain And Service

**Files:**
- Create: `src/main/java/com/lovemaster/entity/Couple.java`
- Create: `src/main/java/com/lovemaster/mapper/CoupleMapper.java`
- Create: `src/main/java/com/lovemaster/dto/request/BindPairRequest.java`
- Create: `src/main/java/com/lovemaster/dto/response/PartnerResponse.java`
- Create: `src/main/java/com/lovemaster/dto/response/PairingResponse.java`
- Create: `src/main/java/com/lovemaster/service/PairingService.java`
- Create: `src/main/java/com/lovemaster/service/impl/PairingServiceImpl.java`
- Modify: `src/main/java/com/lovemaster/exception/ErrorCode.java`
- Modify: `src/main/java/com/lovemaster/service/UserService.java`
- Modify: `src/main/java/com/lovemaster/service/impl/UserServiceImpl.java`
- Test: `src/test/java/com/lovemaster/service/PairingServiceTest.java`

- [ ] **Step 1: Expand failing service tests**

Replace `PairingServiceTest` with tests for successful pairing and invalid self-pairing:

```java
package com.lovemaster.service;

import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.PairingResponse;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class PairingServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private PairingService pairingService;

    @Test
    @DisplayName("输入伴侣邀请码后完成双向配对")
    void bindShouldPairTwoUsers() {
        User userA = register("pair_user_a");
        User userB = register("pair_user_b");

        BindPairRequest request = new BindPairRequest();
        request.setPairCode(userA.getPairCode());

        PairingResponse response = pairingService.bind(userB.getId(), request);

        assertTrue(response.getPaired());
        assertEquals(userA.getId(), response.getPartner().getId());

        User refreshedA = userService.findById(userA.getId());
        User refreshedB = userService.findById(userB.getId());
        assertEquals(userB.getId(), refreshedA.getPartnerId());
        assertEquals(userA.getId(), refreshedB.getPartnerId());
        assertNotNull(refreshedA.getPairedAt());
        assertNotNull(refreshedB.getPairedAt());
    }

    @Test
    @DisplayName("不能使用自己的邀请码配对")
    void bindShouldRejectOwnPairCode() {
        User user = register("pair_user_self");
        BindPairRequest request = new BindPairRequest();
        request.setPairCode(user.getPairCode());

        assertThrows(BusinessException.class, () -> pairingService.bind(user.getId(), request));
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
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PairingServiceTest
```

Expected: compilation failure because `PairingService`, `BindPairRequest`, and `PairingResponse` do not exist.

- [ ] **Step 3: Add error codes**

Modify `ErrorCode.java` by adding after auth errors:

```java
    // 配对相关 1200-1299
    PAIR_CODE_INVALID(1201, "邀请码无效"),
    CANNOT_PAIR_SELF(1202, "不能和自己配对"),
    USER_ALREADY_PAIRED(1203, "当前用户已配对"),
    PARTNER_ALREADY_PAIRED(1204, "对方已配对"),
    PAIRING_NOT_FOUND(1205, "当前没有配对关系"),
```

Keep enum syntax valid by making the final enum item end with `;`.

- [ ] **Step 4: Add Couple entity**

Create `Couple.java`:

```java
package com.lovemaster.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("couple")
public class Couple {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id_1")
    private Long userId1;

    @TableField("user_id_2")
    private Long userId2;

    private Integer status;

    @TableField("paired_at")
    private LocalDateTime pairedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

- [ ] **Step 5: Add CoupleMapper**

Create `CoupleMapper.java`:

```java
package com.lovemaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lovemaster.entity.Couple;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CoupleMapper extends BaseMapper<Couple> {
}
```

- [ ] **Step 6: Add request/response DTOs**

Create `BindPairRequest.java`:

```java
package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BindPairRequest {

    @NotBlank(message = "邀请码不能为空")
    @Size(min = 8, max = 20, message = "邀请码长度不正确")
    private String pairCode;
}
```

Create `PartnerResponse.java`:

```java
package com.lovemaster.dto.response;

import com.lovemaster.entity.User;
import lombok.Data;

@Data
public class PartnerResponse {

    private Long id;
    private String username;
    private String nickname;
    private String avatar;
    private Integer gender;

    public static PartnerResponse fromEntity(User user) {
        PartnerResponse response = new PartnerResponse();
        response.setId(user.getId());
        response.setUsername(user.getUsername());
        response.setNickname(user.getNickname());
        response.setAvatar(user.getAvatar());
        response.setGender(user.getGender());
        return response;
    }
}
```

Create `PairingResponse.java`:

```java
package com.lovemaster.dto.response;

import lombok.Data;

import java.time.LocalDateTime;

@Data
public class PairingResponse {

    private Boolean paired;
    private Long coupleId;
    private String pairCode;
    private PartnerResponse partner;
    private LocalDateTime pairedAt;

    public static PairingResponse unpaired(String pairCode) {
        PairingResponse response = new PairingResponse();
        response.setPaired(false);
        response.setPairCode(pairCode);
        return response;
    }
}
```

- [ ] **Step 7: Extend UserService**

Add to `UserService.java`:

```java
    User findByPairCode(String pairCode);
```

Add to `UserServiceImpl.java`:

```java
    @Override
    public User findByPairCode(String pairCode) {
        return userMapper.selectOne(
                new LambdaQueryWrapper<User>().eq(User::getPairCode, pairCode)
        );
    }
```

- [ ] **Step 8: Add PairingService interface**

Create `PairingService.java`:

```java
package com.lovemaster.service;

import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.response.PairingResponse;

public interface PairingService {

    PairingResponse getMyPairing(Long userId);

    PairingResponse bind(Long userId, BindPairRequest request);

    void unbind(Long userId);
}
```

- [ ] **Step 9: Add PairingService implementation**

Create `PairingServiceImpl.java`:

```java
package com.lovemaster.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.response.PairingResponse;
import com.lovemaster.dto.response.PartnerResponse;
import com.lovemaster.entity.Couple;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.mapper.CoupleMapper;
import com.lovemaster.service.PairingService;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Service
@RequiredArgsConstructor
public class PairingServiceImpl implements PairingService {

    private final CoupleMapper coupleMapper;
    private final UserService userService;

    @Override
    public PairingResponse getMyPairing(Long userId) {
        User user = requireUser(userId);
        if (user.getPartnerId() == null) {
            return PairingResponse.unpaired(user.getPairCode());
        }

        User partner = requireUser(user.getPartnerId());
        Couple couple = findActiveCouple(userId);
        PairingResponse response = new PairingResponse();
        response.setPaired(true);
        response.setPairCode(user.getPairCode());
        response.setCoupleId(couple != null ? couple.getId() : null);
        response.setPartner(PartnerResponse.fromEntity(partner));
        response.setPairedAt(user.getPairedAt());
        return response;
    }

    @Override
    @Transactional
    public PairingResponse bind(Long userId, BindPairRequest request) {
        User currentUser = requireUser(userId);
        User partner = userService.findByPairCode(request.getPairCode());

        if (partner == null) {
            throw new BusinessException(ErrorCode.PAIR_CODE_INVALID);
        }
        if (partner.getId().equals(userId)) {
            throw new BusinessException(ErrorCode.CANNOT_PAIR_SELF);
        }
        if (currentUser.getPartnerId() != null) {
            throw new BusinessException(ErrorCode.USER_ALREADY_PAIRED);
        }
        if (partner.getPartnerId() != null) {
            throw new BusinessException(ErrorCode.PARTNER_ALREADY_PAIRED);
        }

        LocalDateTime now = LocalDateTime.now();
        Couple couple = new Couple();
        couple.setUserId1(currentUser.getId());
        couple.setUserId2(partner.getId());
        couple.setStatus(1);
        couple.setPairedAt(now);
        coupleMapper.insert(couple);

        currentUser.setPartnerId(partner.getId());
        currentUser.setPairedAt(now);
        partner.setPartnerId(currentUser.getId());
        partner.setPairedAt(now);
        userService.update(currentUser);
        userService.update(partner);

        return getMyPairing(userId);
    }

    @Override
    @Transactional
    public void unbind(Long userId) {
        User currentUser = requireUser(userId);
        if (currentUser.getPartnerId() == null) {
            throw new BusinessException(ErrorCode.PAIRING_NOT_FOUND);
        }

        User partner = requireUser(currentUser.getPartnerId());
        Couple couple = findActiveCouple(userId);
        if (couple != null) {
            couple.setStatus(0);
            coupleMapper.updateById(couple);
        }

        currentUser.setPartnerId(null);
        currentUser.setPairedAt(null);
        partner.setPartnerId(null);
        partner.setPairedAt(null);
        userService.update(currentUser);
        userService.update(partner);
    }

    private User requireUser(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Couple findActiveCouple(Long userId) {
        return coupleMapper.selectOne(new LambdaQueryWrapper<Couple>()
                .eq(Couple::getStatus, 1)
                .and(wrapper -> wrapper
                        .eq(Couple::getUserId1, userId)
                        .or()
                        .eq(Couple::getUserId2, userId)));
    }
}
```

- [ ] **Step 10: Run service test**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PairingServiceTest
```

Expected: PASS.

---

### Task 3: User And Pairing Controllers

**Files:**
- Create: `src/main/java/com/lovemaster/controller/UserController.java`
- Create: `src/main/java/com/lovemaster/controller/PairingController.java`
- Test: `src/test/java/com/lovemaster/controller/PairingControllerTest.java`

- [ ] **Step 1: Write failing controller tests**

Create `PairingControllerTest.java`:

```java
package com.lovemaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.response.PairingResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.PairingService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.security.test.context.support.WithMockUser;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class PairingControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private PairingService pairingService;

    @Test
    @DisplayName("GET /api/v1/pairings/me - 成功")
    void getMyPairingShouldReturnSuccess() throws Exception {
        User principal = new User();
        principal.setId(1L);
        PairingResponse response = PairingResponse.unpaired("ABCDEFGH");
        when(pairingService.getMyPairing(1L)).thenReturn(response);

        mockMvc.perform(get("/api/v1/pairings/me")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paired").value(false))
                .andExpect(jsonPath("$.data.pairCode").value("ABCDEFGH"));
    }

    @Test
    @DisplayName("POST /api/v1/pairings/bind - 成功")
    void bindShouldReturnSuccess() throws Exception {
        User principal = new User();
        principal.setId(1L);
        BindPairRequest request = new BindPairRequest();
        request.setPairCode("ABCDEFGH");
        PairingResponse response = new PairingResponse();
        response.setPaired(true);
        when(pairingService.bind(eq(1L), any(BindPairRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/pairings/bind")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.paired").value(true));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PairingControllerTest
```

Expected: compile failure because `PairingController` does not exist.

- [ ] **Step 3: Add UserController**

Create `UserController.java`:

```java
package com.lovemaster.controller;

import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.dto.response.UserResponse;
import com.lovemaster.entity.User;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/users")
public class UserController {

    @GetMapping("/me")
    public ApiResponse<UserResponse> me(@AuthenticationPrincipal User user) {
        return ApiResponse.success(UserResponse.fromEntity(user));
    }
}
```

- [ ] **Step 4: Add PairingController**

Create `PairingController.java`:

```java
package com.lovemaster.controller;

import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.dto.response.PairingResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.PairingService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/pairings")
@RequiredArgsConstructor
public class PairingController {

    private final PairingService pairingService;

    @GetMapping("/me")
    public ApiResponse<PairingResponse> getMyPairing(@AuthenticationPrincipal User user) {
        return ApiResponse.success(pairingService.getMyPairing(user.getId()));
    }

    @PostMapping("/bind")
    public ApiResponse<PairingResponse> bind(@AuthenticationPrincipal User user,
                                             @Valid @RequestBody BindPairRequest request) {
        return ApiResponse.success("配对成功", pairingService.bind(user.getId(), request));
    }

    @PostMapping("/unbind")
    public ApiResponse<Void> unbind(@AuthenticationPrincipal User user) {
        pairingService.unbind(user.getId());
        return ApiResponse.success("解除配对成功", null);
    }
}
```

- [ ] **Step 5: Run controller test**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=PairingControllerTest
```

Expected: PASS.

---

### Task 4: Anniversary Domain And Validation

**Files:**
- Create: `src/main/java/com/lovemaster/entity/Anniversary.java`
- Create: `src/main/java/com/lovemaster/mapper/AnniversaryMapper.java`
- Create: `src/main/java/com/lovemaster/dto/request/CreateAnniversaryRequest.java`
- Create: `src/main/java/com/lovemaster/dto/request/UpdateAnniversaryRequest.java`
- Create: `src/main/java/com/lovemaster/dto/response/AnniversaryResponse.java`
- Create: `src/main/java/com/lovemaster/service/AnniversaryService.java`
- Create: `src/main/java/com/lovemaster/service/impl/AnniversaryServiceImpl.java`
- Modify: `src/main/java/com/lovemaster/exception/ErrorCode.java`
- Test: `src/test/java/com/lovemaster/service/AnniversaryServiceTest.java`

- [ ] **Step 1: Write failing service tests**

Create `AnniversaryServiceTest.java` with tests for private creation and rejecting invalid couple visibility:

```java
package com.lovemaster.service;

import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.AnniversaryResponse;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDate;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AnniversaryServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private AnniversaryService anniversaryService;

    @Test
    @DisplayName("未配对用户可以创建个人纪念日")
    void createPrivateAnniversaryShouldSucceed() {
        User user = register("ann_user_private");
        CreateAnniversaryRequest request = new CreateAnniversaryRequest();
        request.setTitle("第一次约会");
        request.setDate(LocalDate.of(2026, 5, 20));
        request.setType("CUSTOM");
        request.setVisibility("PRIVATE");
        request.setRemindDays(List.of(0, 3));
        request.setSurpriseMode(true);
        request.setRemark("准备手写卡片");

        AnniversaryResponse response = anniversaryService.create(user.getId(), request);

        assertNotNull(response.getId());
        assertEquals("第一次约会", response.getTitle());
        assertEquals("PRIVATE", response.getVisibility());
        assertTrue(response.getSurpriseMode());
        assertEquals(List.of(0, 3), response.getRemindDays());
    }

    @Test
    @DisplayName("未配对用户不能创建双方可见纪念日")
    void createCoupleAnniversaryWithoutPairingShouldFail() {
        User user = register("ann_user_unpaired");
        CreateAnniversaryRequest request = new CreateAnniversaryRequest();
        request.setTitle("恋爱纪念日");
        request.setDate(LocalDate.of(2026, 5, 20));
        request.setType("LOVE_ANNIVERSARY");
        request.setVisibility("COUPLE");
        request.setRemindDays(List.of(7));
        request.setSurpriseMode(false);

        assertThrows(BusinessException.class, () -> anniversaryService.create(user.getId(), request));
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
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=AnniversaryServiceTest
```

Expected: compilation failure because anniversary classes do not exist.

- [ ] **Step 3: Add anniversary error codes**

Add to `ErrorCode.java`:

```java
    // 纪念日相关 1300-1399
    ANNIVERSARY_NOT_FOUND(1301, "纪念日不存在"),
    ANNIVERSARY_ACCESS_DENIED(1302, "无权访问该纪念日"),
    ANNIVERSARY_VISIBILITY_INVALID(1303, "纪念日可见性不合法"),
    ANNIVERSARY_TYPE_INVALID(1304, "纪念日类型不合法"),
    SURPRISE_MODE_VISIBILITY_CONFLICT(1305, "惊喜模式只能用于个人可见纪念日");
```

- [ ] **Step 4: Add Anniversary entity and mapper**

Create `Anniversary.java`:

```java
package com.lovemaster.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("anniversary")
public class Anniversary {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("couple_id")
    private Long coupleId;

    private String title;

    private LocalDate date;

    private String type;

    private String visibility;

    @TableField("remind_days")
    private String remindDays;

    @TableField("surprise_mode")
    private Boolean surpriseMode;

    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
```

Create `AnniversaryMapper.java`:

```java
package com.lovemaster.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.lovemaster.entity.Anniversary;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface AnniversaryMapper extends BaseMapper<Anniversary> {
}
```

- [ ] **Step 5: Add request and response DTOs**

Create `CreateAnniversaryRequest.java`:

```java
package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateAnniversaryRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题最多50个字符")
    private String title;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotBlank(message = "可见性不能为空")
    private String visibility;

    private List<Integer> remindDays;

    private Boolean surpriseMode = false;

    @Size(max = 500, message = "备注最多500个字符")
    private String remark;
}
```

Create `UpdateAnniversaryRequest.java` with the same fields as create request.

Create `AnniversaryResponse.java`:

```java
package com.lovemaster.dto.response;

import com.lovemaster.entity.Anniversary;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.Collections;
import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@Data
public class AnniversaryResponse {

    private Long id;
    private Long creatorId;
    private Long coupleId;
    private String title;
    private LocalDate date;
    private String type;
    private String visibility;
    private List<Integer> remindDays;
    private Boolean surpriseMode;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;

    public static AnniversaryResponse fromEntity(Anniversary anniversary) {
        AnniversaryResponse response = new AnniversaryResponse();
        response.setId(anniversary.getId());
        response.setCreatorId(anniversary.getCreatorId());
        response.setCoupleId(anniversary.getCoupleId());
        response.setTitle(anniversary.getTitle());
        response.setDate(anniversary.getDate());
        response.setType(anniversary.getType());
        response.setVisibility(anniversary.getVisibility());
        response.setRemindDays(parseRemindDays(anniversary.getRemindDays()));
        response.setSurpriseMode(Boolean.TRUE.equals(anniversary.getSurpriseMode()));
        response.setRemark(anniversary.getRemark());
        response.setCreatedAt(anniversary.getCreatedAt());
        response.setUpdatedAt(anniversary.getUpdatedAt());
        return response;
    }

    private static List<Integer> parseRemindDays(String remindDays) {
        if (remindDays == null || remindDays.isBlank()) {
            return Collections.emptyList();
        }
        return Stream.of(remindDays.split(","))
                .map(Integer::parseInt)
                .collect(Collectors.toList());
    }
}
```

- [ ] **Step 6: Add AnniversaryService interface**

Create `AnniversaryService.java`:

```java
package com.lovemaster.service;

import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;

import java.util.List;

public interface AnniversaryService {

    AnniversaryResponse create(Long userId, CreateAnniversaryRequest request);

    List<AnniversaryResponse> list(Long userId, String type, String visibility);

    AnniversaryResponse get(Long userId, Long anniversaryId);

    AnniversaryResponse update(Long userId, Long anniversaryId, UpdateAnniversaryRequest request);

    void delete(Long userId, Long anniversaryId);

    List<AnniversaryResponse> reminders(Long userId, Integer days);
}
```

- [ ] **Step 7: Add minimal AnniversaryService implementation**

Create `AnniversaryServiceImpl.java` with `create` implemented first:

```java
package com.lovemaster.service.impl;

import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;
import com.lovemaster.entity.Anniversary;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.mapper.AnniversaryMapper;
import com.lovemaster.service.AnniversaryService;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Set;
import java.util.stream.Collectors;

@Service
@RequiredArgsConstructor
public class AnniversaryServiceImpl implements AnniversaryService {

    private static final Set<String> TYPES = Set.of("LOVE_ANNIVERSARY", "BIRTHDAY", "CUSTOM");
    private static final Set<String> VISIBILITIES = Set.of("PRIVATE", "COUPLE");
    private static final Set<Integer> ALLOWED_REMIND_DAYS = Set.of(0, 1, 3, 7);

    private final AnniversaryMapper anniversaryMapper;
    private final UserService userService;

    @Override
    public AnniversaryResponse create(Long userId, CreateAnniversaryRequest request) {
        User user = requireUser(userId);
        validateRequest(request.getType(), request.getVisibility(), request.getRemindDays(), request.getSurpriseMode());

        Anniversary anniversary = new Anniversary();
        anniversary.setCreatorId(userId);
        anniversary.setCoupleId("COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null);
        anniversary.setTitle(request.getTitle());
        anniversary.setDate(request.getDate());
        anniversary.setType(request.getType());
        anniversary.setVisibility(request.getVisibility());
        anniversary.setRemindDays(formatRemindDays(request.getRemindDays()));
        anniversary.setSurpriseMode(Boolean.TRUE.equals(request.getSurpriseMode()));
        anniversary.setRemark(request.getRemark());
        anniversaryMapper.insert(anniversary);
        return AnniversaryResponse.fromEntity(anniversary);
    }

    @Override
    public List<AnniversaryResponse> list(Long userId, String type, String visibility) {
        throw new UnsupportedOperationException("list will be implemented in Task 5");
    }

    @Override
    public AnniversaryResponse get(Long userId, Long anniversaryId) {
        throw new UnsupportedOperationException("get will be implemented in Task 5");
    }

    @Override
    public AnniversaryResponse update(Long userId, Long anniversaryId, UpdateAnniversaryRequest request) {
        throw new UnsupportedOperationException("update will be implemented in Task 5");
    }

    @Override
    public void delete(Long userId, Long anniversaryId) {
        throw new UnsupportedOperationException("delete will be implemented in Task 5");
    }

    @Override
    public List<AnniversaryResponse> reminders(Long userId, Integer days) {
        throw new UnsupportedOperationException("reminders will be implemented in Task 5");
    }

    private User requireUser(Long userId) {
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }
        return user;
    }

    private Long requireCoupleId(User user) {
        if (user.getPartnerId() == null) {
            throw new BusinessException(ErrorCode.PAIRING_NOT_FOUND);
        }
        return null;
    }

    private void validateRequest(String type, String visibility, List<Integer> remindDays, Boolean surpriseMode) {
        if (!TYPES.contains(type)) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_TYPE_INVALID);
        }
        if (!VISIBILITIES.contains(visibility)) {
            throw new BusinessException(ErrorCode.ANNIVERSARY_VISIBILITY_INVALID);
        }
        if (Boolean.TRUE.equals(surpriseMode) && "COUPLE".equals(visibility)) {
            throw new BusinessException(ErrorCode.SURPRISE_MODE_VISIBILITY_CONFLICT);
        }
        if (remindDays != null && !ALLOWED_REMIND_DAYS.containsAll(remindDays)) {
            throw new BusinessException(ErrorCode.BAD_REQUEST, "提醒天数只能是0、1、3、7");
        }
    }

    private String formatRemindDays(List<Integer> remindDays) {
        if (remindDays == null || remindDays.isEmpty()) {
            return null;
        }
        return remindDays.stream()
                .distinct()
                .sorted()
                .map(String::valueOf)
                .collect(Collectors.joining(","));
    }
}
```

- [ ] **Step 8: Run anniversary creation tests**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=AnniversaryServiceTest
```

Expected: PASS for the two creation validation tests.

---

### Task 5: Anniversary Listing, Access Control, Update, Delete, Reminder

**Files:**
- Modify: `src/main/java/com/lovemaster/service/impl/AnniversaryServiceImpl.java`
- Test: `src/test/java/com/lovemaster/service/AnniversaryServiceTest.java`

- [ ] **Step 1: Add failing tests for visibility and ownership**

Add tests to `AnniversaryServiceTest`:

```java
@Test
@DisplayName("创建者可以编辑自己的纪念日")
void creatorCanUpdateAnniversary() {
    User user = register("ann_user_update");
    AnniversaryResponse created = anniversaryService.create(user.getId(), privateRequest("旧标题"));

    UpdateAnniversaryRequest update = new UpdateAnniversaryRequest();
    update.setTitle("新标题");
    update.setDate(LocalDate.of(2026, 6, 1));
    update.setType("CUSTOM");
    update.setVisibility("PRIVATE");
    update.setRemindDays(List.of(1));
    update.setSurpriseMode(false);
    update.setRemark("更新备注");

    AnniversaryResponse response = anniversaryService.update(user.getId(), created.getId(), update);

    assertEquals("新标题", response.getTitle());
    assertEquals(List.of(1), response.getRemindDays());
}

@Test
@DisplayName("非创建者不能删除别人的个人纪念日")
void nonCreatorCannotDeletePrivateAnniversary() {
    User owner = register("ann_user_owner");
    User other = register("ann_user_other");
    AnniversaryResponse created = anniversaryService.create(owner.getId(), privateRequest("私人纪念日"));

    assertThrows(BusinessException.class, () -> anniversaryService.delete(other.getId(), created.getId()));
}

private CreateAnniversaryRequest privateRequest(String title) {
    CreateAnniversaryRequest request = new CreateAnniversaryRequest();
    request.setTitle(title);
    request.setDate(LocalDate.of(2026, 5, 20));
    request.setType("CUSTOM");
    request.setVisibility("PRIVATE");
    request.setRemindDays(List.of(0, 3));
    request.setSurpriseMode(false);
    return request;
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=AnniversaryServiceTest
```

Expected: failure because `update` and `delete` are not implemented.

- [ ] **Step 3: Implement list/get/update/delete/reminders**

In `AnniversaryServiceImpl`, replace unsupported methods with:

```java
@Override
public List<AnniversaryResponse> list(Long userId, String type, String visibility) {
    User user = requireUser(userId);
    return anniversaryMapper.selectList(new LambdaQueryWrapper<Anniversary>()
                    .and(wrapper -> wrapper
                            .eq(Anniversary::getCreatorId, userId)
                            .or(user.getPartnerId() != null,
                                    coupleWrapper -> coupleWrapper
                                            .eq(Anniversary::getVisibility, "COUPLE")
                                            .eq(Anniversary::getCoupleId, requireCoupleId(user))))
                    .eq(type != null && !type.isBlank(), Anniversary::getType, type)
                    .eq(visibility != null && !visibility.isBlank(), Anniversary::getVisibility, visibility)
                    .orderByAsc(Anniversary::getDate))
            .stream()
            .map(AnniversaryResponse::fromEntity)
            .toList();
}

@Override
public AnniversaryResponse get(Long userId, Long anniversaryId) {
    Anniversary anniversary = requireVisibleAnniversary(userId, anniversaryId);
    return AnniversaryResponse.fromEntity(anniversary);
}

@Override
public AnniversaryResponse update(Long userId, Long anniversaryId, UpdateAnniversaryRequest request) {
    Anniversary anniversary = requireManageableAnniversary(userId, anniversaryId);
    User user = requireUser(userId);
    validateRequest(request.getType(), request.getVisibility(), request.getRemindDays(), request.getSurpriseMode());
    anniversary.setCoupleId("COUPLE".equals(request.getVisibility()) ? requireCoupleId(user) : null);
    anniversary.setTitle(request.getTitle());
    anniversary.setDate(request.getDate());
    anniversary.setType(request.getType());
    anniversary.setVisibility(request.getVisibility());
    anniversary.setRemindDays(formatRemindDays(request.getRemindDays()));
    anniversary.setSurpriseMode(Boolean.TRUE.equals(request.getSurpriseMode()));
    anniversary.setRemark(request.getRemark());
    anniversaryMapper.updateById(anniversary);
    return AnniversaryResponse.fromEntity(anniversary);
}

@Override
public void delete(Long userId, Long anniversaryId) {
    Anniversary anniversary = requireManageableAnniversary(userId, anniversaryId);
    anniversaryMapper.deleteById(anniversary.getId());
}

@Override
public List<AnniversaryResponse> reminders(Long userId, Integer days) {
    int windowDays = days == null ? 7 : days;
    LocalDate today = LocalDate.now();
    LocalDate end = today.plusDays(windowDays);
    return list(userId, null, null).stream()
            .filter(item -> !item.getDate().isBefore(today) && !item.getDate().isAfter(end))
            .filter(item -> item.getRemindDays().stream()
                    .anyMatch(remindDay -> item.getDate().minusDays(remindDay).equals(today)))
            .toList();
}
```

Also add imports:

```java
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import java.time.LocalDate;
```

Add helper methods:

```java
private Anniversary requireVisibleAnniversary(Long userId, Long anniversaryId) {
    Anniversary anniversary = anniversaryMapper.selectById(anniversaryId);
    if (anniversary == null) {
        throw new BusinessException(ErrorCode.ANNIVERSARY_NOT_FOUND);
    }
    if (!canView(userId, anniversary)) {
        throw new BusinessException(ErrorCode.ANNIVERSARY_ACCESS_DENIED);
    }
    return anniversary;
}

private Anniversary requireManageableAnniversary(Long userId, Long anniversaryId) {
    Anniversary anniversary = anniversaryMapper.selectById(anniversaryId);
    if (anniversary == null) {
        throw new BusinessException(ErrorCode.ANNIVERSARY_NOT_FOUND);
    }
    if (!anniversary.getCreatorId().equals(userId)) {
        throw new BusinessException(ErrorCode.ANNIVERSARY_ACCESS_DENIED);
    }
    return anniversary;
}

private boolean canView(Long userId, Anniversary anniversary) {
    if (anniversary.getCreatorId().equals(userId)) {
        return true;
    }
    User user = requireUser(userId);
    return user.getPartnerId() != null
            && "COUPLE".equals(anniversary.getVisibility())
            && anniversary.getCoupleId() != null
            && anniversary.getCoupleId().equals(requireCoupleId(user));
}
```

- [ ] **Step 4: Make `requireCoupleId` return active couple ID**

Inject `CoupleMapper` into `AnniversaryServiceImpl` and implement:

```java
private final CoupleMapper coupleMapper;
```

```java
private Long requireCoupleId(User user) {
    if (user.getPartnerId() == null) {
        throw new BusinessException(ErrorCode.PAIRING_NOT_FOUND);
    }
    Couple couple = coupleMapper.selectOne(new LambdaQueryWrapper<Couple>()
            .eq(Couple::getStatus, 1)
            .and(wrapper -> wrapper
                    .eq(Couple::getUserId1, user.getId())
                    .or()
                    .eq(Couple::getUserId2, user.getId())));
    if (couple == null) {
        throw new BusinessException(ErrorCode.PAIRING_NOT_FOUND);
    }
    return couple.getId();
}
```

Add imports:

```java
import com.lovemaster.entity.Couple;
import com.lovemaster.mapper.CoupleMapper;
```

- [ ] **Step 5: Run anniversary tests**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=AnniversaryServiceTest
```

Expected: PASS.

---

### Task 6: Anniversary Controller

**Files:**
- Create: `src/main/java/com/lovemaster/controller/AnniversaryController.java`
- Test: `src/test/java/com/lovemaster/controller/AnniversaryControllerTest.java`

- [ ] **Step 1: Write failing controller test**

Create `AnniversaryControllerTest.java`:

```java
package com.lovemaster.controller;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.AnniversaryService;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.http.MediaType;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.test.web.servlet.MockMvc;

import java.time.LocalDate;
import java.util.List;

import static org.mockito.ArgumentMatchers.any;
import static org.mockito.ArgumentMatchers.eq;
import static org.mockito.Mockito.when;
import static org.springframework.security.test.web.servlet.request.SecurityMockMvcRequestPostProcessors.authentication;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.get;
import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.post;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest
@AutoConfigureMockMvc
@ActiveProfiles("test")
class AnniversaryControllerTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private ObjectMapper objectMapper;

    @MockBean
    private AnniversaryService anniversaryService;

    @Test
    @DisplayName("POST /api/v1/anniversaries - 成功")
    void createShouldReturnSuccess() throws Exception {
        User principal = new User();
        principal.setId(1L);

        CreateAnniversaryRequest request = new CreateAnniversaryRequest();
        request.setTitle("第一次约会");
        request.setDate(LocalDate.of(2026, 5, 20));
        request.setType("CUSTOM");
        request.setVisibility("PRIVATE");
        request.setRemindDays(List.of(0, 3));
        request.setSurpriseMode(false);

        AnniversaryResponse response = new AnniversaryResponse();
        response.setId(10L);
        response.setTitle("第一次约会");
        response.setDate(LocalDate.of(2026, 5, 20));
        response.setType("CUSTOM");
        response.setVisibility("PRIVATE");
        response.setRemindDays(List.of(0, 3));
        response.setSurpriseMode(false);

        when(anniversaryService.create(eq(1L), any(CreateAnniversaryRequest.class))).thenReturn(response);

        mockMvc.perform(post("/api/v1/anniversaries")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null)))
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200))
                .andExpect(jsonPath("$.data.id").value(10))
                .andExpect(jsonPath("$.data.title").value("第一次约会"));
    }

    @Test
    @DisplayName("GET /api/v1/anniversaries - 成功")
    void listShouldReturnSuccess() throws Exception {
        User principal = new User();
        principal.setId(1L);
        when(anniversaryService.list(1L, null, null)).thenReturn(List.of());

        mockMvc.perform(get("/api/v1/anniversaries")
                        .with(authentication(new org.springframework.security.authentication.UsernamePasswordAuthenticationToken(principal, null))))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.code").value(200));
    }
}
```

- [ ] **Step 2: Run test to verify it fails**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=AnniversaryControllerTest
```

Expected: compile failure because `AnniversaryController` does not exist.

- [ ] **Step 3: Add AnniversaryController**

Create `AnniversaryController.java`:

```java
package com.lovemaster.controller;

import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.AnniversaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/anniversaries")
@RequiredArgsConstructor
public class AnniversaryController {

    private final AnniversaryService anniversaryService;

    @PostMapping
    public ApiResponse<AnniversaryResponse> create(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody CreateAnniversaryRequest request) {
        return ApiResponse.success("创建成功", anniversaryService.create(user.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<AnniversaryResponse>> list(@AuthenticationPrincipal User user,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) String visibility) {
        return ApiResponse.success(anniversaryService.list(user.getId(), type, visibility));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnniversaryResponse> get(@AuthenticationPrincipal User user, @PathVariable Long id) {
        return ApiResponse.success(anniversaryService.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnniversaryResponse> update(@AuthenticationPrincipal User user,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody UpdateAnniversaryRequest request) {
        return ApiResponse.success("更新成功", anniversaryService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User user, @PathVariable Long id) {
        anniversaryService.delete(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/reminders")
    public ApiResponse<List<AnniversaryResponse>> reminders(@AuthenticationPrincipal User user,
                                                            @RequestParam(required = false) Integer days) {
        return ApiResponse.success(anniversaryService.reminders(user.getId(), days));
    }
}
```

- [ ] **Step 4: Run controller test**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test -Dtest=AnniversaryControllerTest
```

Expected: PASS.

---

### Task 7: End-To-End Verification With Docker

**Files:**
- No source edits unless verification exposes defects.

- [ ] **Step 1: Run all automated tests**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' test
```

Expected: BUILD SUCCESS. All existing and new tests pass.

- [ ] **Step 2: Start Docker dependencies**

Run:

```powershell
docker compose up -d
docker compose ps
```

Expected: `lovemaster-mysql` and `lovemaster-redis` are `healthy`.

- [ ] **Step 3: Restart dev database if schema was already initialized before new tables**

If `couple` and `anniversary` are missing because the MySQL volume already existed, run:

```powershell
docker exec lovemaster-mysql mysql -uroot -p123456 -D lovemaster -e "SHOW TABLES;"
```

If new tables are absent, manually apply schema:

```powershell
docker exec -i lovemaster-mysql mysql -uroot -p123456 lovemaster < src/main/resources/db/schema.sql
```

Expected: `SHOW TABLES;` includes `user`, `couple`, and `anniversary`.

- [ ] **Step 4: Run Spring Boot app**

Run:

```powershell
& 'C:\Users\91414\.m2\wrapper\dists\apache-maven-3.9.9\977a63e90f436cd6ade95b4c0e10c20c\bin\mvn.cmd' spring-boot:run
```

Expected: log contains `Tomcat started on port 8080`.

- [ ] **Step 5: Exercise API manually**

Register user A:

```powershell
$a = Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/register -Method Post -ContentType 'application/json' -Body '{"username":"api_user_a","password":"password123"}'
$a.data.pairCode
```

Register user B:

```powershell
$b = Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/register -Method Post -ContentType 'application/json' -Body '{"username":"api_user_b","password":"password123"}'
```

Login user B:

```powershell
$loginB = Invoke-RestMethod -Uri http://localhost:8080/api/v1/auth/login -Method Post -ContentType 'application/json' -Body '{"username":"api_user_b","password":"password123"}'
$headersB = @{ Authorization = "Bearer $($loginB.data.accessToken)" }
```

Bind B to A:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/v1/pairings/bind -Method Post -Headers $headersB -ContentType 'application/json' -Body (@{ pairCode = $a.data.pairCode } | ConvertTo-Json)
```

Create couple anniversary:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/v1/anniversaries -Method Post -Headers $headersB -ContentType 'application/json' -Body '{"title":"恋爱纪念日","date":"2026-05-20","type":"LOVE_ANNIVERSARY","visibility":"COUPLE","remindDays":[0,3,7],"surpriseMode":false}'
```

List anniversaries:

```powershell
Invoke-RestMethod -Uri http://localhost:8080/api/v1/anniversaries -Method Get -Headers $headersB
```

Expected: bind returns `paired=true`; anniversary create returns `code=200`; list includes the created anniversary.

- [ ] **Step 6: Stop local Spring Boot process**

After manual verification, stop the Spring Boot process:

```powershell
Get-CimInstance Win32_Process |
  Where-Object { $_.Name -like 'java*' -and $_.CommandLine -like '*lovemaster-server*' } |
  ForEach-Object { Stop-Process -Id $_.ProcessId -Force }
```

Expected: no Java process remains for `lovemaster-server`.

---

## Self-Review Checklist

- [x] PRD MVP v1.1 scope is covered: user info, pairing, anniversary CRUD, reminder query, permissions, tests.
- [x] AI, preference, date idea, memory, and communication template are excluded from this implementation plan because PRD places them in later versions.
- [x] No git commit steps are required because repository instructions forbid committing without explicit user confirmation.
- [x] Test-first flow is included for each implementation task.
- [x] Docker verification is included.
