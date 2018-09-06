package com.mq.transaction.client.context;

import com.mq.transaction.client.base.MqMessage;
import com.mq.transaction.client.spring.MqTransactionSynchronization;
import com.mq.transaction.client.spring.MqTransactionSynchronizationRegister;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 一次事务请求的线程级别缓存
 *
 * @see com.mq.transaction.client.spring.MqTransactionSynchronizationRegister#registe(org.springframework.transaction.support.TransactionSynchronization)
 *
 */
public class DisposableCache {

    private List<MqMessage> currentThreadMqMessages = new ArrayList<>();
    private SqlSession sqlSession;
    private ArrayBlockingQueue<MqMessage> memoryMqMessageQueue;
    private String mqMessageTableName;

    public DisposableCache(String mqMessageTableName, ArrayBlockingQueue<MqMessage> memoryMqMessageQueue, SqlSession sqlSession){
        if (null == sqlSession) throw new RuntimeException("SqlSession must not be null");
        if (null == memoryMqMessageQueue) throw new RuntimeException("memoryMqMessageQueue must not be null");
        this.mqMessageTableName = mqMessageTableName;
        this.sqlSession = sqlSession;
        this.memoryMqMessageQueue = memoryMqMessageQueue;

        // registe to spring transaction manager
        new MqTransactionSynchronizationRegister().registe(new MqTransactionSynchronization(this.mqMessageTableName));
    }

    public void addMqMessage(MqMessage mqMessage) {
        this.currentThreadMqMessages.add(mqMessage);
    }

    public List<MqMessage> getCurrentThreadMqMessages() {
        return currentThreadMqMessages;
    }

    public SqlSession getSqlSession(){
        return this.sqlSession;
    }

    public ArrayBlockingQueue<MqMessage> getMemoryMqMessageQueue(){
        return memoryMqMessageQueue;
    }

}
