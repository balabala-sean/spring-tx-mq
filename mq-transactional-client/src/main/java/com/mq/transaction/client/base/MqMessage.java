package com.mq.transaction.client.base;

import lombok.Data;
import lombok.NoArgsConstructor;
import lombok.ToString;

import java.util.Date;


@NoArgsConstructor
@Data
@ToString
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

}
