package com.blog.base.api.dto;

import lombok.Data;

/**
 * Lightweight response envelope matching the service's Result structure,
 * kept inside base-api so consumers need no dependency on base-common.
 */
@Data
public class ApiResult<T> {

    private int code;
    private String message;
    private T data;

    public boolean isSuccess() {
        return code == 0;
    }
}
