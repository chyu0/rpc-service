package com.cy.rpc.register.curator;

import com.cy.rpc.register.properties.ZookeeperProperties;
import com.cy.rpc.register.utils.Base64Utils;
import lombok.Getter;
import lombok.extern.slf4j.Slf4j;
import org.apache.curator.framework.CuratorFramework;
import org.apache.curator.framework.CuratorFrameworkFactory;
import org.apache.curator.framework.api.ACLProvider;
import org.apache.curator.retry.ExponentialBackoffRetry;
import org.apache.curator.utils.CloseableUtils;
import org.apache.zookeeper.ZooDefs;
import org.apache.zookeeper.data.ACL;
import org.apache.zookeeper.data.Id;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

import java.util.*;
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
            if(StringUtils.hasText(properties.getUserNameAndPassword())) {
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
                        clients.put(appName, buildClient(properties, properties.getDigestMap().get(appName)));
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
        if(!StringUtils.hasText(properties.getUserNameAndPassword())) {
            return builder.build();
        }

        String digest = Base64Utils.getDigest(properties.getUserNameAndPassword());
        if(!StringUtils.hasText(properties.getUserNameAndPassword())) {
            return builder.build();
        }

        ACLProvider aclProvider = buildAclProvider(digest,
                Arrays.asList(ZooDefs.Perms.CREATE, ZooDefs.Perms.WRITE, ZooDefs.Perms.DELETE, ZooDefs.Perms.READ));

        return builder.aclProvider(aclProvider).build();
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

        if(properties.getDigestMap().get(appName) == null) {
            return builder.build();
        }

        return builder.aclProvider(buildAclProvider(properties.getDigestMap().get(appName), Collections.singletonList(ZooDefs.Perms.READ))).build();
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

    /**
     * 构建一个权限控制对象，包含读，写，创建的权限，digest是加密后的令牌，作用于服务端
     * @return
     */
    private static ACLProvider buildAclProvider(String digest, List<Integer> perms) {
        if(CollectionUtils.isEmpty(perms)) {
            return null;
        }

        List<ACL> aclList = new ArrayList<>();
        for(Integer perm : perms) {
            aclList.add(new ACL(perm, new Id("digest", digest)));
        }

        return new ACLProvider() {
            @Override
            public List<ACL> getDefaultAcl() {
                return aclList;
            }

            @Override
            public List<ACL> getAclForPath(final String path) {
                return aclList;
            }
        };
    }
}
