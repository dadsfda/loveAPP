package com.lovemaster.exception;

import lombok.Getter;

@Getter
public enum ErrorCode {

    SUCCESS(200, "操作成功"),
    BAD_REQUEST(400, "请求参数错误"),
    UNAUTHORIZED(401, "未授权"),
    FORBIDDEN(403, "禁止访问"),
    NOT_FOUND(404, "资源不存在"),
    INTERNAL_ERROR(500, "服务器内部错误"),

    // 用户相关 1000-1099
    USER_NOT_FOUND(1001, "用户不存在"),
    USER_ALREADY_EXISTS(1002, "用户名已存在"),
    PASSWORD_ERROR(1003, "密码错误"),
    PHONE_ALREADY_EXISTS(1004, "手机号已注册"),
    EMAIL_ALREADY_EXISTS(1005, "邮箱已注册"),

    // 认证相关 1100-1199
    TOKEN_INVALID(1101, "Token无效"),
    TOKEN_EXPIRED(1102, "Token已过期"),
    REFRESH_TOKEN_INVALID(1103, "Refresh Token无效"),

    // 配对相关 1200-1299
    PAIR_CODE_INVALID(1201, "邀请码无效"),
    CANNOT_PAIR_SELF(1202, "不能和自己配对"),
    USER_ALREADY_PAIRED(1203, "当前用户已配对"),
    PARTNER_ALREADY_PAIRED(1204, "对方已配对"),
    PAIRING_NOT_FOUND(1205, "当前没有配对关系"),

    // 纪念日相关 1300-1399
    ANNIVERSARY_NOT_FOUND(1301, "纪念日不存在"),
    ANNIVERSARY_ACCESS_DENIED(1302, "无权访问该纪念日"),
    ANNIVERSARY_VISIBILITY_INVALID(1303, "纪念日可见性不合法"),
    ANNIVERSARY_TYPE_INVALID(1304, "纪念日类型不合法"),
    SURPRISE_MODE_VISIBILITY_CONFLICT(1305, "惊喜模式只能用于个人可见纪念日"),

    // 喜好清单相关 1400-1499
    PREFERENCE_NOT_FOUND(1401, "喜好记录不存在"),
    PREFERENCE_ACCESS_DENIED(1402, "无权访问该喜好记录"),
    PREFERENCE_VISIBILITY_INVALID(1403, "喜好记录可见性不合法"),
    PREFERENCE_CATEGORY_INVALID(1404, "喜好记录分类不合法"),
    PREFERENCE_TARGET_INVALID(1405, "喜好记录对象不合法"),

    // 约会灵感相关 1500-1599
    DATE_IDEA_NOT_FOUND(1501, "约会灵感不存在"),

    // 回忆收藏夹相关 1600-1699
    MEMORY_NOT_FOUND(1601, "回忆不存在"),
    MEMORY_ACCESS_DENIED(1602, "无权访问该回忆"),
    MEMORY_VISIBILITY_INVALID(1603, "回忆可见性不合法"),

    // 文件上传相关 1700-1799
    FILE_EMPTY(1701, "上传文件不能为空"),
    FILE_TYPE_NOT_ALLOWED(1702, "不支持的文件类型"),
    FILE_SIZE_EXCEEDED(1703, "上传文件大小超出限制"),
    FILE_SAVE_FAILED(1704, "文件保存失败"),

    // AI相关 1800-1899
    AI_DISABLED(1801, "AI服务未启用"),
    AI_API_KEY_MISSING(1802, "AI API Key未配置"),
    AI_PROVIDER_ERROR(1803, "AI服务调用失败"),
    AI_RESPONSE_INVALID(1804, "AI响应格式异常");

    private final Integer code;
    private final String message;

    ErrorCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}
