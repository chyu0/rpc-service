package com.cy.rpc.server.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * @author chenyu3
 * rpc服务配置
 */
@ConfigurationProperties(prefix = "rpc.server")
@Getter
@Setter
public class RpcServerConfigurationProperties {

    private int port;

}
