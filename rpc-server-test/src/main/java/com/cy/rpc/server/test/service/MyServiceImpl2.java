package com.cy.rpc.server.test.service;

import com.cy.rpc.server.facade.entity.Message;
import com.cy.rpc.server.facade.service.MyService;
import org.springframework.stereotype.Service;

import java.util.List;

@Service("myService2")
public class MyServiceImpl2 implements MyService {


    @Override
    public String getName(Message message, String extend, int i) {
        return  message.getName();
    }

    @Override
    public String getName() {
        return "nonono";
    }

    @Override
    public void getName(String name) {
        System.out.println("MyServiceImpl2:" + name);
    }

    @Override
    public List<Message> getNameList(Message message) {
        return null;
    }
}
