package com.cy.rpc.server.test.controller;

import com.cy.rpc.server.facade.service.MyService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.io.IOException;

@RestController
public class TestController {

    @Autowired
    private MyService myService;

    @GetMapping("/test")
    public String test() throws IOException {
        myService.getName();

        return "success";
    }
}
