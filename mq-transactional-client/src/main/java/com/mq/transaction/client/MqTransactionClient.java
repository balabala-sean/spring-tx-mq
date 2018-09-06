package com.mq.transaction.client;

import com.mq.transaction.client.conf.MqTransactionConfiguration;
import com.mq.transaction.client.context.WorkerContext;
import com.mq.transaction.client.context.DisposableThreadContext;
import com.mq.transaction.client.base.MqMessage;
import com.mq.transaction.client.activemq.ActiveMqConnectionFactory;
import com.mq.transaction.client.mybatis.MybatisSqlSessionFactory;
import com.mq.transaction.client.context.DisposableCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.stereotype.Component;

import java.util.Iterator;
import java.util.List;

@Component
public class MqTransactionClient {

    private static final Logger logger = LoggerFactory.getLogger(MqTransactionClient.class);

    private ActiveMqConnectionFactory activeMqConnectionFactory;
    private WorkerContext workerContext;
    private MqTransactionConfiguration mqTransactionConfiguration;
    private MybatisSqlSessionFactory mybatisSqlSessionFactory;

    public MqTransactionClient(MqTransactionConfiguration mqTransactionConfiguration){
        this.mqTransactionConfiguration = mqTransactionConfiguration;
    }

    public void send(MqMessage mqMessage){
        if (null == DisposableThreadContext.getCurrentThreadCache()){
            DisposableCache disposableCache = new DisposableCache(
                    mqTransactionConfiguration.getTableName(),
                    workerContext.getMemoryMqMessageQueue(),
                    mybatisSqlSessionFactory.getSessionTemplate()
            );
            DisposableThreadContext.setCurrentThreadCache(disposableCache);
        }
        DisposableThreadContext.getCurrentThreadCache().addMqMessage(mqMessage);
    }


    public void send(List<MqMessage> mqMessageList){
        for (Iterator<MqMessage> iterator = mqMessageList.iterator(); iterator.hasNext(); ) {
            MqMessage mqMessage = iterator.next();
            this.send(mqMessage);
        }
    }

    public void start(){
        logger.info("spring transaction-message client start");
        //database
        mybatisSqlSessionFactory = new MybatisSqlSessionFactory(mqTransactionConfiguration.getDataSource());

        //activemq
        activeMqConnectionFactory = new ActiveMqConnectionFactory(mqTransactionConfiguration.getBrokerUrl());
        activeMqConnectionFactory.start();

        //thread worker
        workerContext = new WorkerContext(mqTransactionConfiguration, mybatisSqlSessionFactory, activeMqConnectionFactory);
        workerContext.start();
    }

    public void stop(){
        mybatisSqlSessionFactory.getSessionTemplate().close();
        activeMqConnectionFactory.close();
        workerContext.destory();
    }
}
