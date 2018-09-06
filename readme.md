### mq-transactional - 基于本地消息解决分布式事务的中间件

mq-transactional支持spring、springboot项目，具体modules:
- mq-transactional-client 支持springboot、非spring boot项目
- mq-transactional-springboot-starter 仅支持springboot项目使用
- examples 调用案例

db-init.sql:(local message table)
```sql
create table mq_message (
  id                    BIGINT AUTO_INCREMENT PRIMARY KEY,
  queue_name            VARCHAR(256)                            NULL,
  status                VARCHAR(24)                             NULL,
  message               VARCHAR(1024)                           NULL,
  retry_count           INT(2)   DEFAULT '0'                    NULL,
  next_retry_time       DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL,
  send_success_time     DATETIME                                NULL,
  send_failed_time      DATETIME                                NULL,
  send_failed_reason    VARCHAR(256)                            NULL,
  version               INT(2) DEFAULT '1'                      NULL,
  create_time           DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL,
  update_time           DATETIME DEFAULT CURRENT_TIMESTAMP      NOT NULL
);
create index index_snr on mq_message (status, retry_count, next_retry_time);
```


调用方application.yml配置：
```
springboot:
  mq:
    transaction:
      brokerUrl: failover:(tcp://127.0.0.1:61616?wireFormat.maxInactivityDuration=0,tcp://127.0.0.1:61616?wireFormat.maxInactivityDuration=0)
      memoryMaxQueueSize: 5000
      senderThreadCount: 10
      selectorThreadCount: 1
      destroyerThreadCount: 1
      expiredDayCount: 3
      queueTableName: mq_message #此处表名称和上面自行创建的表名称一致即可
      autoCreateTable: false
```