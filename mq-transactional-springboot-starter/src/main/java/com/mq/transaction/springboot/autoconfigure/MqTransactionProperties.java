package com.mq.transaction.springboot.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mq-Transaction.
 *
 * @author ZhangYongjia
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "mq.transaction")
public class MqTransactionProperties {

    @Value("${memory-max-queue-size}")
    private Integer memoryMaxQueueSize;

    @Value("${sender-thread-count}")
    private Integer senderThreadCount;

    @Value("${selector-thread-count}")
    private Integer selectorThreadCount;

    @Value("${destroyer-thread-count}")
    private Integer destroyerThreadCount;

    @Value("${expired-day-count}")
    private Integer expiredDayCount;

    @Value("${table-name}")
    private String tableName;

    @Value("${broker-url}")
    private String brokerUrl;

    @Value("${auto-create-table:false}")
    private boolean autoCreateTable;

}
