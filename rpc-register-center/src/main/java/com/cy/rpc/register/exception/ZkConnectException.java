package com.cy.rpc.register.exception;

import lombok.Getter;
import lombok.Setter;

/**
 * @author chenyu3
 * rpc 异常类
 */
@Getter
@Setter
public class ZkConnectException extends RuntimeException {

    private int code;

    private String message;

    public ZkConnectException(){
        code = 500;
        message = "系统异常";
    }

    public ZkConnectException(int code, String message){
        this.code = code;
        this.message = message;
    }

    public ZkConnectException(ZkErrorEnum zkErrorEnum){
        this.code = zkErrorEnum.getCode();
        this.message = zkErrorEnum.getMessage();
    }


    public ZkConnectException(ZkErrorEnum zkErrorEnum, String message){
        this.code = zkErrorEnum.getCode();
        this.message = !"".equals(message) ? message : zkErrorEnum.getMessage();
    }
}
