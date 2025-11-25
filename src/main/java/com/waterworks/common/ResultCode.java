package com.waterworks.common;

import lombok.Getter;

/**
 * 响应码枚举
 */
@Getter
public enum ResultCode {

    SUCCESS(200, "操作成功"),
    ERROR(500, "操作失败"),
    
    // 参数相关 1xxx
    PARAM_IS_INVALID(1001, "参数无效"),
    PARAM_IS_BLANK(1002, "参数为空"),
    PARAM_TYPE_ERROR(1003, "参数类型错误"),
    PARAM_NOT_COMPLETE(1004, "参数缺失"),
    
    // 用户相关 2xxx
    USER_NOT_LOGIN(2001, "用户未登录"),
    USER_LOGIN_ERROR(2002, "账号或密码错误"),
    USER_ACCOUNT_FORBIDDEN(2003, "账号已被禁用"),
    USER_NOT_EXIST(2004, "用户不存在"),
    USER_HAS_EXISTED(2005, "用户已存在"),
    USER_PASSWORD_ERROR(2006, "密码错误"),
    
    // 业务相关 3xxx
    BUSINESS_ERROR(3001, "业务异常"),
    
    // 权限相关 4xxx
    NO_PERMISSION(4001, "没有权限"),
    TOKEN_INVALID(4002, "Token无效"),
    TOKEN_EXPIRED(4003, "Token已过期"),
    
    // 数据相关 5xxx
    DATA_NOT_EXIST(5001, "数据不存在"),
    DATA_IS_WRONG(5002, "数据错误"),
    DATA_ALREADY_EXISTED(5003, "数据已存在"),
    
    // 接口相关 6xxx
    INTERFACE_INNER_INVOKE_ERROR(6001, "内部接口调用异常"),
    INTERFACE_OUTER_INVOKE_ERROR(6002, "外部接口调用异常"),
    INTERFACE_FORBID_VISIT(6003, "接口禁止访问"),
    INTERFACE_ADDRESS_INVALID(6004, "接口地址无效"),
    INTERFACE_REQUEST_TIMEOUT(6005, "接口请求超时"),
    INTERFACE_EXCEED_LOAD(6006, "接口负载过高");

    private final Integer code;
    private final String message;

    ResultCode(Integer code, String message) {
        this.code = code;
        this.message = message;
    }
}


