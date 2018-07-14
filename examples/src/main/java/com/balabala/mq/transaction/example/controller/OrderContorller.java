package com.balabala.mq.transaction.example.controller;

import com.balabala.mq.transaction.example.entity.Order;
import com.balabala.mq.transaction.example.service.OrderService;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RestController;

@RestController
public class OrderContorller {

    @Autowired
    private OrderService orderService;

    @PostMapping("/order-commit")
    public void commit(Order order){
        orderService.orderCommit(order);
    }
}
