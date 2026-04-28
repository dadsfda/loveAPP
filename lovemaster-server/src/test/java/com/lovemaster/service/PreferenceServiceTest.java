package com.lovemaster.service;

import com.lovemaster.dto.request.BindPairRequest;
import com.lovemaster.dto.request.CreatePreferenceRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.request.UpdatePreferenceRequest;
import com.lovemaster.dto.response.PreferenceResponse;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
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
    private PairingService pairingService;

    @Autowired
    private PreferenceService preferenceService;

    @Test
    @DisplayName("用户可以创建个人可见喜好记录")
    void createPrivatePreferenceShouldSucceed() {
        User user = register("pref_user_private");
        CreatePreferenceRequest request = createRequest("SELF", "HOBBY", "PRIVATE", "喜欢散步和拍照");

        PreferenceResponse response = preferenceService.create(user.getId(), request);

        assertNotNull(response.getId());
        assertEquals("HOBBY", response.getCategory());
        assertEquals("PRIVATE", response.getVisibility());
        assertEquals(List.of("拍照", "散步"), response.getTags());
    }

    @Test
    @DisplayName("未配对用户不能创建双方可见喜好记录")
    void createCouplePreferenceWithoutPairingShouldFail() {
        User user = register("pref_user_unpaired");
        CreatePreferenceRequest request = createRequest("SELF", "GIFT", "COUPLE", "喜欢实用型礼物");

        assertThrows(BusinessException.class, () -> preferenceService.create(user.getId(), request));
    }

    @Test
    @DisplayName("配对后伴侣可以查看双方可见喜好记录，不能查看个人记录")
    void listShouldRespectVisibility() {
        User user = register("pref_user_list_a");
        User partner = register("pref_user_list_b");
        pair(user, partner);

        PreferenceResponse couplePreference = preferenceService.create(user.getId(),
                createRequest("SELF", "FAVORITE_FOOD", "COUPLE", "喜欢清淡粤菜"));
        PreferenceResponse privatePreference = preferenceService.create(user.getId(),
                createRequest("SELF", "LIFE_BOUNDARY", "PRIVATE", "不喜欢临时改计划"));

        List<PreferenceResponse> partnerVisibleList = preferenceService.list(partner.getId(), null, null);

        assertTrue(partnerVisibleList.stream().anyMatch(item -> item.getId().equals(couplePreference.getId())));
        assertTrue(partnerVisibleList.stream().noneMatch(item -> item.getId().equals(privatePreference.getId())));
        assertThrows(BusinessException.class, () -> preferenceService.get(partner.getId(), privatePreference.getId()));
    }

    @Test
    @DisplayName("只有创建者可以更新或删除喜好记录")
    void updateAndDeleteShouldRequireCreator() {
        User user = register("pref_user_update_a");
        User partner = register("pref_user_update_b");
        pair(user, partner);
        PreferenceResponse created = preferenceService.create(user.getId(),
                createRequest("PARTNER_OBSERVED", "GIFT", "COUPLE", "喜欢手写卡片"));

        assertThrows(BusinessException.class, () -> preferenceService.update(partner.getId(), created.getId(), updateRequest("被篡改")));
        PreferenceResponse updated = preferenceService.update(user.getId(), created.getId(), updateRequest("喜欢有纪念意义的小礼物"));

        assertEquals("喜欢有纪念意义的小礼物", updated.getContent());
        assertThrows(BusinessException.class, () -> preferenceService.delete(partner.getId(), created.getId()));
        preferenceService.delete(user.getId(), created.getId());
        assertThrows(BusinessException.class, () -> preferenceService.get(user.getId(), created.getId()));
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

    private CreatePreferenceRequest createRequest(String target, String category, String visibility, String content) {
        CreatePreferenceRequest request = new CreatePreferenceRequest();
        request.setTarget(target);
        request.setCategory(category);
        request.setContent(content);
        request.setVisibility(visibility);
        request.setTags(List.of("散步", "拍照"));
        request.setRemark("测试备注");
        return request;
    }

    private UpdatePreferenceRequest updateRequest(String content) {
        UpdatePreferenceRequest request = new UpdatePreferenceRequest();
        request.setTarget("SELF");
        request.setCategory("CUSTOM");
        request.setContent(content);
        request.setVisibility("PRIVATE");
        request.setTags(List.of("礼物", "纪念"));
        request.setRemark("更新后的备注");
        return request;
    }
}
