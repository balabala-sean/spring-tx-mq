package com.balabala.spring.tx.dist.spring;

import com.balabala.spring.tx.dist.base.MqMessage;
import org.apache.ibatis.session.SqlSession;

import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 在Constructor上会把当前的线程注册到spring的TransactionManager
 *
 * @see com.balabala.spring.tx.dist.spring.MqTransactionSynchronizationRegister#registe(org.springframework.transaction.support.TransactionSynchronization)
 *
 */
public class MqTransactionThreadCache {

    private List<MqMessage> currentThreadMqMessages = new ArrayList<>();
    private SqlSession sqlSession;
    private ArrayBlockingQueue<MqMessage> memoryMqMessageQueue;

    public MqTransactionThreadCache(ArrayBlockingQueue<MqMessage> memoryMqMessageQueue, SqlSession sqlSession){
        if (null == sqlSession) throw new RuntimeException("SqlSession must not be null");
        if (null == memoryMqMessageQueue) throw new RuntimeException("memoryMqMessageQueue must not be null");

        this.sqlSession = sqlSession;
        this.memoryMqMessageQueue = memoryMqMessageQueue;

        // registe to spring transaction manager
        new MqTransactionSynchronizationRegister().registe(new MqTransactionSynchronization());
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
