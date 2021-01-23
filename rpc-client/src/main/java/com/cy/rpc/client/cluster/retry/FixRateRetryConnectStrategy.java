package com.cy.rpc.client.cluster.retry;

/**
 * @author chenyu3
 * 固定时间间隔重试
 */
public class FixRateRetryConnectStrategy extends AbstractRetryConnectStrategy {
    @Override
    public long calculationNextExecuteDelay(long delay, int times) {
        return delay;
    }
}
