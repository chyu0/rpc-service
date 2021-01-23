package com.cy.rpc.client.future;


import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
import com.cy.rpc.common.payload.ResultPayload;

import java.util.Collections;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;
import java.util.concurrent.locks.Condition;
import java.util.concurrent.locks.Lock;

/**
 * @author chenyu3
 * ResultFuture工厂
 */
public class FutureFactory {

    private static final Map<String, ResultFuture> futureMap = Collections.synchronizedMap(new HashMap<>());

    public static void put(ResultFuture future) {

        if(future == null){
            return ;
        }

        futureMap.put(future.getRequestId(), future);
    }


    /**
     * 收到回调通知，发信号
     * @param requestId
     * @param result
     */
    public static void receive(String requestId, ResultPayload result) {
        ResultFuture resultFuture = futureMap.get(requestId);
        if(resultFuture == null) {
            return ;
        }

        resultFuture.getLock().lock();
        try {
            resultFuture.setResult(result);
            Condition condition = resultFuture.getCondition();
            if(condition != null) {
                condition.signal();
            }
        }catch (Exception e){
            e.printStackTrace();
        }finally {
            resultFuture.getLock().unlock();
        }
    }

    /**
     * 获取回调数据
     * @param requestId
     * @return
     */
    public static ResultPayload getData(String requestId, long timeout) {
        ResultFuture resultFuture = futureMap.get(requestId);
        if(resultFuture == null) {
            return null;
        }

        Lock lock = resultFuture.getLock();
        lock.lock();//先锁
        try {
            boolean await = resultFuture.getCondition().await(timeout, TimeUnit.MILLISECONDS);
            if(!await) {
                throw new RpcException(RpcErrorEnum.CALL_TIME_OUT);
            }
        } catch (InterruptedException e) {
            e.printStackTrace();
        }finally{
            lock.unlock();//释放锁
            futureMap.remove(requestId);
        }

        return resultFuture.getResult();
    }
}
