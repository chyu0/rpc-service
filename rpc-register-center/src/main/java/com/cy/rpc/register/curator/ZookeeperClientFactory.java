package com.cy.rpc.register.curator;

import com.cy.rpc.register.properties.ZookeeperProperties;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.ConcurrentHashMap;
import java.util.concurrent.TimeUnit;

/**
 * @author chenyu3
 * zk客户端连接工厂
 */
@Slf4j
public class ZookeeperClientFactory {

    @Getter
    private static final Map<String, CuratorFramework> clients = new ConcurrentHashMap<>(new HashMap<>());

    /**
     * 初始化
     * @param properties
     */
    public static void init(ZookeeperProperties properties, List<String> clientAppNames) {
        //默认连接
        CuratorFramework defaultClient = builder(properties).build();

        //服务端
        if(clients.get(properties.getAppName()) == null) {
            if(StringUtils.hasText(properties.getZkDigest())) {
                clients.put(properties.getAppName(), buildServer(properties));
            }else {
                clients.put(properties.getAppName(), defaultClient);
            }
        }

        //客户端
        if(!CollectionUtils.isEmpty(clientAppNames)) {
            for(String appName : clientAppNames) {
                //未添加过
                if(clients.get(appName) == null) {
                    if(properties.getDigestMap().get(appName) != null) {
                        clients.put(appName, buildClient(properties, appName));
                    }else {
                        clients.put(appName, defaultClient);
                    }
                }
            }
        }

        //启动所有zk连接
        for(CuratorFramework framework : clients.values()) {
            if(framework.getZookeeperClient().isConnected()) {
                continue;
            }

            //启动
            framework.start();
            try {
                framework.blockUntilConnected(properties.getMaxSleepTimeMilliseconds() * properties.getMaxRetries(), TimeUnit.MILLISECONDS);
                if(!framework.getZookeeperClient().isConnected()) {
                    CloseableUtils.closeQuietly(framework);
                }
                log.error("Client failed to connect to zookeeper service");
            } catch (final Exception ex) {
                log.error("Client failed to connect to zookeeper service : ", ex);
            }
        }
    }

    /**
     * 带权限验证
     * @return
     */
    public static CuratorFramework buildServer(ZookeeperProperties properties) {
        CuratorFrameworkFactory.Builder builder = builder(properties);
        if(!StringUtils.hasText(properties.getZkDigest())) {
            return builder.build();
        }

        return builder.aclProvider(buildDefaultAclProvider()).authorization("digest", properties.getZkDigest().getBytes()).build();
    }

    /**
     * 带权限验证
     * @return
     */
    private static CuratorFramework buildClient(ZookeeperProperties properties, String appName) {
        CuratorFrameworkFactory.Builder builder = builder(properties);
        if(!StringUtils.hasText(appName)) {
            return builder.build();
        }

        if(CollectionUtils.isEmpty(properties.getDigestMap())) {
            return builder.build();
        }

        String digest = properties.getDigestMap().get(appName);

        if(digest == null) {
            return builder.build();
        }

        return builder.aclProvider(buildDefaultAclProvider()).authorization("digest", digest.getBytes()).build();
    }

    /**
     * 构建一个基本的 curator framework builder对象
     * @return
     */
    public static CuratorFrameworkFactory.Builder builder(ZookeeperProperties properties) {
        CuratorFrameworkFactory.Builder builder = CuratorFrameworkFactory.builder();
        builder.connectString(properties.getServerLists()).namespace(properties.getNamespace())
                .retryPolicy(new ExponentialBackoffRetry(properties.getBaseSleepTimeMilliseconds(), properties.getMaxRetries(), properties.getMaxSleepTimeMilliseconds()));

        if(properties.getSessionTimeoutMilliseconds() > 0) {
            builder.sessionTimeoutMs(properties.getSessionTimeoutMilliseconds());
        }

        if(properties.getConnectionTimeoutMilliseconds() > 0) {
            builder.connectionTimeoutMs(properties.getConnectionTimeoutMilliseconds());
        }
        return builder;
    }

    private static ACLProvider buildDefaultAclProvider() {
        return new ACLProvider() {
            @Override
            public List<ACL> getDefaultAcl() {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }

            @Override
            public List<ACL> getAclForPath(String s) {
                return ZooDefs.Ids.CREATOR_ALL_ACL;
            }
        };
    }
}
