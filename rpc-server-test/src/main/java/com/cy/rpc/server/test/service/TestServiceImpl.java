package com.cy.rpc.server.test.service;

import com.cy.rpc.server.facade.entity.Message;
import com.cy.rpc.server.facade.service.TestService;
import org.springframework.stereotype.Service;

@Service("testService")
public class TestServiceImpl implements TestService {

    @Override
    public String getTestName(Message message, String extend, int i) {
        return extend;
    }
}
