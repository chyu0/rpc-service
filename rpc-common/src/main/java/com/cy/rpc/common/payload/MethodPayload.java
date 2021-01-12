package com.cy.rpc.common.payload;

import lombok.Getter;
import lombok.Setter;

@Getter
@Setter
public class MethodPayload {

    //服务名称
    private String serviceName;

    //方法
    private String method;

    //参数
    private Object[] args;

    //参数类型
    private Class<?>[] argsClass;

    //请求id
    private String requestId;

}
