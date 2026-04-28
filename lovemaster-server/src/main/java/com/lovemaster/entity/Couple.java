package com.lovemaster.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("couple")
public class Couple {

    @TableId(type = IdType.AUTO)
    private Long id;

    @TableField("user_id_1")
    private Long userId1;

    @TableField("user_id_2")
    private Long userId2;

    private Integer status;

    @TableField("paired_at")
    private LocalDateTime pairedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
