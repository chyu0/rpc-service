package com.cy.rpc.client.configuration;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.cache.ServiceCache;
import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import lombok.extern.slf4j.Slf4j;
import org.apache.zookeeper.WatchedEvent;
import org.apache.zookeeper.Watcher;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.util.CollectionUtils;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

@Slf4j
@EnableConfigurationProperties({RpcServiceZookeeperProperties.class})
public class RpcClientConfiguration {

    private static final String SERVICE_INTERFACE_PATH = "/interface/";

    private static final String SERVER_PATH = "/server/";

    @Resource
    private RpcServiceZookeeperProperties zookeeperProperties;

    @PostConstruct
    public void init() {

        Set<String> interfaceCaches = ServiceCache.getInterfaceCaches();

        if(CollectionUtils.isEmpty(interfaceCaches)) {
            return ;
        }

        ZookeeperClientFactory.init(zookeeperProperties);

        Set<String> appIds = new HashSet<>();
        ServiceCuratorFramework curatorFramework = ZookeeperClientFactory.getDefaultClient();
        for(String interfaceCache : interfaceCaches) {
            List<String> childrenList = curatorFramework.getChildren(SERVICE_INTERFACE_PATH + interfaceCache, new Watcher() {
                @Override
                public void process(WatchedEvent watchedEvent) {
                    synchronized (ServiceCache.getAppCaches()){
                        List<String> changeChildren = curatorFramework.getChildren(SERVICE_INTERFACE_PATH + interfaceCache, this);
                        log.info("监听到子节点变更通知，interface : {}, changes : {}, cache : {}", interfaceCache, changeChildren, ServiceCache.getAppCaches());
                        //新增的节点
                        changeChildren.stream().filter(item -> !ServiceCache.getAppCaches().contains(item)).forEach(item -> {
                            registerAppId(item);
                        });
                        //删除的节点
                        ServiceCache.getAppCaches().stream().filter(item -> !changeChildren.contains(item)).forEach(ServiceCache::removeAppCache);
                    }
                }
            });
            //加入到appIds
            if(!CollectionUtils.isEmpty(childrenList)) {
                appIds.addAll(childrenList);
            }
        }

        for(String appId : appIds) {
            registerAppId(appId);
        }
    }

    private void registerAppId(String appId) {
        ServiceCuratorFramework framework = ZookeeperClientFactory.getCuratorFrameworkByAppName(appId);

        List<String> hostNameList = framework.getChildren(SERVER_PATH + appId, new Watcher() {
            @Override
            public void process(WatchedEvent watchedEvent) {
                if(watchedEvent.getType() == Event.EventType.NodeChildrenChanged) {
                    List<String> changeHostNames = framework.getChildren(SERVER_PATH + appId, this);
                    synchronized (ServiceCache.getAppCaches()) {
                        log.info("监听到子节点变更通知，appId : {}, changes : {}, cache : {}", appId, changeHostNames, ServiceCache.getAppCaches(appId));
                        if(ServiceCache.getAppCaches(appId) != null) {
                            //新加入的节点注册到客户端，创建连接
                            changeHostNames.stream().filter(item -> !ServiceCache.getAppCaches(appId).contains(item)).forEach(item -> {
                                ServiceCache.putAppCaches(appId, item);
                                connect(appId, item);
                            });

                            ServiceCache.getAppCaches(appId).stream().filter(item -> !changeHostNames.contains(item)).forEach(item -> {
                                ServiceCache.removeAppCache(appId, item);
                            });
                        }else {
                            ServiceCache.putAllAppCaches(appId, changeHostNames);
                            changeHostNames.forEach(item -> connect(appId, item));
                        }
                    }
                }
            }
        });


        ServiceCache.putAllAppCaches(appId, hostNameList);

        //连接服务端
        for(String hostname : hostNameList) {
            connect(appId, hostname);
        }
    }


    /**
     * 连接客户端
     * @param appId
     * @param hostName
     */
    private void connect(String appId, String hostName) {
        try {
            String[] address = hostName.split(":");
            if(address.length != 2) {
                log.warn("服务器地址有误！address:{}", hostName);
            }

            Client client = new Client(appId, address[0], Integer.parseInt(address[1]));
            client.connect();

            log.warn("客户端连接成功！address: {}", hostName);
        }catch (Exception e){
            log.error("RPC服务启动异常！", e);
        }
    }

}
