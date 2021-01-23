package com.cy.rpc.client.cluster.retry;

/**
 * @author chenyu3
 * 按照重试时间递增
 */
public class IncreaseByTimesRetryConnectStrategy extends AbstractRetryConnectStrategy {
    @Override
    public long calculationNextExecuteDelay(long delay, int times) {
        return delay + delay * (times - 1);
    }
}
