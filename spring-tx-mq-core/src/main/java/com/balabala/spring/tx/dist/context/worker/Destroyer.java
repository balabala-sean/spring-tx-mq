package com.balabala.spring.tx.dist.context.worker;

import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

public class Destroyer extends TerminalWorker {

    private static Logger logger = LoggerFactory.getLogger(Destroyer.class);

    private Integer interval = 60 * 1000;

    private SqlSessionTemplate sqlSessionTemplate;

    public Destroyer(SqlSessionTemplate sqlSessionTemplate){
        this.sqlSessionTemplate = sqlSessionTemplate;
    }


    @Override
    protected void prepare() {
        // do nothing
    }

    @Override
    protected void work() {
        this.deleteHistoryMessages();
    }

    @Override
    protected void after() {
        // do nothing
    }

    private void deleteHistoryMessages() {
        SqlSession session = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
        String tableName = "mq_message";
        try {
            int count = session.delete("MqMessageMapper.", tableName);
            logger.info("delete last 3 days history activemq messages, count : {}", count);
        } catch (Exception e) {
            logger.error("删除数据出错" + e);
        } finally {
            session.close();
        }
    }

}