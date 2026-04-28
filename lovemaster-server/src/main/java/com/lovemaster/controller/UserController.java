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
