package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.util.List;

@Data
public class UpdatePreferenceRequest {

    @NotBlank(message = "记录对象不能为空")
    private String target;

    @NotBlank(message = "分类不能为空")
    private String category;

    @NotBlank(message = "内容不能为空")
    @Size(max = 500, message = "内容最多500个字符")
    private String content;

    @NotBlank(message = "可见性不能为空")
    private String visibility;

    private List<String> tags;

    @Size(max = 500, message = "备注最多500个字符")
    private String remark;
}
