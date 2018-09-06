package com.mq.transaction.client.conf;

import lombok.Data;
import lombok.ToString;
import org.springframework.util.StringUtils;

import javax.sql.DataSource;

@Data
@ToString
public class Configuration {

    //默认的过期时间，单位是天
    private static final int DEFAULT_EXPIRED_DAY_COUNT = 3;

    //默认的消息消费线程数
    private static final int DEFAULT_SENDER_THREAD_COUNT = 10;

    //默认的消息生产线程数
    private static final int DEFAULT_SELECTOR_THREAD_COUNT = 1;

    //默认的消息销毁线程数
    private static final int DEFAULT_DESTROYER_THREAD_COUNT = 1;

    //默认的内存中存放的消息长度
    private static final int DEFAULT_MEMORY_MQ_QUEUE_SIZE = 5000;

    //默认的本地数据库表名称
    private static final String DEFAULT_MQ_TABLE_NAME = "transaction_mq_message";

    private Integer memoryMaxQueueSize = DEFAULT_MEMORY_MQ_QUEUE_SIZE;
    private Integer senderThreadCount = DEFAULT_SENDER_THREAD_COUNT;
    private Integer selectorThreadCount = DEFAULT_SELECTOR_THREAD_COUNT;
    private Integer destroyerThreadCount = DEFAULT_DESTROYER_THREAD_COUNT;
    private Integer expiredDayCount = DEFAULT_EXPIRED_DAY_COUNT;
    private DataSource dataSource;
    private String tableName = DEFAULT_MQ_TABLE_NAME;
    private String brokerUrl;
    private boolean autoCreateTable = false;


    public Configuration(DataSource dataSource, String brokerUrl) {
        this(DEFAULT_MEMORY_MQ_QUEUE_SIZE, DEFAULT_SENDER_THREAD_COUNT, DEFAULT_EXPIRED_DAY_COUNT, dataSource, DEFAULT_MQ_TABLE_NAME, brokerUrl, false);
    }


    public Configuration(Integer memoryMaxQueueSize, Integer senderThreadCount, Integer expiredDayCount, DataSource dataSource, String tableName, String brokerUrl, boolean autoCreateTable) {
        if (null == dataSource)  throw new IllegalArgumentException("dataSource must not be null");
        if (StringUtils.isEmpty(brokerUrl)) throw new IllegalArgumentException("brokerUrl must not be null");
        this.dataSource = dataSource;
        this.brokerUrl = brokerUrl;
        //use default value
        if (null != memoryMaxQueueSize) this.memoryMaxQueueSize = memoryMaxQueueSize;
        if (null != senderThreadCount) this.senderThreadCount = senderThreadCount;
        if (null != expiredDayCount) this.expiredDayCount = expiredDayCount;
        if (null != tableName) this.tableName = tableName;
        if (autoCreateTable) this.autoCreateTable = true;
    }
}
