package com.cy.rpc.client.cache;

/**
 * @author chenyu3
 * 配置信息缓存
 */
public class ConfigCache {

    /**
     * 基本信息
     */
    private static RpcClientConfig rpcClientConfig;

    /**
     * 重试连接策略
     */
    private static RetryConnectStrategyConfig retryConnectStrategyConfig;

    /**
     * 缓存rpc client基本配置
     * @param config
     */
    public static void setRpcClientConfig(RpcClientConfig config) {
        rpcClientConfig = config;
    }

    /**
     * 获取rpc client基本配置
     */
    public static RpcClientConfig getRpcClientConfig() {
        return rpcClientConfig != null ? rpcClientConfig : new RpcClientConfig();
    }

    /**
     * 缓存rpc client基本配置
     * @param config
     */
    public static void setRetryConnectStrategyConfig(RetryConnectStrategyConfig config) {
        retryConnectStrategyConfig = config;
    }

    /**
     * 获取rpc client基本配置
     */
    public static RetryConnectStrategyConfig getRetryConnectStrategyConfig() {
        return retryConnectStrategyConfig != null ? retryConnectStrategyConfig : new RetryConnectStrategyConfig();
    }
}
