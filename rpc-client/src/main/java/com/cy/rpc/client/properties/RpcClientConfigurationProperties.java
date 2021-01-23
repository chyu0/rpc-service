package com.cy.rpc.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

@ConfigurationProperties(prefix = "rpc.client")
@Getter
@Setter
public class RpcClientConfigurationProperties {

    /**
     * 接口调用超时时间
     */
    private long timeout = 3000;

    /**
     * 选择器
     */
    private String selector = "RandomSelector";

    /**
     * 重试策略
     */
    private String retryStrategy = "FixRateRetryConnectStrategy";

    /**
     * 最大重试次数
     */
    private int maxRetryTimes = 3;

    /**
     * 每次重试时间间隔
     */
    private long retryDelay = 10000;

}
