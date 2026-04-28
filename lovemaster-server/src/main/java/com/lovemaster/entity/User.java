package com.lovemaster.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@TableName("user")
public class User {

    @TableId(type = IdType.AUTO)
    private Long id;

    private String username;

    private String nickname;

    private String avatar;

    @TableField("password_hash")
    private String passwordHash;

    private String phone;

    private String email;

    private Integer gender;

    private Integer status;

    @TableField("pair_code")
    private String pairCode;

    @TableField("partner_id")
    private Long partnerId;

    @TableField("paired_at")
    private LocalDateTime pairedAt;

    @TableField(value = "created_at", fill = FieldFill.INSERT)
    private LocalDateTime createdAt;

    @TableField(value = "updated_at", fill = FieldFill.INSERT_UPDATE)
    private LocalDateTime updatedAt;

    @TableLogic
    private Integer deleted;
}
