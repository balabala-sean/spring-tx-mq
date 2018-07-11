package com.balabala.spring.tx.dist.context.worker;

import com.alibaba.fastjson.JSON;
import com.balabala.spring.tx.dist.activemq.ActiveMqConnectionFactory;
import com.balabala.spring.tx.dist.base.MqMessage;
import com.balabala.spring.tx.dist.base.MqMessageStatusEnum;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.Destination;
import javax.jms.JMSException;
import javax.jms.MessageProducer;
import javax.jms.Session;
import javax.jms.TextMessage;
import java.sql.Timestamp;
import java.util.concurrent.ArrayBlockingQueue;

public class Sender extends TerminalWorker {

    private static Logger logger = LoggerFactory.getLogger(Sender.class);

    private SqlSessionTemplate sqlSessionTemplate;
    private ArrayBlockingQueue<MqMessage> memoryMqMessageQueue;
    private ActiveMqConnectionFactory activeMqConnectionFactory;

    public Sender(SqlSessionTemplate sqlSessionTemplate, ArrayBlockingQueue<MqMessage> memoryMqMessageQueue, ActiveMqConnectionFactory activeMqConnectionFactory){
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.memoryMqMessageQueue = memoryMqMessageQueue;
        this.activeMqConnectionFactory = activeMqConnectionFactory;
    }


    @Override
    protected void prepare() {
        // do nothing
    }

    @Override
    protected void work() {
        try {
            logger.debug("线程{}等待从队列取消息", Thread.currentThread().getName());
            MqMessage mqMessage = memoryMqMessageQueue.take();
            logger.debug("线程{}从队列取到消息{},队列长度是:{}", Thread.currentThread().getName(), JSON.toJSONString(mqMessage), memoryMqMessageQueue.size());
            this.sendMqMessage(mqMessage);
        } catch (InterruptedException e) {
            logger.error("队列发送消息中断异常" + e.getMessage());
        }
    }

    @Override
    protected void after() {
        // do nothing
    }


    /**
     * 这里有两种方案可以选:
     * <p>
     * 1. 一种是先发消息再更新表，
     * 2. 一种是先更新表再发消息，先发消息会在队列消息没发完的时候重复发消息，例如queue里面有500条每消费，多个客户端都有，其他客户端端又把同样的消息
     * 加入queue，先更新表queue会造成突然间应用程序宕机丢掉一条消息，这些都是在消息积压的情况下发生的，概率很小，我选择的是后者
     */
    public void sendMqMessage(MqMessage mqMessage) {
        Session mqSession = activeMqConnectionFactory.createSession();
        // 消息成功更新数据库发送出去
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
        if (mqMessage.getStatus().equals(MqMessageStatusEnum.WAIT.getStatus())) {
            mqMessage.setStatus(MqMessageStatusEnum.SUCCESS.getStatus());
        }
        mqMessage.setUpdateTime(new Timestamp(System.currentTimeMillis()));

        try {
            Destination sendDestination = mqSession.createQueue(mqMessage.getQueueName());
            MessageProducer producer = mqSession.createProducer(sendDestination);
            TextMessage tmessage = mqSession.createTextMessage(mqMessage.getMessage());
            producer.send(tmessage);
            int update = sqlSession.update("MqMessageMapper.update", mqMessage);
            logger.debug("发送消息前更新数据库结果{}", update);

        } catch (Exception e) {
            logger.error("发送消息到mq出现异常", e);
        } finally {
            sqlSession.close();
            try {
                mqSession.close();
            } catch (JMSException e) {
                logger.error("jms session 关闭异常{}", e.getMessage());
            }
        }
    }
}