package com.balabala.mq.transaction.example.conf;

import com.mq.transaction.client.MqTransactionClient;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Configuration;

import javax.annotation.PostConstruct;

@Configuration
public class Config {

    @Autowired
    private MqTransactionClient mqTransactionClient;

    @PostConstruct
    public void start(){
        mqTransactionClient.start();
    }
}
