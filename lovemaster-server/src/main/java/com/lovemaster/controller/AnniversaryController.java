package com.lovemaster.controller;

import com.lovemaster.dto.request.CreateAnniversaryRequest;
import com.lovemaster.dto.request.UpdateAnniversaryRequest;
import com.lovemaster.dto.response.AnniversaryResponse;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.AnniversaryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/v1/anniversaries")
@RequiredArgsConstructor
public class AnniversaryController {

    private final AnniversaryService anniversaryService;

    @PostMapping
    public ApiResponse<AnniversaryResponse> create(@AuthenticationPrincipal User user,
                                                   @Valid @RequestBody CreateAnniversaryRequest request) {
        return ApiResponse.success("创建成功", anniversaryService.create(user.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<AnniversaryResponse>> list(@AuthenticationPrincipal User user,
                                                       @RequestParam(required = false) String type,
                                                       @RequestParam(required = false) String visibility) {
        return ApiResponse.success(anniversaryService.list(user.getId(), type, visibility));
    }

    @GetMapping("/{id}")
    public ApiResponse<AnniversaryResponse> get(@AuthenticationPrincipal User user,
                                                @PathVariable Long id) {
        return ApiResponse.success(anniversaryService.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<AnniversaryResponse> update(@AuthenticationPrincipal User user,
                                                   @PathVariable Long id,
                                                   @Valid @RequestBody UpdateAnniversaryRequest request) {
        return ApiResponse.success("更新成功", anniversaryService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User user,
                                    @PathVariable Long id) {
        anniversaryService.delete(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }

    @GetMapping("/reminders")
    public ApiResponse<List<AnniversaryResponse>> reminders(@AuthenticationPrincipal User user,
                                                            @RequestParam(required = false) Integer days) {
        return ApiResponse.success(anniversaryService.reminders(user.getId(), days));
    }
}
