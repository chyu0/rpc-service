package com.cy.rpc.client.configuration;

import com.cy.rpc.client.cache.ConfigCache;
import com.cy.rpc.client.cache.RetryConnectStrategyConfig;
import com.cy.rpc.client.cache.RpcClientConfig;
import com.cy.rpc.client.cache.ServiceCache;
import com.cy.rpc.client.cluster.ClientConnect;
import com.cy.rpc.client.properties.RpcClientConfigurationProperties;
import com.cy.rpc.common.utils.IpUtil;
import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.loader.ServiceRegister;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashMap;
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@EnableConfigurationProperties({RpcClientConfigurationProperties.class, RpcServiceZookeeperProperties.class})
public class RpcClientConfiguration {

    @Resource
    private RpcServiceZookeeperProperties zookeeperProperties;

    @Resource
    private RpcClientConfigurationProperties properties;

    @PostConstruct
    public void init() {
        //缓存基本的配置信息
        ConfigCache.setRpcClientConfig(RpcClientConfig.builder().timeout(properties.getTimeout())
                .selector(properties.getSelector()).build());
        //设置重试策略的缓存
        ConfigCache.setRetryConnectStrategyConfig(RetryConnectStrategyConfig.builder().retryStrategy(properties.getRetryStrategy())
                .maxRetryTimes(properties.getMaxRetryTimes()).retryDelay(properties.getRetryDelay())
                .maxRetryDelay(properties.getMaxRetryDelay()).build());

        Set<String> interfaceCaches = ServiceCache.getInterfaceCaches();
        if(CollectionUtils.isEmpty(interfaceCaches)) {
            return ;
        }

        //初始化消费者接口
        ZookeeperClientFactory.init(zookeeperProperties);

        Map<String, Set<String>> appHostMap = new HashMap<>();

        for(String interfaceCache : interfaceCaches) {
            //注册到消费者
            ServiceRegister.registerConsumerInterface(interfaceCache, zookeeperProperties.getAppName(), IpUtil.getHostIP());

            Map<String, Set<String>> hostNames = ServiceRegister.getHostNames(interfaceCache, new Watcher() {
                @Override
                public void process(WatchedEvent event) {
                    if(event.getType() == Watcher.Event.EventType.NodeChildrenChanged) {
                        String appName = ServiceRegister.getAppName(interfaceCache);
                        Set<String> oldAppName = ServiceCache.getAppCaches(appName);
                        Set<String> newAppName = ServiceRegister.getHostNames(interfaceCache, this).get(appName);
                        log.info("检测到子节点变更，path: {}，oldAppName: {}，newAppName: {}", event.getPath(), oldAppName, newAppName);
                        //删除可以无需监听，ClientHeartPingHandler监听
                        if(oldAppName == null) {
                            newAppName.forEach(item -> {
                                connect(appName, item);
                            });
                        }else if(oldAppName.size() < newAppName.size()) {
                            //新增时进行连接
                            newAppName.stream().filter(item -> !oldAppName.contains(item)).collect(Collectors.toList()).forEach(item -> {
                                log.info("检测到子节点变更，创建新连接，path: {}，oldAppName: {}，newAppName: {}", event.getPath(), oldAppName, newAppName);
                                connect(appName, item);
                            });
                        }
                        log.info("检测到子节点变更，处理结束，path: {}，oldAppName: {}，newAppName: {}", event.getPath(), oldAppName, newAppName);
                    }
                }
            });

            appHostMap.putAll(hostNames);
        }

        //连接到客户端
        appHostMap.forEach((key, value) -> {
            value.forEach(item -> {
                connect(key, item);
            });
        });
    }

    /**
     * 连接客户端，加个同步，虽然貌似没有必要
     * @param appName
     * @param hostName
     */
    private void connect(String appName, String hostName) {
        try {
            String[] address = hostName.split(":");
            if(address.length != 2) {
                log.warn("服务器地址有误！address:{}", hostName);
            }
            //添加到集群，并开启客户端连接
            ClientConnect.connect(appName, address[0], Integer.parseInt(address[1]));
            log.warn("客户端连接成功！address: {}", hostName);
        }catch (Exception e){
            log.error("RPC服务启动异常！", e);
        }
    }

}
