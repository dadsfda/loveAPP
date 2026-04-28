package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class UpdateAnniversaryRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 50, message = "标题最多50个字符")
    private String title;

    @NotNull(message = "日期不能为空")
    private LocalDate date;

    @NotBlank(message = "类型不能为空")
    private String type;

    @NotBlank(message = "可见性不能为空")
    private String visibility;

    private List<Integer> remindDays;

    private Boolean surpriseMode = false;

    @Size(max = 500, message = "备注最多500个字符")
    private String remark;
}
