package com.cy.rpc.register.exception;

import lombok.Getter;

/**
 * @author chenyu3
 * rpc 错误枚举
 */
public enum ZkErrorEnum {

    INNER_ERROR(50000, "服务器内部错误"),
    APP_NAME_NOT_FOUND(50001, "zk连接异常，未找到appName");

    ZkErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Getter
    private int code;

    @Getter
    private String message;

}
