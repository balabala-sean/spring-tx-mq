package com.mq.transaction.client.base;

import lombok.Getter;


@Getter
public enum MqMessageStatusEnum {
    WAIT("WAIT"),
    SELECTED("SELECTED"),
    SUCCESS("SEND_SUCCESS"),
    FAILED("FAILED");

    private String status;

    MqMessageStatusEnum(String status) {
        this.status = status;
    }
}
