package com.balabala.spring.tx.dist.spring;

import com.balabala.spring.tx.dist.context.DisposableThreadContext;
import com.balabala.spring.tx.dist.base.MqMessageStatusEnum;
import com.balabala.spring.tx.dist.base.MqMessage;
import org.apache.ibatis.session.SqlSession;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationAdapter;
import org.springframework.util.CollectionUtils;

import java.sql.Timestamp;
import java.util.Date;
import java.util.Iterator;
import java.util.List;

/**
 * 扩展Spring的TransactionSynchronization
 *
 * 在事务提交前，对当前线程中的MqMessage执行insert操作
 * @see #beforeCommit
 *
 * 在事务提交后，对事务的状态进行判断，如果事务成功提交，则把当前线程中的MqMessage放到全局的memoryMqMessageQueue中
 * @see #afterCompletion(int)
 *
 * @author ZhangYongjia
 *
 */
public class MqTransactionSynchronization extends TransactionSynchronizationAdapter {

    private static Logger logger = LoggerFactory.getLogger(MqTransactionSynchronization.class);

    private SqlSessionTemplate sqlSessionTemplate;

    @Override
    public void afterCompletion(int status) {
        String currentThreadName = Thread.currentThread().getName();
        Long currentThreadId = Thread.currentThread().getId();
        //判断Spring事务的状态
        if (TransactionSynchronization.STATUS_COMMITTED == 0){
            //transaction success
            logger.info("Thread[{}-{}] local-activemq-spring success", currentThreadName, currentThreadId);
            List<MqMessage> currentThreadMqMessages = DisposableThreadContext.getCurrentThreadMqMessages();
            for (Iterator<MqMessage> iterator = currentThreadMqMessages.iterator(); iterator.hasNext(); ) {
                MqMessage mqMessage = iterator.next();
                DisposableThreadContext.getMemoryMqMessageQueue().add(mqMessage);
            }
        }

        if (TransactionSynchronization.STATUS_ROLLED_BACK == 1){
            logger.info("Thread[{}-{}] local-activemq-spring rollback", currentThreadName, currentThreadId);
        }


        if (TransactionSynchronization.STATUS_UNKNOWN == 2){
            logger.warn("Thread[{}-{}] local-activemq-spring exception", currentThreadName, currentThreadId);
        }

        logger.warn("Thread[{}-{}] clear thread context cache", currentThreadName, currentThreadId);
        DisposableThreadContext.clear();

    }

    @Override
    public void beforeCommit(boolean readOnly) {
        if (readOnly){
            return;
        }

        List<MqMessage> mqMessages = DisposableThreadContext.getCurrentThreadMqMessages();
        if (CollectionUtils.isEmpty(mqMessages)){
            return;
        }

        SqlSession sqlSession = DisposableThreadContext.openSqlSession();
        try {
            for (Iterator<MqMessage> iterator = mqMessages.iterator(); iterator.hasNext(); ) {
                MqMessage mqMessage = iterator.next();
                mqMessage.setRetryCount(0);
                mqMessage.setTableName("mq_message");
                mqMessage.setCreateTime(new Timestamp(System.currentTimeMillis()));
                mqMessage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                mqMessage.setNextRetryTime(new Date());
                mqMessage.setStatus(MqMessageStatusEnum.WAIT.getStatus());
                mqMessage.setVersion(1);
                sqlSession.insert("MqMessageMapper.insert", mqMessage);
                logger.info("Thread[{}] has activemq message:{}", Thread.currentThread().getId(), mqMessage);
            }
        }catch (Exception e){
            logger.error("insert activemq-message to database has a exception: ", e);
            throw new RuntimeException(e);
        }finally {
            sqlSession.close();
        }
    }
}
