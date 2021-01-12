package com.cy.rpc.server.configuration;

import com.cy.rpc.server.service.AbstractServiceFactory;

/**
 * @author chenyu3
 * rpc service配置支持
 */
public interface RpcServerConfigurationSupport {

    AbstractServiceFactory serviceFactory();
}
