package com.cy.rpc.client.cluster.retry;

/**
 * @author chenyu3
 * 连接重试时间
 */
public abstract class AbstractRetryConnectStrategy {

    /**
     * 计算连接重试时间间隔
     * @return
     */
    public abstract long calculationNextExecuteDelay(long delay, long maxDelay, int times);

}
