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
