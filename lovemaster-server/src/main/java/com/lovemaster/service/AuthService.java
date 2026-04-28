package com.lovemaster.service;

import com.lovemaster.dto.request.LoginRequest;
import com.lovemaster.dto.request.RegisterRequest;
import com.lovemaster.dto.response.TokenResponse;
import com.lovemaster.dto.response.UserResponse;

public interface AuthService {

    UserResponse register(RegisterRequest request);

    TokenResponse login(LoginRequest request);

    TokenResponse refreshToken(String refreshToken);

    void logout(Long userId);
}
