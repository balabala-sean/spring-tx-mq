package com.balabala.spring.tx.dist.spring;

import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.transaction.support.TransactionSynchronization;
import org.springframework.transaction.support.TransactionSynchronizationManager;

class MqTransactionSynchronizationRegister {

    private static Logger logger = LoggerFactory.getLogger(MqTransactionSynchronizationRegister.class);

    //注册自定义的事务同步器
    public void registe(TransactionSynchronization transactionSynchronization){
        // 判断当前事务是否是激活状态的
        boolean actualTransactionActive = TransactionSynchronizationManager.isActualTransactionActive();
        if (!actualTransactionActive) {return;}

        // 注册到spring
        TransactionSynchronizationManager.registerSynchronization(transactionSynchronization);
        logger.info("registe {} to TransactionSynchronizationManager(spring framework tx package)", transactionSynchronization.getClass().getSimpleName());
    }

}
