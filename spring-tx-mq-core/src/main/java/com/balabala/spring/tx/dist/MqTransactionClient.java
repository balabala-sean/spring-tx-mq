package com.balabala.spring.tx.dist;

import com.balabala.spring.tx.dist.conf.MqTransactionConfiguration;
import com.balabala.spring.tx.dist.context.WorkerContext;
import com.balabala.spring.tx.dist.context.DisposableThreadContext;
import com.balabala.spring.tx.dist.base.MqMessage;
import com.balabala.spring.tx.dist.activemq.ActiveMqConnectionFactory;
import com.balabala.spring.tx.dist.mybatis.MybatisSqlSessionFactory;
import com.balabala.spring.tx.dist.spring.MqTransactionThreadCache;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Iterator;
import java.util.List;

public class MqTransactionClient {

    private static Logger logger = LoggerFactory.getLogger(MqTransactionClient.class);
    private ActiveMqConnectionFactory activeMqConnectionFactory;
    private WorkerContext workerContext;
    private MqTransactionConfiguration mqTransactionConfiguration;
    private MybatisSqlSessionFactory mybatisSqlSessionFactory;

    public MqTransactionClient(MqTransactionConfiguration mqTransactionConfiguration){
        this.mqTransactionConfiguration = mqTransactionConfiguration;
    }

    public void send(MqMessage mqMessage){
        if (null == DisposableThreadContext.getCurrentThreadCache()){
            MqTransactionThreadCache mqTransactionThreadCache = new MqTransactionThreadCache(
                    workerContext.getMemoryMqMessageQueue(),
                    mybatisSqlSessionFactory.getSessionTemplate()
            );
            DisposableThreadContext.setCurrentThreadCache(mqTransactionThreadCache);
        }
        DisposableThreadContext.addTranasctionalMqMessage(mqMessage);
    }


    public void send(List<MqMessage> mqMessageList){
        for (Iterator<MqMessage> iterator = mqMessageList.iterator(); iterator.hasNext(); ) {
            MqMessage mqMessage = iterator.next();
            this.send(mqMessage);
        }
    }

    public void start(){
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
