package com.blog.base.common.api;

import lombok.Getter;

/**
 * Unified error codes.
 */
@Getter
public enum ErrorCode {

    OK(0, "success"),
    SYSTEM_ERROR(10000, "系统异常"),
    PARAM_ERROR(10001, "参数错误"),
    DATA_NOT_FOUND(10002, "数据不存在"),
    DUPLICATE_NAME(10003, "名称已存在"),
    STATE_ERROR(10004, "状态不允许该操作");

    private final int code;
    private final String message;

    ErrorCode(int code, String message) {
        this.code = code;
        this.message = message;
    }
}
