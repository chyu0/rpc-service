package com.cy.rpc.client.test.controller;

import com.cy.rpc.server.facade.service.MyService;
import com.cy.rpc.server.facade.service.TestService;
import com.cy.rpc.server.facade2.FacadeServer;
import lombok.extern.slf4j.Slf4j;
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
    private TestService testService;

    @Resource
    private FacadeServer facadeServer;

    @GetMapping("/test")
    public String test() {
        myService.getName("哈哈哈哈");
        myService2.getName("aaaaa");

        log.info("facadeServer:" + facadeServer.queryName());

        log.info("testService" + testService.getTestName(null, "ooo", 1));

        return myService.getName();

    }

}
