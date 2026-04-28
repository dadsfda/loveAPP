package com.lovemaster.controller;

import com.lovemaster.dto.request.CreateMemoryRequest;
import com.lovemaster.dto.request.UpdateMemoryRequest;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.dto.response.MemoryResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.MemoryService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.format.annotation.DateTimeFormat;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.util.List;

@RestController
@RequestMapping("/api/v1/memories")
@RequiredArgsConstructor
public class MemoryController {

    private final MemoryService memoryService;

    @PostMapping
    public ApiResponse<MemoryResponse> create(@AuthenticationPrincipal User user,
                                              @Valid @RequestBody CreateMemoryRequest request) {
        return ApiResponse.success("创建成功", memoryService.create(user.getId(), request));
    }

    @GetMapping
    public ApiResponse<List<MemoryResponse>> list(@AuthenticationPrincipal User user,
                                                  @RequestParam(required = false) String visibility,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate startDate,
                                                  @RequestParam(required = false) @DateTimeFormat(iso = DateTimeFormat.ISO.DATE) LocalDate endDate,
                                                  @RequestParam(required = false) String tag) {
        return ApiResponse.success(memoryService.list(user.getId(), visibility, startDate, endDate, tag));
    }

    @GetMapping("/{id}")
    public ApiResponse<MemoryResponse> get(@AuthenticationPrincipal User user,
                                           @PathVariable Long id) {
        return ApiResponse.success(memoryService.get(user.getId(), id));
    }

    @PutMapping("/{id}")
    public ApiResponse<MemoryResponse> update(@AuthenticationPrincipal User user,
                                              @PathVariable Long id,
                                              @Valid @RequestBody UpdateMemoryRequest request) {
        return ApiResponse.success("更新成功", memoryService.update(user.getId(), id, request));
    }

    @DeleteMapping("/{id}")
    public ApiResponse<Void> delete(@AuthenticationPrincipal User user,
                                    @PathVariable Long id) {
        memoryService.delete(user.getId(), id);
        return ApiResponse.success("删除成功", null);
    }
}
