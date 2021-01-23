package com.cy.rpc.server.facade.service;

import com.cy.rpc.common.annotation.RpcService;
import com.cy.rpc.server.facade.entity.Message;

import java.util.List;

@RpcService(value = {"testService"}, defaultValue = "testService")
public interface TestService {

    String getTestName(Message message, String extend, int i);

}
