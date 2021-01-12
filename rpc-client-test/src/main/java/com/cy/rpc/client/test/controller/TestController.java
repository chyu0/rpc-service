package com.cy.rpc.client.test.controller;

import com.cy.rpc.server.facade.service.MyService;
import com.cy.rpc.server.facade2.FacadeServer;
import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import javax.annotation.Resource;

@RestController
@Slf4j
public class TestController {

    @Resource
    private MyService myService;

    @Resource
    private MyService myService2;

    @Resource
    private FacadeServer facadeServer;

    @GetMapping("/test")
    public String test() {
        myService.getName("哈哈哈哈");
        myService2.getName("aaaaa");

        facadeServer.queryName();

        return myService.getName();
    }



}
