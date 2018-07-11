package com.balabala.spring.tx.dist.conf;

import javax.sql.DataSource;

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

    private Integer senderThreadCount = DEFAULT_SENDER_THREAD_COUNT;
    private Integer selectorThreadCount = DEFAULT_SELECTOR_THREAD_COUNT;
    private Integer destroyerThreadCount = DEFAULT_DESTROYER_THREAD_COUNT;
    private Integer expiredDayCount = DEFAULT_EXPIRED_DAY_COUNT;
    private DataSource dataSource;
    private String tableName = DEFAULT_MQ_TABLE_NAME;
    private String brokerUrl;
    private boolean autoCreateTable = false;


    public DataSource getDataSource() {
        return dataSource;
    }

    public void setDataSource(DataSource dataSource) {
        this.dataSource = dataSource;
    }

    public Integer getSenderThreadCount() {
        return senderThreadCount;
    }

    public void setSenderThreadCount(Integer senderThreadCount) {
        this.senderThreadCount = senderThreadCount;
    }

    public Integer getSelectorThreadCount() {
        return selectorThreadCount;
    }

    public void setSelectorThreadCount(Integer selectorThreadCount) {
        this.selectorThreadCount = selectorThreadCount;
    }

    public Integer getDestroyerThreadCount() {
        return destroyerThreadCount;
    }

    public void setDestroyerThreadCount(Integer destroyerThreadCount) {
        this.destroyerThreadCount = destroyerThreadCount;
    }

    public Integer getExpiredDayCount() {
        return expiredDayCount;
    }

    public void setExpiredDayCount(Integer expiredDayCount) {
        this.expiredDayCount = expiredDayCount;
    }

    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public String getBrokerUrl() {
        return brokerUrl;
    }

    public void setBrokerUrl(String brokerUrl) {
        this.brokerUrl = brokerUrl;
    }

    public boolean isAutoCreateTable() {
        return autoCreateTable;
    }

    public void setAutoCreateTable(boolean autoCreateTable) {
        this.autoCreateTable = autoCreateTable;
    }
}
