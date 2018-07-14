package com.mq.transaction.springboot.autoconfigure;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;
import org.springframework.boot.context.properties.ConfigurationProperties;

/**
 * Configuration properties for Mq-Transaction.
 *
 * @author ZhangYongjia
 */

@Data
@AllArgsConstructor
@NoArgsConstructor
@ConfigurationProperties(prefix = "springboot.mq.transaction")
public class MqTransactionProperties {

    private Integer memoryMaxQueueSize;
    private Integer senderThreadCount;
    private Integer selectorThreadCount;
    private Integer destroyerThreadCount;
    private Integer expiredDayCount;
    private String queueTableName;
    private String brokerUrl;
    private boolean autoCreateTable;

}
