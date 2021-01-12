package com.cy.rpc.server.configuration;

import com.cy.rpc.common.enums.RpcErrorEnum;
import com.cy.rpc.common.exception.RpcException;
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
@EnableConfigurationProperties(RpcServerConfigurationProperties.class)
public class RpcServerConfiguration {

    @Resource
    private RpcServerConfigurationProperties properties;

    @Resource
    private RpcServerConfigurationSupport support;

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
    }

}
