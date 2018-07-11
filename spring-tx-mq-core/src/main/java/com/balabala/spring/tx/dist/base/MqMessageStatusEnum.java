package com.balabala.spring.tx.dist.base;

import lombok.Getter;


@Getter
public enum MqMessageStatusEnum {
    WAIT("WAIT"),
    SUCCESS("SEND_SUCCESS"),
    FAILED("FAILED");

    private String status;

    MqMessageStatusEnum(String status) {
        this.status = status;
    }
}
