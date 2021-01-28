package com.cy.rpc.client.configuration;

import com.cy.rpc.client.cache.ServiceCache;
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

import static com.cy.rpc.client.cluster.ClientConnector.connect;

/**
 * @author chenyu3
 * 注册消费者，连接服务端
 */
@Slf4j
@EnableConfigurationProperties({RpcClientConfigurationProperties.class, RpcServiceZookeeperProperties.class})
public class RpcClientConfiguration {

    @Resource
    private RpcServiceZookeeperProperties zookeeperProperties;

    @PostConstruct
    public void init() {
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

            //合并
            hostNames.forEach((key, value) -> appHostMap.merge(key, value, (v1, v2) -> {
                v1.addAll(v2);
                return v1;
            }));
        }

        //连接到客户端
        appHostMap.forEach((key, value) -> {
            value.forEach(item -> {
                connect(key, item);
            });
        });
    }

}
