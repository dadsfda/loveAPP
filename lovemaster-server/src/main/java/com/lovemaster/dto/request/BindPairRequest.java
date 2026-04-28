package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
public class BindPairRequest {

    @NotBlank(message = "邀请码不能为空")
    @Size(min = 8, max = 20, message = "邀请码长度不正确")
    private String pairCode;
}
