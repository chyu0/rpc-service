package com.cy.rpc.register.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Configuration;

import java.util.HashMap;
import java.util.Map;

/**
 * @author chenyu3
 * zookeeper配置信息
 */
@Getter
@Setter
@ConfigurationProperties(prefix = "zk.rpc")
@Configuration
public class ZookeeperProperties{

    /**
     * 连接地址，多个用逗号分隔
     */
    private String serverLists;

    /**
     * application name，与rpc service name对应
     */
    private String appName;

    /**
     * 节点自身令牌
     */
    private String userNameAndPassword;
    /**
     * 注册中心namespace
     */
    private String namespace = "rpc-service/register";

    /**
     * 等待重试的间隔时间的初始值.
     * 单位毫秒.
     */
    private int baseSleepTimeMilliseconds = 1000;

    /**
     * 等待重试的间隔时间的最大值.
     * 单位毫秒.
     */
    private int maxSleepTimeMilliseconds = 3000;

    /**
     * 最大重试次数.
     */
    private int maxRetries = 3;

    /**
     * 会话超时时间.
     * 单位毫秒.
     */
    private int sessionTimeoutMilliseconds = Integer.MAX_VALUE;

    /**
     * 连接超时时间.
     * 单位毫秒.
     */
    private int connectionTimeoutMilliseconds = 3000;

    /**
     * 连接Zookeeper的权限令牌.
     * 服务端权限map
     */
    private Map<String, String> digestMap = new HashMap<>();

}
