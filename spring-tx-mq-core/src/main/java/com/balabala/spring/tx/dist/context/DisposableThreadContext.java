package com.balabala.spring.tx.dist.context;

import com.balabala.spring.tx.dist.base.MqMessage;
import com.balabala.spring.tx.dist.spring.MqTransactionThreadCache;
import org.apache.ibatis.session.SqlSession;

import java.util.List;
import java.util.concurrent.ArrayBlockingQueue;

/**
 * 用于缓存一次Service#@Transactional方法中发送的MqMessages
 * Service方法运行完毕即丢弃的线程上下文对象
 * Service方法运行完会根据事务执行的状态去判断是否把MqMessage放置到WorkContext的memoryMqMessageQueue
 *
 * 详情可以查阅：
 *
 * @see com.balabala.spring.tx.dist.spring.MqTransactionThreadCache#MqTransactionThreadCache(java.util.concurrent.ArrayBlockingQueue, org.apache.ibatis.session.SqlSession)
 * @see com.balabala.spring.tx.dist.context.WorkerContext#getMemoryMqMessageQueue()
 * @see com.balabala.spring.tx.dist.spring.MqTransactionSynchronization#afterCompletion(int)
 *
 * @author ZhangYongjia
 */
public class DisposableThreadContext {

    private static ThreadLocal<MqTransactionThreadCache> threadCache = new ThreadLocal<>();

    public static void addTranasctionalMqMessage(MqMessage mqMessage) {
        if (null == threadCache.get()) {
            throw new NullPointerException("thread cache was null");
        }
        checkCacheNotNull();
        threadCache.get().addMqMessage(mqMessage);
    }

    public static List<MqMessage> getCurrentThreadMqMessages() {
        checkCacheNotNull();
        return threadCache.get().getCurrentThreadMqMessages();
    }


    public static MqTransactionThreadCache getCurrentThreadCache() {
        return threadCache.get();
    }

    public static void setCurrentThreadCache(MqTransactionThreadCache mqTransactionThreadCache) {
        if (null != threadCache.get()) {
            return;
        }
        threadCache.set(mqTransactionThreadCache);
    }

    public static ArrayBlockingQueue<MqMessage> getMemoryMqMessageQueue() {
        return threadCache.get().getMemoryMqMessageQueue();
    }


    public static SqlSession openSqlSession() {
        checkCacheNotNull();
        return threadCache.get().getSqlSession();
    }

    public static void clear() {
        threadCache.remove();
    }

    private static void checkCacheNotNull() {
        if (null == threadCache.get()) {
            throw new NullPointerException("thread cache was null");
        }
    }
}
