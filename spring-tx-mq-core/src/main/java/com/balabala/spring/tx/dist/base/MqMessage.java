package com.balabala.spring.tx.dist.base;

import java.util.Date;


public class MqMessage {
    private Long id;
    private String queueName;
    private String tableName;
    private String status;
    private String message;
    private Integer retryCount;
    private Date nextRetryTime;
    private Date createTime;
    private Date updateTime;
    private Date sendSuccessTime;
    private Date sendFailedTime;
    private String sendFailedReason;
    private Integer version;

    public MqMessage(String queueName, String message) {
        this.queueName = queueName;
        this.message = message;
    }


    public String getTableName() {
        return tableName;
    }

    public void setTableName(String tableName) {
        this.tableName = tableName;
    }

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    public String getQueueName() {
        return queueName;
    }

    public void setQueueName(String queueName) {
        this.queueName = queueName;
    }

    public String getStatus() {
        return status;
    }

    public void setStatus(String status) {
        this.status = status;
    }

    public String getMessage() {
        return message;
    }

    public void setMessage(String message) {
        this.message = message;
    }

    public Integer getRetryCount() {
        return retryCount;
    }

    public void setRetryCount(Integer retryCount) {
        this.retryCount = retryCount;
    }

    public Date getNextRetryTime() {
        return nextRetryTime;
    }

    public void setNextRetryTime(Date nextRetryTime) {
        this.nextRetryTime = nextRetryTime;
    }

    public Date getCreateTime() {
        return createTime;
    }

    public void setCreateTime(Date createTime) {
        this.createTime = createTime;
    }

    public Date getUpdateTime() {
        return updateTime;
    }

    public void setUpdateTime(Date updateTime) {
        this.updateTime = updateTime;
    }

    public Date getSendSuccessTime() {
        return sendSuccessTime;
    }

    public void setSendSuccessTime(Date sendSuccessTime) {
        this.sendSuccessTime = sendSuccessTime;
    }

    public Date getSendFailedTime() {
        return sendFailedTime;
    }

    public void setSendFailedTime(Date sendFailedTime) {
        this.sendFailedTime = sendFailedTime;
    }

    public String getSendFailedReason() {
        return sendFailedReason;
    }

    public void setSendFailedReason(String sendFailedReason) {
        this.sendFailedReason = sendFailedReason;
    }

    public Integer getVersion() {
        return version;
    }

    public void setVersion(Integer version) {
        this.version = version;
    }
}
