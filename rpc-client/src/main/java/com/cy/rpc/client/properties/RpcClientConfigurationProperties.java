package com.cy.rpc.client.properties;

import lombok.Getter;
import lombok.Setter;
import org.springframework.boot.context.properties.ConfigurationProperties;

import java.util.Map;

/**
 * @author chenyu3
 * rpc 客户端地址
 */
@ConfigurationProperties(prefix = "rpc.client")
@Getter
@Setter
public class RpcClientConfigurationProperties {

    private Map<String, String> remotes;
}
