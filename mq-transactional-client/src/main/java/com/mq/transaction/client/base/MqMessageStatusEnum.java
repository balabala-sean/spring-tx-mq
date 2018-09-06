package com.mq.transaction.client.base;

import lombok.Getter;


@Getter
public enum MqMessageStatusEnum {
    WAIT("WAIT_TO_SEND"),
    SELECTED("SELECTED_TO_MEMORY"),
    SUCCESS("SEND_SUCCESS"),
    FAILED("FAILED");

    private String status;

    MqMessageStatusEnum(String status) {
        this.status = status;
    }
}
