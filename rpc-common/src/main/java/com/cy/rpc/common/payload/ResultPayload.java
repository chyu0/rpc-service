package com.cy.rpc.common.payload;

import lombok.Getter;
import lombok.Setter;

import java.io.Serializable;

@Getter
@Setter
public class ResultPayload implements Serializable {

    //请求id
    private String requestId;

    //返回结果
    private Object result;

    //错误编码
    private int code;

    //返回错误信息
    private String message;

    //是否成功
    private boolean success;
}
