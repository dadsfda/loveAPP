package com.lovemaster.controller;

import com.lovemaster.dto.request.CreatePreferenceRequest;
import com.lovemaster.dto.request.UpdatePreferenceRequest;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.dto.response.PreferenceResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.PreferenceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/preferences")
@RequiredArgsConstructor
public class PreferenceController {

    private final PreferenceService preferenceService;

    @PostMapping
    public ApiResponse<PreferenceResponse> create(@AuthenticationPrincipal User user,
                                                  @Valid @RequestBody CreatePreferenceRequest request) {
        return ApiResponse.success("创建成功", preferenceService.create(user.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<PreferenceResponse>> list(@AuthenticationPrincipal User user,
                                                      @RequestParam(required = false) String category,
                                                      @RequestParam(required = false) String visibility) {
        return ApiResponse.success(preferenceService.list(user.getId(), category, visibility));
    }

    @GetMapping("/{id}")
    public ApiResponse<PreferenceResponse> get(@AuthenticationPrincipal User user,
                                               @PathVariable Long id) {
        return ApiResponse.success(preferenceService.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<PreferenceResponse> update(@AuthenticationPrincipal User user,
                                                  @PathVariable Long id,
                                                  @Valid @RequestBody UpdatePreferenceRequest request) {
        return ApiResponse.success("更新成功", preferenceService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User user,
                                    @PathVariable Long id) {
        preferenceService.delete(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }
}
