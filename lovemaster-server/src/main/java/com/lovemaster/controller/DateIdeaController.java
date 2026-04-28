package com.lovemaster.controller;

import com.lovemaster.dto.request.DateIdeaQueryRequest;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.dto.response.DateIdeaResponse;
import com.lovemaster.entity.User;
import com.lovemaster.service.DateIdeaService;
import lombok.RequiredArgsConstructor;
import org.springframework.security.core.annotation.AuthenticationPrincipal;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;
import java.util.stream.Stream;

@RestController
@RequestMapping("/api/v1/date-ideas")
@RequiredArgsConstructor
public class DateIdeaController {

    private final DateIdeaService dateIdeaService;

    @GetMapping
    public ApiResponse<List<DateIdeaResponse>> list(@RequestParam(required = false) String budgetLevel,
                                                    @RequestParam(required = false) String durationLevel,
                                                    @RequestParam(required = false) String scene,
                                                    @RequestParam(required = false) List<String> tags) {
        return ApiResponse.success(dateIdeaService.list(query(budgetLevel, durationLevel, scene, tags)));
    }

    @GetMapping("/recommend")
    public ApiResponse<List<DateIdeaResponse>> recommend(@AuthenticationPrincipal User user,
                                                         @RequestParam(required = false) String budgetLevel,
                                                         @RequestParam(required = false) String durationLevel,
                                                         @RequestParam(required = false) String scene,
                                                         @RequestParam(required = false) List<String> tags) {
        return ApiResponse.success(dateIdeaService.recommend(user.getId(), query(budgetLevel, durationLevel, scene, tags)));
    }

    private DateIdeaQueryRequest query(String budgetLevel, String durationLevel, String scene, List<String> tags) {
        DateIdeaQueryRequest request = new DateIdeaQueryRequest();
        request.setBudgetLevel(budgetLevel);
        request.setDurationLevel(durationLevel);
        request.setScene(scene);
        request.setTags(parseTags(tags));
        return request;
    }

    private List<String> parseTags(List<String> tags) {
        if (tags == null || tags.isEmpty()) {
            return List.of();
        }
        return tags.stream()
                .flatMap(value -> Stream.of(value.split(",")))
                .map(String::trim)
                .filter(value -> !value.isBlank())
                .distinct()
                .collect(Collectors.toList());
    }
}
