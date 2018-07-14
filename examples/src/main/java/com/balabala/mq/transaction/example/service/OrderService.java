package com.balabala.mq.transaction.example.service;

import com.alibaba.fastjson.JSON;
import com.balabala.mq.transaction.example.entity.Order;
import com.balabala.mq.transaction.example.mapper.OrderMapper;
import com.mq.transaction.client.MqTransactionClient;
import com.mq.transaction.client.base.MqMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class OrderService {

    @Autowired
    private OrderMapper orderMapper;

    @Autowired
    private MqTransactionClient mqTransactionClient;

    /**
     * 提交订单：
     * 1. 本地库创建订单
     * 2. MQ发送支付请求到外部服务
     *
     * @param order 订单
     */
    @Transactional
    public void orderCommit(Order order){
        orderMapper.insert(order);

        mqTransactionClient.send(new MqMessage("payment_request_queue", JSON.toJSONString(order)));
    }
}
