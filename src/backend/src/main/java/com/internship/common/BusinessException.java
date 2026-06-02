package com.internship.common;

import lombok.Getter;

/**
 * 业务异常类
 * 用于在业务逻辑中主动抛出可预期的异常，由GlobalExceptionHandler统一捕获处理
 */
@Getter
public class BusinessException extends RuntimeException {

    /** 业务错误码 */
    private final int code;

    /**
     * 使用自定义消息构造，错误码默认为FAIL
     *
     * @param message 异常提示信息
     */
    public BusinessException(String message) {
        super(message);
        this.code = ResultCode.FAIL.getCode();
    }

    /**
     * 使用ResultCode枚举构造
     *
     * @param resultCode 错误码枚举
     */
    public BusinessException(ResultCode resultCode) {
        super(resultCode.getMessage());
        this.code = resultCode.getCode();
    }

    /**
     * 使用自定义错误码和消息构造
     *
     * @param code    错误码
     * @param message 异常提示信息
     */
    public BusinessException(int code, String message) {
        super(message);
        this.code = code;
    }
}
