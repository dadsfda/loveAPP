package com.lovemaster.controller;

import com.lovemaster.dto.request.LoginRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.dto.response.TokenResponse;
import com.lovemaster.dto.response.UserResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.AuthService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/v1/auth")
@RequiredArgsConstructor
public class AuthController {

    private final AuthService authService;

    @PostMapping("/register")
    public ApiResponse<UserResponse> register(@Valid @RequestBody RegisterRequest request) {
        UserResponse user = authService.register(request);
        return ApiResponse.success("注册成功", user);
    }

    @PostMapping("/login")
    public ApiResponse<TokenResponse> login(@Valid @RequestBody LoginRequest request) {
        TokenResponse tokens = authService.login(request);
        return ApiResponse.success("登录成功", tokens);
    }

    @PostMapping("/refresh")
    public ApiResponse<TokenResponse> refresh(@RequestBody TokenRequest request) {
        TokenResponse tokens = authService.refreshToken(request.getRefreshToken());
        return ApiResponse.success("刷新成功", tokens);
    }

    @PostMapping("/logout")
    public ApiResponse<Void> logout(@AuthenticationPrincipal User user) {
        if (user != null) {
            authService.logout(user.getId());
        }
        return ApiResponse.success("退出成功", null);
    }

    // 内部类用于接收 refresh token
    @lombok.Data
    public static class TokenRequest {
        private String refreshToken;
    }
}
