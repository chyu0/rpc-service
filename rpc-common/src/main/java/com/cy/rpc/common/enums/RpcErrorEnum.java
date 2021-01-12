package com.cy.rpc.common.enums;

import lombok.Getter;

/**
 * @author chenyu3
 * rpc 错误枚举
 */
public enum RpcErrorEnum {

    INNER_ERROR(10001, "服务器内部错误");

    RpcErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Getter
    private int code;

    @Getter
    private String message;

}
