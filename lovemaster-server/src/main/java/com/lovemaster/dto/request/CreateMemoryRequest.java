package com.lovemaster.dto.request;

import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

import java.time.LocalDate;
import java.util.List;

@Data
public class CreateMemoryRequest {

    @NotBlank(message = "标题不能为空")
    @Size(max = 80, message = "标题长度不能超过80字符")
    private String title;

    @NotNull(message = "回忆日期不能为空")
    private LocalDate memoryDate;

    @Size(max = 120, message = "地点长度不能超过120字符")
    private String location;

    @Size(max = 2000, message = "内容长度不能超过2000字符")
    private String content;

    @Size(max = 500, message = "图片URL长度不能超过500字符")
    private String imageUrl;

    @NotBlank(message = "可见性不能为空")
    private String visibility;

    private List<String> tags;

    @Size(max = 500, message = "备注长度不能超过500字符")
    private String remark;
}
