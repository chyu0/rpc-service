package com.cy.rpc.client.cluster;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.cache.ConfigCache;
import com.cy.rpc.client.cache.RetryConnectStrategyConfig;
import com.cy.rpc.client.cache.ServiceCache;
import io.netty.channel.EventLoopGroup;
import lombok.extern.slf4j.Slf4j;
import org.apache.commons.lang3.StringUtils;

import java.util.concurrent.Executors;
import java.util.concurrent.ScheduledExecutorService;
import java.util.concurrent.TimeUnit;

/**
 * @author chenyu3
 * 对客户端进行连接
 */
@Slf4j
public class ClientConnect {

    private static final ScheduledExecutorService scheduledExecutorService = Executors.newScheduledThreadPool(1);
    /**
     * 连接客户端
     * @param appName 服务端对应的app名称
     * @param remoteAddress 远程连接 例如127.0.0.1
     * @param port 端口 例如8080
     */
    public static void connect(String appName, String remoteAddress, int port) {
        ClientCluster cluster = ClientClusterCache.put(appName, remoteAddress, port);
        ServiceCache.putAppCaches(appName, StringUtils.joinWith(":",  remoteAddress, port));
        //获取集群客户端
        Client client = cluster.getClient(remoteAddress, port);
        //启动客户端连接
        if(!client.connect(cluster.getEventLoopGroup())) {
            log.info("客户端连接失败，准备重试！appName: {}, client: {}", appName, client);
            executeRetryConnect(cluster.getEventLoopGroup(), appName, client);
        }
    }

    /**
     * 连接客户端
     * @param appName 服务端对应的app名称
     * @param hostName 远程连接 例如127.0.0.1:8080
     */
    public static void connect(String appName, String hostName) {
        String[] address = hostName.split(":");
        if(address.length != 2) {
            log.error("服务器地址有误！address:{}", hostName);
            return ;
        }
        //添加到集群，并开启客户端连接
        connect(appName, address[0], Integer.parseInt(address[1]));
    }

    /**
     * 进行客户端重新连接
     * @param remoteAddress 远程地址
     * @param port 端口
     */
    public static void tryConnect(String appName, String remoteAddress, int port) {
        ClientCluster cluster = ClientClusterCache.getCluster(appName);
        if(cluster == null) {
            log.warn("未找到客户端集群，重连失败！，appName: {}， remoteAddress : {}, port : {}", appName, remoteAddress, port);
            return ;
        }

        Client client = cluster.getClient(remoteAddress, port);
        if(client == null) {
            log.warn("未找到客户端连接，重连失败！，remoteAddress : {}, port : {}", remoteAddress, port);
            return ;
        }
        //客户端重连
        executeRetryConnect(cluster.getEventLoopGroup(), appName, client);
    }

    /**
     * 重试客户端连接
     * @param localAddress 客户端地址
     */
    public static void tryConnect(String appName, String localAddress) {
        ClientCluster cluster = ClientClusterCache.getCluster(appName);
        if(cluster == null) {
            log.warn("未找到客户端集群，重连失败！，appName : {}, localAddress : {}", appName, localAddress);
            return ;
        }

        Client client = cluster.getClient(localAddress);
        if(client == null) {
            log.warn("未找到客户端连接，重连失败！，localAddress : {}", localAddress);
            return ;
        }
        //客户端重连
        executeRetryConnect(cluster.getEventLoopGroup(), appName, client);
    }

    /**
     * 客户端重试
     * @param client 客户端连接
     */
    private static void executeRetryConnect(EventLoopGroup eventLoopGroup, String appName, Client client) {
        //获取重试策略的配置
        RetryConnectStrategyConfig strategyConfig = ConfigCache.getRetryConnectStrategyConfig();

        //超过最大重试次数的话，就把这个客户端移除掉，重试的目的一个是为了避免网络波动，服务端短暂下线的情况
        //正常情况下重试必须存在，否则会有未知的问题
        if(client.getConnectTimes() >= strategyConfig.getMaxRetryTimes()) {
            log.error("连接超时，断开连接，client:{}" , client);
            //移除该客户端的集群配置
            ClientClusterCache.remove(appName, client.getRemoteAddress(), client.getPort());
            ServiceCache.removeAppCache(appName, StringUtils.joinWith(":", client.getRemoteAddress(), client.getPort()));
            return ;
        }

        //通过策略计算每次需要等待的时间
        scheduledExecutorService.schedule(() -> {
            log.info("客户端重连开始！appName: {}, client: {}", appName, client.toString());
            if(!client.connect(eventLoopGroup)) {
                log.warn("客户端重连失败{}次！appName: {}, client: {}", client.getConnectTimes(), appName, client);
                executeRetryConnect(eventLoopGroup, appName, client);
            }
            log.info("客户端重连结束！appName: {}, client: {}", appName, client.toString());
        }, strategyConfig.calculationNextExecuteDelay(client.getConnectTimes()), TimeUnit.MILLISECONDS);
    }

}
