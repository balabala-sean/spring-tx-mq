package com.mq.transaction.client.conf;

import lombok.Data;
import lombok.ToString;

import javax.sql.DataSource;

@Data
@ToString
public class MqTransactionConfiguration {

    //默认的过期时间，单位是天
    private static final int DEFAULT_EXPIRED_DAY_COUNT = 3;

    //默认的消息消费线程数
    private static final int DEFAULT_SENDER_THREAD_COUNT = 10;

    //默认的消息生产线程数
    private static final int DEFAULT_SELECTOR_THREAD_COUNT = 2;

    //默认的消息销毁线程数
    private static final int DEFAULT_DESTROYER_THREAD_COUNT = 1;

    //默认的本地数据库表名称
    private static final String DEFAULT_MQ_TABLE_NAME = "transaction_mq_message";

    private Integer memoryMaxQueueSize;
    private Integer senderThreadCount = DEFAULT_SENDER_THREAD_COUNT;
    private Integer selectorThreadCount = DEFAULT_SELECTOR_THREAD_COUNT;
    private Integer destroyerThreadCount = DEFAULT_DESTROYER_THREAD_COUNT;
    private Integer expiredDayCount = DEFAULT_EXPIRED_DAY_COUNT;
    private DataSource dataSource;
    private String tableName = DEFAULT_MQ_TABLE_NAME;
    private String brokerUrl;
    private boolean autoCreateTable = false;


    public MqTransactionConfiguration(Integer memoryMaxQueueSize, Integer senderThreadCount, Integer selectorThreadCount, Integer destroyerThreadCount, Integer expiredDayCount, DataSource dataSource, String tableName, String brokerUrl, boolean autoCreateTable) {
        this.memoryMaxQueueSize = memoryMaxQueueSize;
        this.senderThreadCount = senderThreadCount;
        this.selectorThreadCount = selectorThreadCount;
        this.destroyerThreadCount = destroyerThreadCount;
        this.expiredDayCount = expiredDayCount;
        this.dataSource = dataSource;
        this.tableName = tableName;
        this.brokerUrl = brokerUrl;
        this.autoCreateTable = autoCreateTable;
    }
}
