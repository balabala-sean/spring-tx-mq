package com.mq.transaction.client.base;

import java.util.Calendar;
import java.util.Date;

public class RetryTimeCalculator {

    private static final int MINUTE_STEP = 5;

    public Date getNextRetryTime(Integer retryCount){

        Calendar calendar = Calendar.getInstance();
        calendar.setTime(new Date());
        if (retryCount >= 3){
            calendar.add(Calendar.MINUTE, retryCount * MINUTE_STEP);
        }else{
            calendar.add(Calendar.MINUTE, MINUTE_STEP);
        }

        return calendar.getTime();
    }
}
