package com.cy.rpc.server.test.service;

import com.cy.rpc.server.facade.entity.Message;
import com.cy.rpc.server.facade.service.MyService;
import org.springframework.stereotype.Service;

import java.util.ArrayList;
import java.util.List;
import java.util.Random;

@Service("myService")
public class MyServiceImpl implements MyService {

    @Override
    public String getName(Message message, String extend, int i) {
        try {
            Thread.sleep(new Random().nextInt(1000));
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
        return message.getName() + extend + i;
    }

    @Override
    public String getName() {
        return "hahahaha";
    }

    @Override
    public void getName(String name) {
        System.out.println("MyServiceImpl:" + name);
    }

    @Override
    public List<Message> getNameList(Message message) {
        List<Message> messages = new ArrayList<>();
        messages.add(message);
        messages.add(message);
        return messages;
    }
}
