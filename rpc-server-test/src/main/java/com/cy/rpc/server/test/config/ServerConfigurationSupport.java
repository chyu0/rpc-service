package com.cy.rpc.server.test.config;

import com.cy.rpc.server.configuration.RpcServerConfigurationSupport;
import com.cy.rpc.server.service.AbstractServiceFactory;
import org.springframework.stereotype.Component;

/**
 * @author chenyu3
 * 服务配置支持！
 */
@Component
public class ServerConfigurationSupport implements RpcServerConfigurationSupport {

    @Override
    public AbstractServiceFactory serviceFactory() {
        return new AbstractServiceFactory() {
            @Override
            public Object getServiceByName(String serviceName) {
                return SpringContextHolder.getBeanByName(serviceName);
            }
        };
    }
}
