package com.mq.transaction.client.context.worker;

import com.mq.transaction.client.activemq.ActiveMqConnectionFactory;
import com.mq.transaction.client.base.MqMessage;
import com.mq.transaction.client.base.MqMessageMapper;
import com.mq.transaction.client.base.MqMessageStatusEnum;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.*;
import java.sql.Timestamp;
import java.util.Date;
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
            MqMessage mqMessage = memoryMqMessageQueue.take();
            logger.debug("Thread[{}] : memory mq-message queue size:{}, get mq-message[{}] from memory queue", Thread.currentThread().getId(), memoryMqMessageQueue.size(), mqMessage.getId());
            this.sendMqMessage(mqMessage);
        } catch (InterruptedException e) {
            logger.error("send mq-message interrupted exception:", e);
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
        if (mqMessage.getStatus().equals(MqMessageStatusEnum.WAIT.getStatus())) {
            mqMessage.setStatus(MqMessageStatusEnum.SUCCESS.getStatus());
        }

        Session mqSession = activeMqConnectionFactory.createSession();
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
        MqMessageMapper mqMessageMapper = new MqMessageMapper(sqlSession);
        try {
            // 消息发送
            Destination sendDestination = mqSession.createQueue(mqMessage.getQueueName());
            MessageProducer producer = mqSession.createProducer(sendDestination);
            TextMessage textMessage = mqSession.createTextMessage(mqMessage.getMessage());
            producer.send(textMessage);

            // 消息发送成功:更新数据库
            mqMessage.setStatus(MqMessageStatusEnum.SUCCESS.getStatus());
            mqMessage.setSendSuccessTime(new Date());
            mqMessage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
            mqMessage.setTableName(super.getTableName());
            int update = mqMessageMapper.updateStatus(mqMessage);
            if (update == 1){
                logger.debug("send mq message success & update db status : {}", mqMessage.getId());
            }
        } catch (Exception e) {
            logger.error("send mq-message to external mq-broker exception:{}", e);
            if (mqMessage.getRetryCount() == 7) {
                mqMessage.setStatus(MqMessageStatusEnum.FAILED.getStatus());
                mqMessage.setSendSuccessTime(new Date());
                mqMessage.setSendFailedReason(e.getMessage());
                mqMessage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                mqMessage.setTableName(super.getTableName());
                int update = mqMessageMapper.updateStatus(mqMessage);
                if (update != 1) {
                    throw new RuntimeException("update mq-message status error" + mqMessage.getId());
                }
                logger.debug("send mq message failed & update db status : {}", mqMessage.getId());
            }
        } finally {
            sqlSession.close();
            try {
                mqSession.close();
            } catch (JMSException e) {
                logger.error("mq-broker close exception :", e);
            }
        }
    }
}