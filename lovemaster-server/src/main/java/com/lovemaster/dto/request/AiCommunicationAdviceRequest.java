package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class AiCommunicationAdviceRequest {

    @NotBlank(message = "场景描述不能为空")
    @Size(max = 500, message = "场景描述不能超过500字符")
    private String scenario;

    @Size(max = 200, message = "我的感受不能超过200字符")
    private String myFeeling;

    @Size(max = 200, message = "对方可能的感受不能超过200字符")
    private String partnerFeeling;

    @Size(max = 200, message = "沟通目标不能超过200字符")
    private String goal;
}
