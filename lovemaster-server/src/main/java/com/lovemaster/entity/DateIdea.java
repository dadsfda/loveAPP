package com.lovemaster.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("date_idea")
public class DateIdea {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String title;

    @TableField("budget_level")
    private String budgetLevel;

    @TableField("duration_level")
    private String durationLevel;

    private String scene;

    @TableField("interest_tags")
    private String interestTags;

    private String steps;

    private String tips;

    private Boolean enabled;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
