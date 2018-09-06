package com.mq.transaction.client.spring;

import com.mq.transaction.client.base.MqMessage;
import com.mq.transaction.client.base.MqMessageMapper;
import com.mq.transaction.client.base.MqMessageStatusEnum;
import com.mq.transaction.client.context.DisposableCache;
import com.mq.transaction.client.context.DisposableThreadContext;
import org.apache.ibatis.session.SqlSession;
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
 * 这里遵循了spring的transaction propagation, 使用的时候不会影响required required_new required_nested等事务操作
 *
 * @author balabala.sean@gmail.com
 *
 */
public class MqTransactionSynchronization extends TransactionSynchronizationAdapter {

    private static Logger logger = LoggerFactory.getLogger(MqTransactionSynchronization.class);

    private String mqMessageTableName;

    public MqTransactionSynchronization(String mqMessageTableName){
        this.mqMessageTableName = mqMessageTableName;
    }

    @Override
    public void afterCompletion(int status) {
        String currentThreadName = Thread.currentThread().getName();
        Long currentThreadId = Thread.currentThread().getId();
        //判断Spring事务的状态
        if (TransactionSynchronization.STATUS_COMMITTED == 0){
            //transaction success
            logger.info("Thread[{}] : local-activemq-spring transaction exec success", currentThreadId);

            DisposableCache currentThreadCache = DisposableThreadContext.getCurrentThreadCache();
            List<MqMessage> currentThreadMqMessages = currentThreadCache.getCurrentThreadMqMessages();

            for (Iterator<MqMessage> iterator = currentThreadMqMessages.iterator(); iterator.hasNext(); ) {
                MqMessage mqMessage = iterator.next();
                currentThreadCache.getMemoryMqMessageQueue().add(mqMessage);
            }
        }

        if (TransactionSynchronization.STATUS_ROLLED_BACK == 1){
            logger.info("Thread[{}] : local-activemq-spring transaction was rollback", currentThreadId);
        }


        if (TransactionSynchronization.STATUS_UNKNOWN == 2){
            logger.info("Thread[{}] : local-activemq-spring transaction has a exception", currentThreadId);
        }

        logger.warn("Thread[{}] : clear thread context cache", currentThreadId);
        DisposableThreadContext.clear();

    }

    @Override
    public void beforeCommit(boolean readOnly) {
        if (readOnly){
            return;
        }

        DisposableCache currentThreadCache = DisposableThreadContext.getCurrentThreadCache();
        List<MqMessage> mqMessages = currentThreadCache.getCurrentThreadMqMessages();
        if (CollectionUtils.isEmpty(mqMessages)){
            return;
        }

        SqlSession sqlSession = currentThreadCache.getSqlSession();
        MqMessageMapper mqMessageMapper = new MqMessageMapper(sqlSession);
        try {
            for (Iterator<MqMessage> iterator = mqMessages.iterator(); iterator.hasNext(); ) {
                MqMessage mqMessage = iterator.next();
                mqMessage.setRetryCount(0);
                mqMessage.setTableName(mqMessageTableName);
                mqMessage.setCreateTime(new Timestamp(System.currentTimeMillis()));
                mqMessage.setUpdateTime(new Timestamp(System.currentTimeMillis()));
                mqMessage.setNextRetryTime(new Date());
                mqMessage.setStatus(MqMessageStatusEnum.WAIT.getStatus());
                mqMessage.setVersion(1);
                mqMessageMapper.insert(mqMessage);
                logger.info("Thread[{}]: insert mq-message to database,", Thread.currentThread().getId(), mqMessage);
            }
        }catch (Exception e){
            logger.error("insert activemq-message to database has a exception: ", e);
            throw new RuntimeException(e);
        }finally {
            sqlSession.close();
        }
    }
}
