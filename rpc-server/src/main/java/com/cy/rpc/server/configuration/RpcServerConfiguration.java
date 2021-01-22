package com.cy.rpc.server.configuration;

import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
import com.cy.rpc.common.utils.IpUtil;
import com.cy.rpc.register.curator.ZookeeperClientFactory;
import com.cy.rpc.register.framework.ServiceCuratorFramework;
import com.cy.rpc.register.loader.ServiceRegister;
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

        //注册接口
        ServiceRegister.registerProviderInterface(rpcServiceZookeeperProperties.getAppName(), IpUtil.getHostIP(), properties.getPort());

    }

}
