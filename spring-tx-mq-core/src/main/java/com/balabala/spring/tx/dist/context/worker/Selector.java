package com.balabala.spring.tx.dist.context.worker;

import com.balabala.spring.tx.dist.base.MqMessage;
import com.balabala.spring.tx.dist.base.MqMessageStatusEnum;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.sql.Timestamp;
import java.util.Date;
import java.util.HashMap;
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
            // 抛异常后休眠一分钟再试，以免打印大量错误日志
            try {
                Thread.sleep(interval);
            } catch (InterruptedException e1) {
            }
        }
    }

    @Override
    protected void after() {
        // do nothing
    }


    public void selectMqMessageFromDatabase() {
        SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
        logger.debug("待发送的消息定时任务开始...");
        // 拿到表名开始任务
        String tableName = "mq_message";
        HashMap<String, Object> map = new HashMap<>();

        map.put("start", start);
        map.put("perIncrement", perIncrement);

        boolean islast = false;
        map.put("tableName", tableName);
        while (!islast) {
            logger.info("队列大小:{}", memoryMqMessageQueue.size());
            List<MqMessage> list = session.selectList("MqMessageMapper.selectNotSendMes", map);
            logger.debug("数据库中未处理集合大小:{}", list.size());
            if (list.size() == 0) {
                session.close();
                try {
                    Thread.sleep(interval);
                } catch (InterruptedException e) {
                    logger.error("activemq message producer exception:", e);
                }
                break;
            }
            if (list.size() < perIncrement) {
                islast = true;
            }

            for (MqMessage mqMessage : list) {
                mqMessage.setTableName(tableName);
                if (!mqMessage.getStatus().equals(MqMessageStatusEnum.WAIT.getStatus())) {
                    continue;
                }
                mqMessage.setUpdateTime(new Date());
                mqMessage.setRetryCount(mqMessage.getRetryCount() + 1);
                mqMessage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                int update = session.update("MqMessageMapper.updateTimes", mqMessage);
                logger.debug("update retry time of activemq message, id:{}, retryCount:{}", mqMessage.getId(), mqMessage.getRetryCount());
                if (update > 0) {
                    memoryMqMessageQueue.add(mqMessage);
                }

            }
        }
        logger.debug("待发送的消息定时任务结束");
    }
}
