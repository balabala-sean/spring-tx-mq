package com.balabala.spring.tx.dist.context;

import com.balabala.spring.tx.dist.activemq.ActiveMqConnectionFactory;
import com.balabala.spring.tx.dist.base.MqMessage;
import com.balabala.spring.tx.dist.conf.MqTransactionConfiguration;
import com.balabala.spring.tx.dist.context.worker.Destroyer;
import com.balabala.spring.tx.dist.context.worker.Selector;
import com.balabala.spring.tx.dist.context.worker.Sender;
import com.balabala.spring.tx.dist.context.worker.TerminalWorker;
import com.balabala.spring.tx.dist.mybatis.MybatisSqlSessionFactory;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/**
 * 全局的线程上下文
 */
public final class WorkerContext {

    private static Logger logger = LoggerFactory.getLogger(WorkerContext.class);

    private volatile boolean destroy = false;// 是否销毁
    private static final int DEFAULT_EXECUTOR_SERVICE_THREAD_NUMBER = 10;
    private static final int DEFAULT_MEMORY_MQ_QUEUE_SIZE = 5000;
    private ExecutorService consumerPool;// 线程池
    private List<TerminalWorker> consumerList;
    private TerminalWorker producer;
    private TerminalWorker destroyer;
    private SqlSessionTemplate sqlSessionTemplate;
    private ActiveMqConnectionFactory activeMqConnectionFactory;
    private ArrayBlockingQueue<MqMessage> memoryMqMessageQueue = null;


    public WorkerContext(MqTransactionConfiguration mqTransactionConfiguration, MybatisSqlSessionFactory mybatisSqlSessionFactory, ActiveMqConnectionFactory activeMqConnectionFactory) {
        this.sqlSessionTemplate = mybatisSqlSessionFactory.getSessionTemplate();
        this.memoryMqMessageQueue = new ArrayBlockingQueue<>(DEFAULT_MEMORY_MQ_QUEUE_SIZE);
        this.activeMqConnectionFactory = activeMqConnectionFactory;
    }

    public void start() {
        //消费者：负责向mq broker发送数据
        this.consumerPool = Executors.newFixedThreadPool(DEFAULT_EXECUTOR_SERVICE_THREAD_NUMBER);
        this.consumerList = new ArrayList<>(DEFAULT_EXECUTOR_SERVICE_THREAD_NUMBER);
        for (int i = 0; i < DEFAULT_EXECUTOR_SERVICE_THREAD_NUMBER; i++) {
            Sender sender = new Sender(sqlSessionTemplate, memoryMqMessageQueue, activeMqConnectionFactory);
            consumerList.add(sender);
            consumerPool.submit(sender);
            logger.info("active activemq producer thread initial : ", i);
        }

        //生产者：负责向memory queue中add数据
        producer = new Selector(sqlSessionTemplate, memoryMqMessageQueue);
        new Thread(producer).start();

        //消息销毁：删除历史3天的数据
        destroyer = new Destroyer(sqlSessionTemplate);
        new Thread(destroyer).start();

    }

    public synchronized void destory() {
        if (destroy) {
            return;
        }

        try {
            for (Iterator<TerminalWorker> iterator = consumerList.iterator(); iterator.hasNext(); ) {
                TerminalWorker consumer = iterator.next();
                consumer.terminate();
            }
            producer.terminate();
            destroyer.terminate();
            destroy = true;
        } catch (Exception e) {
            logger.error("desctory exception :", e);
            throw new RuntimeException(e);
        }
    }


    public ArrayBlockingQueue<MqMessage> getMemoryMqMessageQueue(){
        return memoryMqMessageQueue;
    }
}

