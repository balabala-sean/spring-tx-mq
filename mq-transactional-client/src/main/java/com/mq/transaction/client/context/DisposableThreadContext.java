package com.mq.transaction.client.context;

/**
 * 用于缓存一次Service#@Transactional方法中发送的MqMessages
 * Service方法运行完毕即丢弃的线程上下文对象
 * Service方法运行完会根据事务执行的状态去判断是否把MqMessage放置到WorkContext的memoryMqMessageQueue
 *
 * 详情可以查阅：
 *
 * @see DisposableCache#DisposableCache(java.util.concurrent.ArrayBlockingQueue, org.apache.ibatis.session.SqlSession)
 * @see WorkerContext#getMemoryMqMessageQueue()
 * @see com.mq.transaction.client.spring.MqTransactionSynchronization#afterCompletion(int)
 *
 * @author ZhangYongjia
 */
public class DisposableThreadContext {

    private static ThreadLocal<DisposableCache> threadCache = new ThreadLocal<>();

    public static DisposableCache getCurrentThreadCache() {
        return threadCache.get();
    }

    public static void setCurrentThreadCache(DisposableCache disposableCache) {
        if (null != threadCache.get()) {
            throw new NullPointerException("disposableCache must not be null");
        }
        threadCache.set(disposableCache);
    }

    public static void clear() {
        threadCache.remove();
    }

}
