package com.lovemaster.controller;

import com.lovemaster.dto.request.AiCommunicationAdviceRequest;
import com.lovemaster.dto.response.AiCommunicationAdviceResponse;
import com.lovemaster.dto.response.ApiResponse;
import com.lovemaster.service.AiAdviceService;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/api/v1/ai")
@RequiredArgsConstructor
public class AiController {

    private final AiAdviceService aiAdviceService;

    @PostMapping("/communication-advice")
    public ApiResponse<AiCommunicationAdviceResponse> generateCommunicationAdvice(
            @Valid @RequestBody AiCommunicationAdviceRequest request) {
        return ApiResponse.success("生成成功", aiAdviceService.generate(request));
    }
}
