package com.cy.rpc.server.facade2;

import com.cy.rpc.common.annotation.RpcService;

@RpcService(value = {"myService"}, defaultValue = "myService")
public interface FacadeServer {

    String queryName();
}
