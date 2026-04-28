package com.lovemaster.service;

import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.request.CreateMemoryRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.request.UpdateMemoryRequest;
import com.lovemaster.dto.response.MemoryResponse;
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
class MemoryServiceTest {

    @Autowired private AuthService authService;
    @Autowired private UserService userService;
    @Autowired private PairingService pairingService;
    @Autowired private MemoryService memoryService;

    @Test
    @DisplayName("未配对用户可以创建个人回忆")
    void createPrivateMemoryShouldSucceed() {
        User user = register("memory_user_private");

        MemoryResponse response = memoryService.create(user.getId(),
                createRequest("江边散步", "PRIVATE", LocalDate.of(2026, 5, 20)));

        assertNotNull(response.getId());
        assertEquals("江边散步", response.getTitle());
        assertEquals("PRIVATE", response.getVisibility());
        assertEquals(List.of("拍照", "散步"), response.getTags());
    }

    @Test
    @DisplayName("未配对用户不能创建双方可见回忆")
    void createCoupleMemoryWithoutPairingShouldFail() {
        User user = register("memory_user_unpaired");

        assertThrows(BusinessException.class, () -> memoryService.create(user.getId(),
                createRequest("共同晚餐", "COUPLE", LocalDate.of(2026, 5, 21))));
    }

    @Test
    @DisplayName("配对后伴侣可以查看双方可见回忆，不能查看个人回忆")
    void listShouldRespectVisibility() {
        User user = register("memory_user_list_a");
        User partner = register("memory_user_list_b");
        pair(user, partner);

        MemoryResponse coupleMemory = memoryService.create(user.getId(),
                createRequest("共同散步", "COUPLE", LocalDate.of(2026, 5, 20)));
        MemoryResponse privateMemory = memoryService.create(user.getId(),
                createRequest("秘密准备", "PRIVATE", LocalDate.of(2026, 5, 21)));

        List<MemoryResponse> partnerVisibleList = memoryService.list(partner.getId(), null, null, null, null);

        assertTrue(partnerVisibleList.stream().anyMatch(item -> item.getId().equals(coupleMemory.getId())));
        assertTrue(partnerVisibleList.stream().noneMatch(item -> item.getId().equals(privateMemory.getId())));
        assertThrows(BusinessException.class, () -> memoryService.get(partner.getId(), privateMemory.getId()));
    }

    @Test
    @DisplayName("只有创建者可以更新或删除回忆")
    void updateAndDeleteShouldRequireCreator() {
        User user = register("memory_user_update_a");
        User partner = register("memory_user_update_b");
        pair(user, partner);
        MemoryResponse created = memoryService.create(user.getId(),
                createRequest("共同散步", "COUPLE", LocalDate.of(2026, 5, 20)));

        assertThrows(BusinessException.class, () -> memoryService.update(partner.getId(), created.getId(), updateRequest("被篡改")));
        MemoryResponse updated = memoryService.update(user.getId(), created.getId(), updateRequest("更新后的回忆"));

        assertEquals("更新后的回忆", updated.getTitle());
        assertThrows(BusinessException.class, () -> memoryService.delete(partner.getId(), created.getId()));
        memoryService.delete(user.getId(), created.getId());
        assertThrows(BusinessException.class, () -> memoryService.get(user.getId(), created.getId()));
    }

    @Test
    @DisplayName("列表按回忆日期倒序返回")
    void listShouldOrderByMemoryDateDesc() {
        User user = register("memory_user_order");
        memoryService.create(user.getId(), createRequest("较早回忆", "PRIVATE", LocalDate.of(2026, 5, 1)));
        memoryService.create(user.getId(), createRequest("较晚回忆", "PRIVATE", LocalDate.of(2026, 5, 20)));

        List<MemoryResponse> list = memoryService.list(user.getId(), null, null, null, null);

        assertEquals("较晚回忆", list.get(0).getTitle());
        assertEquals("较早回忆", list.get(1).getTitle());
    }

    private User register(String username) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("password123");
        authService.register(request);
        return userService.findByUsername(username);
    }

    private void pair(User user, User partner) {
        BindPairRequest request = new BindPairRequest();
        request.setPairCode(partner.getPairCode());
        pairingService.bind(user.getId(), request);
    }

    private CreateMemoryRequest createRequest(String title, String visibility, LocalDate memoryDate) {
        CreateMemoryRequest request = new CreateMemoryRequest();
        request.setTitle(title);
        request.setMemoryDate(memoryDate);
        request.setLocation("江边公园");
        request.setContent("一起散步拍了很多照片。");
        request.setImageUrl("/uploads/images/test.jpg");
        request.setVisibility(visibility);
        request.setTags(List.of("散步", "拍照"));
        request.setRemark("测试回忆");
        return request;
    }

    private UpdateMemoryRequest updateRequest(String title) {
        UpdateMemoryRequest request = new UpdateMemoryRequest();
        request.setTitle(title);
        request.setMemoryDate(LocalDate.of(2026, 5, 22));
        request.setLocation("城市书店");
        request.setContent("更新后的内容。");
        request.setImageUrl("/uploads/images/updated.jpg");
        request.setVisibility("PRIVATE");
        request.setTags(List.of("阅读", "聊天"));
        request.setRemark("更新后的备注");
        return request;
    }
}
