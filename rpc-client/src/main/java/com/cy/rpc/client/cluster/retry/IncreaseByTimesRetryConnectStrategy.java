package com.cy.rpc.client.cluster.retry;

/**
 * @author chenyu3
 * 按照重试时间递增
 */
public class IncreaseByTimesRetryConnectStrategy extends AbstractRetryConnectStrategy {

    /**
     * 按次数进行倍数递增
     * @param delay
     * @param maxDelay
     * @param times
     * @return
     */
    @Override
    public long calculationNextExecuteDelay(long delay, long maxDelay, int times) {
        return Math.min(delay * times, maxDelay);
    }
}
