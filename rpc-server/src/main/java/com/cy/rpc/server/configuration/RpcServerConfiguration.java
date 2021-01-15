package com.cy.rpc.server.configuration;

import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import com.cy.rpc.register.loader.ServerServiceRegister;
import com.cy.rpc.register.properties.RpcServiceZookeeperProperties;
import com.cy.rpc.server.Server;
import com.cy.rpc.server.properties.RpcServerConfigurationProperties;
import io.netty.channel.socket.ServerSocketChannel;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

/**
 * @author chenyu3
 * 启动rpc server
 */
@Slf4j
@ConditionalOnBean(RpcServerConfigurationSupport.class)
@EnableConfigurationProperties({RpcServerConfigurationProperties.class, RpcServiceZookeeperProperties.class})
public class RpcServerConfiguration {

    @Resource
    private RpcServerConfigurationProperties properties;

    @Resource
    private RpcServerConfigurationSupport support;

    @Resource
    private RpcServiceZookeeperProperties rpcServiceZookeeperProperties;

    private static final String SERVICE_INTERFACE_PATH = "/interface";

    private static final String SERVER_PATH = "/server";

    @PostConstruct
    public void init() {

        if(support.serviceFactory() == null) {
            throw new RpcException(RpcErrorEnum.INNER_ERROR, "丢失 serviceFactory Bean");
        }

        Server server = new Server(support.serviceFactory(), properties.getPort());
        try {
            ServerSocketChannel serverSocketChannel = server.start();
            if(serverSocketChannel == null) {
                log.error("RPC服务启动失败！");
                return ;
            }
            log.info("RPC服务已启动，remoteAddress:{}", serverSocketChannel);
        }catch (Exception e){
            log.error("RPC服务启动异常！", e);
        }

        //初始化zk
        ZookeeperClientFactory.init(rpcServiceZookeeperProperties);

        //初始化根节点
        ServiceCuratorFramework curatorFramework = ZookeeperClientFactory.getDefaultClient();
        curatorFramework.persist(SERVER_PATH, null);
        curatorFramework.persist(SERVICE_INTERFACE_PATH, null);

        //注册接口
        ServerServiceRegister.registerInterface(rpcServiceZookeeperProperties.getAppName());
        //注册服务
        ServerServiceRegister.registerServer(rpcServiceZookeeperProperties.getAppName(), properties.getPort());

    }

}
