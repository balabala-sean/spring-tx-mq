package com.balabala.mq.transaction.example;

import com.balabala.mq.transaction.example.conf.Config;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.transaction.annotation.EnableTransactionManagement;

@SpringBootApplication(scanBasePackages={"com.balabala.mq.transaction.example"})
@EnableTransactionManagement
@MapperScan("com.balabala.mq.transaction.example.mapper")
public class ExampleApplication {
    public static void main(String[] args) {
        SpringApplication.run(Config.class, args);
    }
}
