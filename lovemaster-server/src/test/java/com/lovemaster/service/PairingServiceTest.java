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

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;
import static org.junit.jupiter.api.Assertions.assertThrows;
import static org.junit.jupiter.api.Assertions.assertTrue;

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
