package com.cy.rpc.client.configuration;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.cache.ServiceCache;
import com.cy.rpc.client.sockect.ClientFactory;
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
import java.util.Map;
import java.util.Set;
import java.util.stream.Collectors;

@Slf4j
@EnableConfigurationProperties({RpcServiceZookeeperProperties.class})
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
                        if(oldAppName.size() < newAppName.size()) {
                            //新增时进行连接
                            newAppName.stream().filter(item -> !oldAppName.contains(item)).collect(Collectors.toList()).forEach(item -> {
                                if(!ClientFactory.exist(appName, item)) {
                                    connect(appName, item);
                                }
                            });
                        }
                    }
                }
            });

            //连接到客户端
            hostNames.forEach((key, value) -> {
                ServiceCache.putAllAppHostCaches(key, value);
                value.forEach(item -> {
                    if(!ClientFactory.exist(key, item)) {
                        connect(key, item);
                    }
                });
            });
        }

    }

    /**
     * 连接客户端
     * @param appName
     * @param hostName
     */
    private void connect(String appName, String hostName) {
        try {
            String[] address = hostName.split(":");
            if(address.length != 2) {
                log.warn("服务器地址有误！address:{}", hostName);
            }
            Client client = new Client(appName, address[0], Integer.parseInt(address[1]));
            client.toConnect();
            log.warn("客户端连接成功！address: {}", hostName);
        }catch (Exception e){
            log.error("RPC服务启动异常！", e);
        }
    }

}
