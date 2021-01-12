package com.cy.rpc.common.exception;

import com.cy.rpc.common.enums.RpcErrorEnum;
import lombok.Getter;
import lombok.Setter;

/**
 * @author chenyu3
 * rpc 异常类
 */
@Getter
@Setter
public class RpcException extends RuntimeException {

    private int code;

    private String message;

    public RpcException(){
        code = 500;
        message = "系统异常";
    }

    public RpcException(int code, String message){
        this.code = code;
        this.message = message;
    }

    public RpcException(RpcErrorEnum rpcErrorEnum){
        this.code = rpcErrorEnum.getCode();
        this.message = rpcErrorEnum.getMessage();
    }


    public RpcException(RpcErrorEnum rpcErrorEnum, String message){
        this.code = rpcErrorEnum.getCode();
        this.message = !"".equals(message) ? message : rpcErrorEnum.getMessage();
    }
}
