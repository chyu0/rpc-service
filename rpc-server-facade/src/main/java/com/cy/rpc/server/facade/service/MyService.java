package com.cy.rpc.server.facade.service;

import com.cy.rpc.common.annotation.RpcService;
import com.cy.rpc.server.facade.entity.Message;

import java.util.List;

@RpcService(value = {"myService","myService2"}, defaultValue = "myService")
public interface MyService {

    String getName(Message message, String extend, int i);


    String getName();


    void getName(String name);


    List<Message> getNameList(Message message);
}
