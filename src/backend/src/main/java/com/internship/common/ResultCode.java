package com.internship.common;

import lombok.AllArgsConstructor;
import lombok.Getter;

/**
 * 响应状态码枚举
 * 定义系统所有业务状态码，统一管理错误码与提示信息
 */
@Getter
@AllArgsConstructor
public enum ResultCode {

    /** 成功 */
    SUCCESS(200, "操作成功"),
    /** 通用失败 */
    FAIL(500, "操作失败"),
    /** 未认证 */
    UNAUTHORIZED(401, "未认证，请先登录"),
    /** 无权限 */
    FORBIDDEN(403, "没有相关权限"),
    /** 资源不存在 */
    NOT_FOUND(404, "资源不存在"),
    /** 请求参数错误 */
    BAD_REQUEST(400, "请求参数错误"),
    /** Token已过期 */
    TOKEN_EXPIRED(4011, "Token已过期"),
    /** Token无效 */
    TOKEN_INVALID(4012, "Token无效"),
    /** 用户不存在 */
    USER_NOT_FOUND(1001, "用户不存在"),
    /** 密码错误 */
    USER_PASSWORD_ERROR(1002, "密码错误"),
    /** 用户已禁用 */
    USER_DISABLED(1003, "用户已被禁用");

    /** 状态码 */
    private final int code;

    /** 提示信息 */
    private final String message;
}
