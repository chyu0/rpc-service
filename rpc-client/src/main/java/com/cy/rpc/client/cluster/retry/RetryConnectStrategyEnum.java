package com.cy.rpc.client.cluster.retry;

import lombok.Getter;

/**
 * @author chenyu3
 * 重试策略枚举
 */
public enum RetryConnectStrategyEnum {

    FixRateRetryConnectStrategy(new FixRateRetryConnectStrategy(), "随机选择器"),
    IncreaseByTimesRetryConnectStrategy(new IncreaseByTimesRetryConnectStrategy(), "按照次数递增策略");

    RetryConnectStrategyEnum(AbstractRetryConnectStrategy strategy, String desc) {
        this.strategy = strategy;
        this.desc = desc;
    }

    @Getter
    private AbstractRetryConnectStrategy strategy;

    @Getter
    private String desc;
}
