package com.mq.transaction.client.context.worker;

import com.mq.transaction.client.base.MqMessageMapper;
import com.mq.transaction.client.base.MqMessageStatusEnum;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import java.util.Calendar;
import java.util.Date;

public class Destroyer extends TerminalWorker {

    private static Logger logger = LoggerFactory.getLogger(Destroyer.class);

    private Integer interval = 60 * 1000;
    private Integer expiredDayCount;

    private SqlSessionTemplate sqlSessionTemplate;

    public Destroyer(SqlSessionTemplate sqlSessionTemplate, Integer expiredDayCount){
        this.sqlSessionTemplate = sqlSessionTemplate;
        this.expiredDayCount = expiredDayCount;
    }

    @Override
    protected void prepare() {
        // do nothing
    }

    @Override
    protected void work() {
        SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
        MqMessageMapper mqMessageMapper = new MqMessageMapper(session);
        try {
            Calendar calendar = Calendar.getInstance();
            calendar.setTime(new Date());
            calendar.add(Calendar.DAY_OF_MONTH, expiredDayCount);

            int i;
            i = mqMessageMapper.deleteHistoryMqMessages(super.tableName, calendar.getTime(), MqMessageStatusEnum.SUCCESS.getStatus());
            logger.info("delete last {} days history mq messages[successed], count : {}", expiredDayCount, i);

            i = mqMessageMapper.deleteHistoryMqMessages(super.tableName, calendar.getTime(), MqMessageStatusEnum.FAILED.getStatus());
            logger.info("delete last {} days history mq messages[failed], count : {}", expiredDayCount, i);

            Thread.currentThread().sleep(interval);
        } catch (Exception e) {
            logger.error("delete last {} days history mq messages[error]", e);
        } finally {
            session.close();
        }
    }

    @Override
    protected void after() {
        // do nothing
    }

}