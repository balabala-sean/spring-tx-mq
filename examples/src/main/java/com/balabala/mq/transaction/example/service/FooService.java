package com.balabala.mq.transaction.example.service;

import com.alibaba.fastjson.JSON;
import com.balabala.mq.transaction.example.entity.Foo;
import com.balabala.mq.transaction.example.mapper.FooMapper;
import com.mq.transaction.client.MqTransactionClient;
import com.mq.transaction.client.base.MqMessage;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Service
public class FooService {

    @Autowired
    private FooMapper fooMapper;

    @Autowired
    private MqTransactionClient mqTransactionClient;

    // 执行foo insert操作之后，mq通知另外的系统进行其他后续操作
    @Transactional
    public void biz(Foo foo){
        fooMapper.insertFoo(foo);
        mqTransactionClient.send(new MqMessage("foo_queue", JSON.toJSONString(foo)));
    }
}
