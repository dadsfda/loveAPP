package com.lovemaster.service.impl;

import com.lovemaster.dto.request.LoginRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.TokenResponse;
import com.lovemaster.dto.response.UserResponse;
import com.lovemaster.entity.User;
import com.lovemaster.exception.BusinessException;
import com.lovemaster.exception.ErrorCode;
import com.lovemaster.security.JwtUtil;
import com.lovemaster.service.AuthService;
import com.lovemaster.service.UserService;
import lombok.RequiredArgsConstructor;
import org.springframework.data.redis.core.StringRedisTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.UUID;
import java.util.concurrent.TimeUnit;

@Service
@RequiredArgsConstructor
public class AuthServiceImpl implements AuthService {

    private final UserService userService;
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final StringRedisTemplate redisTemplate;

    private static final String REFRESH_TOKEN_KEY_PREFIX = "refresh_token:";
    private static final String ACCESS_TOKEN_BLACKLIST_PREFIX = "token_blacklist:";

    @Override
    @Transactional
    public UserResponse register(RegisterRequest request) {
        // 校验用户名是否已存在
        if (userService.existsByUsername(request.getUsername())) {
            throw new BusinessException(ErrorCode.USER_ALREADY_EXISTS);
        }

        // 校验手机号是否已存在
        if (request.getPhone() != null && !request.getPhone().isEmpty()) {
            if (userService.existsByPhone(request.getPhone())) {
                throw new BusinessException(ErrorCode.PHONE_ALREADY_EXISTS);
            }
        }

        // 校验邮箱是否已存在
        if (request.getEmail() != null && !request.getEmail().isEmpty()) {
            if (userService.existsByEmail(request.getEmail())) {
                throw new BusinessException(ErrorCode.EMAIL_ALREADY_EXISTS);
            }
        }

        // 创建用户
        User user = new User();
        user.setUsername(request.getUsername());
        user.setPasswordHash(passwordEncoder.encode(request.getPassword()));
        user.setNickname(request.getNickname() != null ? request.getNickname() : request.getUsername());
        user.setPhone(request.getPhone());
        user.setEmail(request.getEmail());
        user.setStatus(1);
        user.setGender(0);

        userService.save(user);

        // 生成邀请码
        String pairCode = generatePairCode();
        user.setPairCode(pairCode);
        userService.update(user);

        return UserResponse.fromEntity(user);
    }

    @Override
    public TokenResponse login(LoginRequest request) {
        // 查找用户
        User user = userService.findByUsername(request.getUsername());
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 验证密码
        if (!passwordEncoder.matches(request.getPassword(), user.getPasswordHash())) {
            throw new BusinessException(ErrorCode.PASSWORD_ERROR);
        }

        // 检查用户状态
        if (user.getStatus() != 1) {
            throw new BusinessException(ErrorCode.FORBIDDEN, "用户已被禁用");
        }

        // 生成 Token
        String accessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String refreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 存储 Refresh Token 到 Redis
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + user.getId();
        redisTemplate.opsForValue().set(refreshKey, refreshToken, 7, TimeUnit.DAYS);

        return TokenResponse.of(accessToken, refreshToken, jwtUtil.getAccessTokenExpiration());
    }

    @Override
    public TokenResponse refreshToken(String refreshToken) {
        // 验证 Token
        if (!jwtUtil.validateToken(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        Long userId = jwtUtil.getUserIdFromToken(refreshToken);

        // 检查 Redis 中的 Refresh Token
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + userId;
        String storedToken = redisTemplate.opsForValue().get(refreshKey);

        if (storedToken == null || !storedToken.equals(refreshToken)) {
            throw new BusinessException(ErrorCode.REFRESH_TOKEN_INVALID);
        }

        // 查找用户
        User user = userService.findById(userId);
        if (user == null) {
            throw new BusinessException(ErrorCode.USER_NOT_FOUND);
        }

        // 生成新的 Token
        String newAccessToken = jwtUtil.generateAccessToken(user.getId(), user.getUsername());
        String newRefreshToken = jwtUtil.generateRefreshToken(user.getId());

        // 更新 Redis 中的 Refresh Token
        redisTemplate.opsForValue().set(refreshKey, newRefreshToken, 7, TimeUnit.DAYS);

        return TokenResponse.of(newAccessToken, newRefreshToken, jwtUtil.getAccessTokenExpiration());
    }

    @Override
    public void logout(Long userId) {
        // 删除 Redis 中的 Refresh Token
        String refreshKey = REFRESH_TOKEN_KEY_PREFIX + userId;
        redisTemplate.delete(refreshKey);
    }

    private String generatePairCode() {
        return UUID.randomUUID().toString().substring(0, 8).toUpperCase();
    }
}
