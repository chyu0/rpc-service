package com.cy.rpc.common.payload;

import lombok.Getter;
import lombok.Setter;

import java.util.Arrays;

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

    @Override
    public String toString() {
        return  "{ requestId = " + requestId + " ," +
                "method = " + method + " ," +
                "argClass = " + Arrays.toString(argsClass) + " ," +
                "args = " + Arrays.toString(args) + " ," +
                "serviceName = " + serviceName + " }";
    }
}
