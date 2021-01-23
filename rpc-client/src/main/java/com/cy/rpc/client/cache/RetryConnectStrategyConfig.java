package com.cy.rpc.client.cache;

import com.cy.rpc.client.cluster.retry.AbstractRetryConnectStrategy;
import com.cy.rpc.client.cluster.retry.FixRateRetryConnectStrategy;
import com.cy.rpc.client.cluster.retry.RetryConnectStrategyEnum;
import lombok.Getter;
import lombok.Setter;
import org.apache.commons.lang3.EnumUtils;

/**
 * @author chenyu3
 * 重试策略的缓存
 */
@Getter
@Setter
public class RetryConnectStrategyConfig {
    /**
     * 重试策略
     */
    private AbstractRetryConnectStrategy retryStrategy;

    /**
     * 最大重试次数
     */
    private int maxRetryTimes;

    /**
     * 每次重试时间间隔
     */
    private long retryDelay;

    /**
     * 初始化
     */
    public RetryConnectStrategyConfig() {
        Builder builder = new Builder();
        this.maxRetryTimes = builder.maxRetryTimes;
        this.retryDelay = builder.retryDelay;
        this.retryStrategy = builder.retryStrategy;
    }

    /**
     * 构造函数
     * @param builder
     */
    public RetryConnectStrategyConfig(Builder builder) {
        this.maxRetryTimes = builder.maxRetryTimes;
        this.retryDelay = builder.retryDelay;
        this.retryStrategy = builder.retryStrategy;
    }

    /**
     * builder
     */
    @Getter
    @Setter
    public static class Builder {
        /**
         * 重试策略
         */
        private AbstractRetryConnectStrategy retryStrategy = new FixRateRetryConnectStrategy();

        /**
         * 最大重试次数
         */
        private int maxRetryTimes = 3;

        /**
         * 每次重试时间间隔
         */
        private long retryDelay = 10000;


        public Builder maxRetryTimes(int maxRetryTimes) {
            this.maxRetryTimes = maxRetryTimes;
            return this;
        }

        public Builder retryStrategy(String retryStrategy) {
            this.retryStrategy = EnumUtils.getEnum(RetryConnectStrategyEnum.class, retryStrategy).getStrategy();
            return this;
        }

        public Builder retryDelay(long retryDelay) {
            this.retryDelay = retryDelay;
            return this;
        }

        public RetryConnectStrategyConfig build() {
            return new RetryConnectStrategyConfig(this);
        }
    }


    /**
     *
     * @return
     */
    public static Builder builder() {
        return new Builder();
    }

    /**
     * 计算下次执行时间
     * @param times 重试次数
     * @return
     */
    public long calculationNextExecuteDelay(int times) {
        return retryStrategy.calculationNextExecuteDelay(retryDelay, times);
    }
}
