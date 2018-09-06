package com.mq.transaction.client.context.worker;

import com.mq.transaction.client.base.MqMessage;
import com.mq.transaction.client.base.MqMessageMapper;
import com.mq.transaction.client.base.MqMessageStatusEnum;
import com.mq.transaction.client.base.RetryTimeCalculator;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

public class Selector extends TerminalWorker {

    private static Logger logger = LoggerFactory.getLogger(Selector.class);

    private Integer interval = 60 * 1000;
    private int start = 0;
    private int perIncrement = 1000;

    private SqlSessionTemplate sqlSessionTemplate;
    private ArrayBlockingQueue<MqMessage> memoryMqMessageQueue;

    public Selector(SqlSessionTemplate sqlSessionTemplate, ArrayBlockingQueue<MqMessage> memoryMqMessageQueue){
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.memoryMqMessageQueue = memoryMqMessageQueue;
    }

    @Override
    protected void prepare() {
        // do nothing
    }

    @Override
    protected void work() {
        try {
            selectMqMessageFromDatabase();
        } catch (Exception e) {
            logger.error("后台补偿线程出现错误" + e.getMessage());
        }

        super.sleep(interval);
    }


    @Override
    protected void after() {
        // do nothing
    }


    public void selectMqMessageFromDatabase() {
        SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
        MqMessageMapper mqMessageMapper = new MqMessageMapper(session);
        logger.debug("待发送的消息定时任务开始");

        boolean end = false;
        Date nextRetryTime = new Date();
        while (!end) {
            List<MqMessage> list = mqMessageMapper.selectRetryMqMessageList(super.tableName, nextRetryTime, MqMessageStatusEnum.WAIT.getStatus(), start, perIncrement);

            logger.info("memory mq-message queue size:{}", memoryMqMessageQueue.size());
            logger.info("database select mq-message list size:{}", list.size());

            if (list.size() == 0) break;
            if (list.size() < perIncrement) end = true;

            for (MqMessage mqMessage : list) {
                mqMessage.setTableName(tableName);
                if (!mqMessage.getStatus().equals(MqMessageStatusEnum.WAIT.getStatus())) {
                    continue;
                }
                mqMessage.setUpdateTime(new Date());
                mqMessage.setRetryCount(mqMessage.getRetryCount() + 1);
                mqMessage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                mqMessage.setNextRetryTime(new RetryTimeCalculator().getNextRetryTime(mqMessage.getRetryCount() + 1));

                boolean flag = memoryMqMessageQueue.add(mqMessage);
                if (!flag){
                    logger.info("add mq-message [{}] from database to memory queue failed", mqMessage.getId());
                    continue;
                }
                int updateRow = mqMessageMapper.updateRetryCount(mqMessage);
                if (updateRow!=1) {
                    logger.info("add mq-message [{}] from database to memory queue failed", mqMessage.getId());
                    memoryMqMessageQueue.remove(mqMessage);
                }
            }
        }

        session.close();
        logger.debug("待发送的消息定时任务结束");
    }
}
