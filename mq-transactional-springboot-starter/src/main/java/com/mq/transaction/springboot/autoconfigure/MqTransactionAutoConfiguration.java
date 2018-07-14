package com.mq.transaction.springboot.autoconfigure;

import com.mq.transaction.client.MqTransactionClient;
import com.mq.transaction.client.conf.MqTransactionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.condition.ConditionalOnMissingBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;
import org.springframework.jdbc.datasource.init.DataSourceInitializer;

import javax.sql.DataSource;

/**
 * SpringBoot项目需要注入MqTransactionClient, 并手动调用start/stop方法
 *
 * @see com.mq.transaction.client.MqTransactionClient#start()
 * @see com.mq.transaction.client.MqTransactionClient#stop()
 */

@Configuration
@ConditionalOnBean({DataSourceInitializer.class, DataSource.class})
@EnableConfigurationProperties(MqTransactionProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ComponentScan(
        basePackages = {
                "com.mq.transaction.springboot.autoconfigure",
                "com.mq.transaction.client"
        }
)
public class MqTransactionAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MqTransactionAutoConfiguration.class);

    @Autowired
    private MqTransactionProperties mqTransactionProperties;

    @Bean
    @ConditionalOnMissingBean
    public MqTransactionClient mqTransactionClient(DataSource dataSource) {
        logger.info("init bean {}", MqTransactionConfiguration.class.getCanonicalName());
        MqTransactionConfiguration mqTransactionConfiguration = new MqTransactionConfiguration(
                mqTransactionProperties.getMemoryMaxQueueSize(),
                mqTransactionProperties.getSenderThreadCount(),
                mqTransactionProperties.getSelectorThreadCount(),
                mqTransactionProperties.getDestroyerThreadCount(),
                mqTransactionProperties.getExpiredDayCount(),
                dataSource,
                mqTransactionProperties.getQueueTableName(),
                mqTransactionProperties.getBrokerUrl(),
                mqTransactionProperties.isAutoCreateTable()
        );

        MqTransactionClient mqTransactionClient = new MqTransactionClient(mqTransactionConfiguration);
        logger.info("init bean {}", MqTransactionClient.class.getCanonicalName());
        logger.info("begin start with configuration:  ");
        logger.info("-----------------------------------------------------------------");
        logger.info("brokerUrl:{}", mqTransactionProperties.getBrokerUrl());
        logger.info("memoryMaxQueueSize:{}", mqTransactionProperties.getMemoryMaxQueueSize());
        logger.info("senderThreadCount:{}", mqTransactionProperties.getSenderThreadCount());
        logger.info("selectorThreadCount:{}", mqTransactionProperties.getSelectorThreadCount());
        logger.info("destroyerThreadCount:{}", mqTransactionProperties.getDestroyerThreadCount());
        logger.info("expiredDayCount:{}", mqTransactionProperties.getExpiredDayCount());
        logger.info("queueTableName:{}", mqTransactionProperties.getQueueTableName());
        logger.info("autoCreateTable:{}", mqTransactionProperties.isAutoCreateTable());
        logger.info("-----------------------------------------------------------------");

        mqTransactionClient.start();
        return mqTransactionClient;
    }
}
