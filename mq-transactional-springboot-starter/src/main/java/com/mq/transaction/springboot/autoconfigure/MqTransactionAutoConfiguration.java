package com.mq.transaction.springboot.autoconfigure;

import com.mq.transaction.client.MqTransactionClient;
import com.mq.transaction.client.conf.MqTransactionConfiguration;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.autoconfigure.AutoConfigureAfter;
import org.springframework.boot.autoconfigure.condition.ConditionalOnBean;
import org.springframework.boot.autoconfigure.jdbc.DataSourceAutoConfiguration;
import org.springframework.boot.context.properties.EnableConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

/**
 * SpringBoot项目需要注入MqTransactionClient, 并手动调用start方法
 *
 * @see com.mq.transaction.client.MqTransactionClient#start()
 * @see com.mq.transaction.client.MqTransactionClient#stop()
 */
@Configuration
@ConditionalOnBean(DataSource.class)
@EnableConfigurationProperties(MqTransactionProperties.class)
@AutoConfigureAfter(DataSourceAutoConfiguration.class)
@ComponentScan("com.mq.transaction")
public class MqTransactionAutoConfiguration {

    private static final Logger logger = LoggerFactory.getLogger(MqTransactionAutoConfiguration.class);

    @Autowired
    private MqTransactionProperties mqTransactionProperties;

    @Autowired
    private DataSource dataSource;

    @Bean
    public MqTransactionClient mqTransactionClient() {
        MqTransactionConfiguration mqTransactionConfiguration = new MqTransactionConfiguration(
                mqTransactionProperties.getMemoryMaxQueueSize(),
                mqTransactionProperties.getSenderThreadCount(),
                mqTransactionProperties.getSelectorThreadCount(),
                mqTransactionProperties.getDestroyerThreadCount(),
                mqTransactionProperties.getExpiredDayCount(),
                dataSource,
                mqTransactionProperties.getTableName(),
                mqTransactionProperties.getBrokerUrl(),
                mqTransactionProperties.isAutoCreateTable()
        );

        MqTransactionClient mqTransactionClient = new MqTransactionClient(mqTransactionConfiguration);
        return mqTransactionClient;
    }

}
