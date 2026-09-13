package com.blog.base.common.exception;

import com.blog.base.common.api.ErrorCode;
import lombok.Getter;

/**
 * Business exception carrying an ErrorCode.
 */
@Getter
public class BizException extends RuntimeException {

    private final ErrorCode errorCode;

    public BizException(ErrorCode errorCode) {
        super(errorCode.getMessage());
        this.errorCode = errorCode;
    }

    public BizException(ErrorCode errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public static BizException notFound() {
        return new BizException(ErrorCode.DATA_NOT_FOUND);
    }
}
