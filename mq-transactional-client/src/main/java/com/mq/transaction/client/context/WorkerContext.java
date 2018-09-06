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
    private MqTransactionConfiguration mqTransactionConfiguration;
    private ExecutorService senderThreadPool;// 线程池
    private List<TerminalWorker> senderWorkList;
    private TerminalWorker selector;
    private TerminalWorker destroyer;
    private SqlSessionTemplate sqlSessionTemplate;
    private ActiveMqConnectionFactory activeMqConnectionFactory;
    private ArrayBlockingQueue<MqMessage> memoryMqMessageQueue = null;


    public WorkerContext(MqTransactionConfiguration mqTransactionConfiguration, MybatisSqlSessionFactory mybatisSqlSessionFactory, ActiveMqConnectionFactory activeMqConnectionFactory) {
        // 初始化内存队列
        this.memoryMqMessageQueue = new ArrayBlockingQueue<>(mqTransactionConfiguration.getMemoryMaxQueueSize());
        // 初始化mybatis
        this.sqlSessionTemplate = mybatisSqlSessionFactory.getSessionTemplate();
        // 初始化activemq
        this.activeMqConnectionFactory = activeMqConnectionFactory;
    }

    public void start() {
        //消费者：负责向mq broker发送数据
        Integer senderThreadCount = mqTransactionConfiguration.getSenderThreadCount();
        this.senderThreadPool = Executors.newFixedThreadPool(senderThreadCount);
        this.senderWorkList = new ArrayList<>(senderThreadCount);
        for (int i = 0; i < senderThreadCount; i++) {
            Sender sender = new Sender(sqlSessionTemplate, memoryMqMessageQueue, activeMqConnectionFactory);
            sender.setTableName(mqTransactionConfiguration.getTableName());
            this.senderWorkList.add(sender);
            this.senderThreadPool.submit(sender);
            logger.info("active activemq selector thread initial : ", i);
        }

        // FIXME:babalaba.sean@gmail.com 这里目前都是单线程，压测后根据实际结果进行调优

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
            logger.error("destroy exception :", e);
            throw new RuntimeException(e);
        }
    }


    public ArrayBlockingQueue<MqMessage> getMemoryMqMessageQueue(){
        return memoryMqMessageQueue;
    }
}

