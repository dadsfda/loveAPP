package com.lovemaster.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("anniversary")
public class Anniversary {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("creator_id")
    private Long creatorId;

    @TableField("couple_id")
    private Long coupleId;

    private String title;

    private LocalDate date;

    private String type;

    private String visibility;

    @TableField("remind_days")
    private String remindDays;

    @TableField("surprise_mode")
    private Boolean surpriseMode;

    private String remark;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
