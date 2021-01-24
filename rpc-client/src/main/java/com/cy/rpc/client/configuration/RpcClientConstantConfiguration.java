package com.cy.rpc.client.configuration;

import com.cy.rpc.client.cache.ConfigCache;
import com.cy.rpc.client.cache.RetryConnectStrategyConfig;
import com.cy.rpc.client.cache.RpcClientConfig;
import com.cy.rpc.client.properties.RpcClientConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;

import javax.annotation.Resource;

/**
 * @author chenyu3
 * 静态常量加载
 */
@Slf4j
@EnableConfigurationProperties({RpcClientConfigurationProperties.class})
public class RpcClientConstantConfiguration {

    @Resource
    private RpcClientConfigurationProperties properties;

    @Bean
    public RpcClientConfig rpcClientConfig() {
        RpcClientConfig rpcClientConfig = RpcClientConfig.builder().timeout(properties.getTimeout())
                .selector(properties.getSelector()).build();
        //缓存基本的配置信息
        ConfigCache.setRpcClientConfig(rpcClientConfig);
        return rpcClientConfig;
    }

    @Bean
    public RetryConnectStrategyConfig retryConnectStrategy() {
        RetryConnectStrategyConfig retryConnectStrategyConfig = RetryConnectStrategyConfig.builder().retryStrategy(properties.getRetryStrategy())
                .maxRetryTimes(properties.getMaxRetryTimes()).retryDelay(properties.getRetryDelay())
                .maxRetryDelay(properties.getMaxRetryDelay()).build();
        //缓存重试策略
        ConfigCache.setRetryConnectStrategyConfig(retryConnectStrategyConfig);
        return retryConnectStrategyConfig;
    }
}
