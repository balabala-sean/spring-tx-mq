package com.balabala.mq.transaction.example.controller;

import com.balabala.mq.transaction.example.entity.Foo;
import com.balabala.mq.transaction.example.service.FooService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
@RequestMapping("/foo")
public class FooContorller {

    @Autowired
    private FooService fooService;

    @PostMapping
    public void addFoo(Foo foo){
        fooService.biz(foo);
    }
}
