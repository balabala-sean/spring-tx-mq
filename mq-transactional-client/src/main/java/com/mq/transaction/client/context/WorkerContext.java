package com.mq.transaction.client.context;

import com.mq.transaction.client.activemq.ActiveMqConnectionFactory;
import com.mq.transaction.client.base.MqMessage;
import com.mq.transaction.client.conf.MqTransactionConfiguration;
import com.mq.transaction.client.context.worker.Destroyer;
import com.mq.transaction.client.context.worker.Selector;
import com.mq.transaction.client.context.worker.Sender;
import com.mq.transaction.client.context.worker.TerminalWorker;
import com.mq.transaction.client.mybatis.MybatisSqlSessionFactory;
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
    private MqTransactionConfiguration mqTransactionConfiguration;
    private ExecutorService senderThreadPool;// 线程池
    private List<TerminalWorker> senderWorkList;
    private TerminalWorker selector;
    private TerminalWorker destroyer;
    private SqlSessionTemplate sqlSessionTemplate;
    private ActiveMqConnectionFactory activeMqConnectionFactory;
    private ArrayBlockingQueue<MqMessage> memoryMqMessageQueue = null;


    public WorkerContext(MqTransactionConfiguration mqTransactionConfiguration, MybatisSqlSessionFactory mybatisSqlSessionFactory, ActiveMqConnectionFactory activeMqConnectionFactory) {
        this.sqlSessionTemplate = mybatisSqlSessionFactory.getSessionTemplate();
        this.initMemoryQueueSize(mqTransactionConfiguration.getMemoryMaxQueueSize());
        this.activeMqConnectionFactory = activeMqConnectionFactory;
    }

    private void initMemoryQueueSize(Integer memoryMaxQueueSize){
        memoryMaxQueueSize = null == memoryMaxQueueSize ? DEFAULT_MEMORY_MQ_QUEUE_SIZE : memoryMaxQueueSize;
        this.memoryMqMessageQueue = new ArrayBlockingQueue<>(memoryMaxQueueSize);
    }

    public void start() {
        //消费者：负责向mq broker发送数据
        this.senderThreadPool = Executors.newFixedThreadPool(DEFAULT_EXECUTOR_SERVICE_THREAD_NUMBER);
        this.senderWorkList = new ArrayList<>(DEFAULT_EXECUTOR_SERVICE_THREAD_NUMBER);
        for (int i = 0; i < DEFAULT_EXECUTOR_SERVICE_THREAD_NUMBER; i++) {
            Sender sender = new Sender(sqlSessionTemplate, memoryMqMessageQueue, activeMqConnectionFactory);
            sender.setTableName(mqTransactionConfiguration.getTableName());
            senderWorkList.add(sender);
            this.senderThreadPool.submit(sender);
            logger.info("active activemq selector thread initial : ", i);
        }

        //生产者：负责向memory queue中add数据
        selector = new Selector(sqlSessionTemplate, memoryMqMessageQueue);
        selector.setTableName(mqTransactionConfiguration.getTableName());

        new Thread(selector).start();

        //消息销毁：删除历史3天的数据
        destroyer = new Destroyer(sqlSessionTemplate, mqTransactionConfiguration.getExpiredDayCount());
        destroyer.setTableName(mqTransactionConfiguration.getTableName());

        new Thread(destroyer).start();

    }

    public synchronized void destory() {
        if (destroy) {
            return;
        }

        try {
            for (Iterator<TerminalWorker> iterator = senderWorkList.iterator(); iterator.hasNext(); ) {
                TerminalWorker consumer = iterator.next();
                consumer.terminate();
            }
            selector.terminate();
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

