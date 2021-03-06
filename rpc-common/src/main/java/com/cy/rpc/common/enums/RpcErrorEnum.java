package com.cy.rpc.common.enums;

import lombok.Getter;

/**
 * @author chenyu3
 * rpc 错误枚举
 */
public enum RpcErrorEnum {

    INNER_ERROR(10001, "服务器内部错误"),
    CLIENT_NOT_FOUND(10002, "客户端为找到"),
    CLIENT_CLUSTER_NOT_FOUND(10003, "未找到对应集群客户端"),
    REMOTE_CONNECTION_CLOSED(10004, "远程连接已关闭"),
    SERVICE_NAME_NO_MATCH(10005, "没有匹配的serviceName"),
    PRC_SERVICE_IS_NULL(10006, "rpc service为空"),
    CALL_TIME_OUT(10007, "接口调用超时"),
    CALL_EXCEPTION(10008, "服务调用异常"),
    SERVICE_NAME_NOT_FOUND(10009, "服务未找到"),
    ;

    RpcErrorEnum(int code, String message) {
        this.code = code;
        this.message = message;
    }

    @Getter
    private int code;

    @Getter
    private String message;

}
