package com.mq.transaction.client.activemq;

import org.apache.activemq.ActiveMQConnection;
import org.apache.activemq.ActiveMQConnectionFactory;
import org.apache.activemq.pool.PooledConnection;
import org.apache.activemq.pool.PooledConnectionFactory;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;

import javax.jms.JMSException;
import javax.jms.Session;

public class ActiveMqConnectionFactory {

    private static Logger logger = LoggerFactory.getLogger(ActiveMqConnectionFactory.class);

    private String brokerUrls;
    private static PooledConnection connection;
    private static PooledConnectionFactory pooledConnectionFactory;

    public ActiveMqConnectionFactory(String brokerUrls) {
        this.brokerUrls = brokerUrls;
    }

    // 初始化连接池等工作
    public void start() {
        ActiveMQConnectionFactory activeMQConnectionFactory = new ActiveMQConnectionFactory();
        // 设置发送消息为同步
        activeMQConnectionFactory.setUseAsyncSend(false);
        activeMQConnectionFactory.setUserName(ActiveMQConnection.DEFAULT_USER);
        activeMQConnectionFactory.setPassword(ActiveMQConnection.DEFAULT_PASSWORD);
        activeMQConnectionFactory.setBrokerURL(brokerUrls);
        try {

            pooledConnectionFactory = new PooledConnectionFactory(activeMQConnectionFactory);
            pooledConnectionFactory.setMaxConnections(200);
            pooledConnectionFactory.setBlockIfSessionPoolIsFull(true);
            connection = (PooledConnection) pooledConnectionFactory.createConnection();
            connection.start();
        } catch (JMSException e) {
            logger.error("activemq exception:", e);
            System.exit(-1);
        }
    }

    public void close() {
        try {
            if (connection != null) {
                connection.stop();
            }
        } catch (JMSException e) {
            logger.error("activemq exception:", e);
        }
    }

    public PooledConnection getConnection() {
        try {
            connection = (PooledConnection) pooledConnectionFactory.createConnection();
            connection.start();
        } catch (JMSException e) {
            logger.error("activemq exception:", e);
        }
        return connection;
    }

    public void setConnection(PooledConnection connection) {
        ActiveMqConnectionFactory.connection = connection;
    }

    public Session createSession() {
        try {
            return connection.createSession(false, Session.AUTO_ACKNOWLEDGE);
        } catch (JMSException e) {
            logger.error("activemq exception:", e);
        }
        return null;
    }
}
