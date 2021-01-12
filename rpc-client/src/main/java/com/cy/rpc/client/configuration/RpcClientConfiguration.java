package com.cy.rpc.client.configuration;

import com.cy.rpc.client.Client;
import com.cy.rpc.client.properties.RpcClientConfigurationProperties;
import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.context.properties.EnableConfigurationProperties;

import javax.annotation.PostConstruct;
import javax.annotation.Resource;

@Slf4j
@EnableConfigurationProperties({RpcClientConfigurationProperties.class})
public class RpcClientConfiguration {

    @Resource
    private RpcClientConfigurationProperties properties;

    @PostConstruct
    public void init() {

        if(properties.getRemotes() == null) {
            log.warn("没有找到任何可以绑定的服务器！");
        }

        properties.getRemotes().forEach((key, value) -> {
            try {
                String[] address = value.split(":");
                if(address.length != 2) {
                    log.warn("服务器地址有误！address:{}", value);
                }

                Client client = new Client(key, address[0], Integer.parseInt(address[1]));
                client.connect();

                log.warn("客户端连接成功！address: {}", value);
            }catch (Exception e){
                log.error("RPC服务启动异常！", e);
            }
        });
    }
}
