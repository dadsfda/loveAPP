package com.lovemaster.service;

import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
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

    @Autowired
    private PairingService pairingService;

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

    @Test
    @DisplayName("配对用户可以查看双方可见纪念日，不能查看对方个人纪念日")
    void listShouldRespectVisibility() {
        User user = register("ann_user_list_a");
        User partner = register("ann_user_list_b");
        pair(user, partner);

        AnniversaryResponse coupleAnniversary = anniversaryService.create(user.getId(),
                createRequest("恋爱纪念日", "LOVE_ANNIVERSARY", "COUPLE", LocalDate.now().plusDays(7)));
        AnniversaryResponse partnerPrivate = anniversaryService.create(partner.getId(),
                createRequest("秘密礼物", "CUSTOM", "PRIVATE", LocalDate.now().plusDays(1)));

        List<AnniversaryResponse> userVisibleList = anniversaryService.list(user.getId(), null, null);
        List<AnniversaryResponse> partnerVisibleList = anniversaryService.list(partner.getId(), null, null);

        assertTrue(partnerVisibleList.stream().anyMatch(item -> item.getId().equals(coupleAnniversary.getId())));
        assertTrue(partnerVisibleList.stream().anyMatch(item -> item.getId().equals(partnerPrivate.getId())));
        assertTrue(userVisibleList.stream().noneMatch(item -> item.getId().equals(partnerPrivate.getId())));
        assertThrows(BusinessException.class, () -> anniversaryService.get(user.getId(), partnerPrivate.getId()));
    }

    @Test
    @DisplayName("只有创建者可以更新或删除纪念日")
    void updateAndDeleteShouldRequireCreator() {
        User user = register("ann_user_update_a");
        User partner = register("ann_user_update_b");
        pair(user, partner);
        AnniversaryResponse created = anniversaryService.create(user.getId(),
                createRequest("恋爱纪念日", "LOVE_ANNIVERSARY", "COUPLE", LocalDate.now().plusDays(7)));

        assertThrows(BusinessException.class, () -> anniversaryService.update(partner.getId(), created.getId(), updateRequest("被篡改")));
        AnniversaryResponse updated = anniversaryService.update(user.getId(), created.getId(), updateRequest("第一个恋爱纪念日"));

        assertEquals("第一个恋爱纪念日", updated.getTitle());
        assertThrows(BusinessException.class, () -> anniversaryService.delete(partner.getId(), created.getId()));
        anniversaryService.delete(user.getId(), created.getId());
        assertThrows(BusinessException.class, () -> anniversaryService.get(user.getId(), created.getId()));
    }

    @Test
    @DisplayName("提醒列表只返回命中提醒天数的未来纪念日")
    void remindersShouldReturnMatchedFutureAnniversaries() {
        User user = register("ann_user_reminder");
        AnniversaryResponse matched = anniversaryService.create(user.getId(),
                createRequest("七天后提醒", "CUSTOM", "PRIVATE", LocalDate.now().plusDays(7)));
        anniversaryService.create(user.getId(),
                createRequest("三天后提醒", "CUSTOM", "PRIVATE", LocalDate.now().plusDays(3)));

        List<AnniversaryResponse> reminders = anniversaryService.reminders(user.getId(), 7);

        assertEquals(1, reminders.size());
        assertEquals(matched.getId(), reminders.get(0).getId());
    }

    private User register(String username) {
        RegisterRequest request = new RegisterRequest();
        request.setUsername(username);
        request.setPassword("password123");
        authService.register(request);
        return userService.findByUsername(username);
    }

    private void pair(User user, User partner) {
        com.lovemaster.dto.request.BindPairRequest request = new com.lovemaster.dto.request.BindPairRequest();
        request.setPairCode(partner.getPairCode());
        pairingService.bind(user.getId(), request);
    }

    private CreateAnniversaryRequest createRequest(String title, String type, String visibility, LocalDate date) {
        CreateAnniversaryRequest request = new CreateAnniversaryRequest();
        request.setTitle(title);
        request.setDate(date);
        request.setType(type);
        request.setVisibility(visibility);
        request.setRemindDays(List.of(0, 3, 7));
        request.setSurpriseMode(false);
        return request;
    }

    private UpdateAnniversaryRequest updateRequest(String title) {
        UpdateAnniversaryRequest request = new UpdateAnniversaryRequest();
        request.setTitle(title);
        request.setDate(LocalDate.now().plusDays(14));
        request.setType("CUSTOM");
        request.setVisibility("PRIVATE");
        request.setRemindDays(List.of(0, 1));
        request.setSurpriseMode(true);
        request.setRemark("更新后的备注");
        return request;
    }
}
