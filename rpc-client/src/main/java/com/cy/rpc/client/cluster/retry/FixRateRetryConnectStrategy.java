package com.cy.rpc.client.cluster.retry;

/**
 * @author chenyu3
 * 固定时间间隔重试
 */
public class FixRateRetryConnectStrategy extends AbstractRetryConnectStrategy {
    @Override
    public long calculationNextExecuteDelay(long delay, long maxDelay, int times) {
        return Math.min(delay, maxDelay);
    }
}
