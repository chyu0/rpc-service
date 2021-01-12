package com.cy.rpc.client.future;

import com.cy.rpc.common.payload.ResultPayload;
import lombok.Getter;
import lombok.Setter;

import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;
import java.util.concurrent.locks.ReentrantLock;

/**
 * @author chenyu3
 * 返回结果future
 */
@Getter
@Setter
public class ResultFuture {

    //请求id
    private String requestId;

    //返回结果
    private ResultPayload result;

    //获取锁
    private volatile Lock lock = new ReentrantLock();

    //线程通知条件
    private volatile Condition condition = lock.newCondition();

}
