package com.internship.common;

import lombok.Data;

/**
 * 统一响应封装类
 * 所有接口返回值均使用此类包装，保证响应格式一致
 *
 * @param <T> 响应数据泛型
 */
@Data
public class Result<T> {

    /** 响应状态码 */
    private int code;

    /** 响应提示信息 */
    private String message;

    /** 响应数据 */
    private T data;

    private Result() {}

    /**
     * 构建成功响应（无数据）
     */
    public static <T> Result<T> success() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        return result;
    }

    /**
     * 构建成功响应（带数据）
     *
     * @param data 响应数据
     */
    public static <T> Result<T> success(T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(ResultCode.SUCCESS.getMessage());
        result.setData(data);
        return result;
    }

    /**
     * 构建成功响应（自定义消息和数据）
     *
     * @param message 提示信息
     * @param data    响应数据
     */
    public static <T> Result<T> success(String message, T data) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.SUCCESS.getCode());
        result.setMessage(message);
        result.setData(data);
        return result;
    }

    /**
     * 构建失败响应（默认失败信息）
     */
    public static <T> Result<T> fail() {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAIL.getCode());
        result.setMessage(ResultCode.FAIL.getMessage());
        return result;
    }

    /**
     * 构建失败响应（自定义消息）
     *
     * @param message 失败提示信息
     */
    public static <T> Result<T> fail(String message) {
        Result<T> result = new Result<>();
        result.setCode(ResultCode.FAIL.getCode());
        result.setMessage(message);
        return result;
    }

    /**
     * 构建失败响应（使用ResultCode枚举）
     *
     * @param resultCode 错误码枚举
     */
    public static <T> Result<T> fail(ResultCode resultCode) {
        Result<T> result = new Result<>();
        result.setCode(resultCode.getCode());
        result.setMessage(resultCode.getMessage());
        return result;
    }

    /**
     * 构建失败响应（自定义错误码和消息）
     *
     * @param code    错误码
     * @param message 失败提示信息
     */
    public static <T> Result<T> fail(int code, String message) {
        Result<T> result = new Result<>();
        result.setCode(code);
        result.setMessage(message);
        return result;
    }
}
