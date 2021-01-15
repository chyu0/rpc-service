package com.cy.rpc.client.test.service;

import com.cy.rpc.server.facade2.FacadeServer;
import org.springframework.stereotype.Service;

@Service("facadeServer")
public class FacadeServerImpl implements FacadeServer {

    @Override
    public String queryName() {
        return "didididid";
    }
}
