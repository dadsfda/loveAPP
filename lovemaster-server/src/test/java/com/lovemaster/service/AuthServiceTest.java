package com.lovemaster.service;

import com.lovemaster.dto.request.LoginRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.TokenResponse;
import com.lovemaster.dto.response.UserResponse;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.security.JwtUtil;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.mock.mockito.MockBean;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.data.redis.core.ValueOperations;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.test.context.ActiveProfiles;
import org.springframework.transaction.annotation.Transactional;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@SpringBootTest
@ActiveProfiles("test")
@Transactional
class AuthServiceTest {

    @Autowired
    private AuthService authService;

    @Autowired
    private UserService userService;

    @Autowired
    private PasswordEncoder passwordEncoder;

    @Autowired
    private JwtUtil jwtUtil;

    @MockBean
    private StringRedisTemplate redisTemplate;

    private ValueOperations<String, String> valueOperations;

    private RegisterRequest registerRequest;

    @BeforeEach
    void setUp() {
        valueOperations = mock(ValueOperations.class);
        when(redisTemplate.opsForValue()).thenReturn(valueOperations);

        registerRequest = new RegisterRequest();
        registerRequest.setUsername("testuser");
        registerRequest.setPassword("password123");
        registerRequest.setNickname("测试用户");
        registerRequest.setPhone("13800138000");
        registerRequest.setEmail("test@example.com");
    }

    @Test
    @DisplayName("用户注册 - 成功")
    void testRegisterSuccess() {
        UserResponse response = authService.register(registerRequest);

        assertNotNull(response.getId());
        assertEquals("testuser", response.getUsername());
        assertEquals("测试用户", response.getNickname());
        assertEquals("13800138000", response.getPhone());
        assertEquals("test@example.com", response.getEmail());
        assertNotNull(response.getPairCode());
        assertEquals(8, response.getPairCode().length());
    }

    @Test
    @DisplayName("用户注册 - 用户名已存在")
    void testRegisterUsernameExists() {
        authService.register(registerRequest);

        RegisterRequest request2 = new RegisterRequest();
        request2.setUsername("testuser");
        request2.setPassword("password456");

        assertThrows(BusinessException.class, () -> authService.register(request2));
    }

    @Test
    @DisplayName("用户登录 - 成功")
    void testLoginSuccess() {
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        TokenResponse response = authService.login(loginRequest);

        assertNotNull(response.getAccessToken());
        assertNotNull(response.getRefreshToken());
        assertEquals("Bearer", response.getTokenType());
        assertTrue(jwtUtil.validateToken(response.getAccessToken()));
    }

    @Test
    @DisplayName("用户登录 - 密码错误")
    void testLoginWrongPassword() {
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("wrongpassword");

        assertThrows(BusinessException.class, () -> authService.login(loginRequest));
    }

    @Test
    @DisplayName("刷新Token - 成功")
    void testRefreshTokenSuccess() {
        authService.register(registerRequest);

        LoginRequest loginRequest = new LoginRequest();
        loginRequest.setUsername("testuser");
        loginRequest.setPassword("password123");

        TokenResponse tokens = authService.login(loginRequest);
        Long userId = jwtUtil.getUserIdFromToken(tokens.getRefreshToken());
        when(valueOperations.get("refresh_token:" + userId)).thenReturn(tokens.getRefreshToken());

        TokenResponse newTokens = authService.refreshToken(tokens.getRefreshToken());

        assertNotNull(newTokens.getAccessToken());
        assertNotNull(newTokens.getRefreshToken());
        assertNotEquals(tokens.getAccessToken(), newTokens.getAccessToken());
    }
}
