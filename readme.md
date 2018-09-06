### mq-transactional - 基于本地消息解决分布式事务的中间件

mq-transactional提供了local message解决分布式事务的方式：

Features:
- 支持spring、springboot项目
- 扩展spring的事务处理，并遵循了spring的transction propagation，使用的时候不会影响required required_new required_nested等事务操作


Modules: 
- mq-transactional-client (支持springboot、非spring boot项目)
- mq-transactional-springboot-starter (仅支持springboot项目使用)

初始化sql脚本[ db-init.sql ]:
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
      expiredDayCount: 3
      queueTableName: mq_message #此处表名称和上面自行创建的表名称一致即可
      autoCreateTable: false
```

调用代码：
```
@Autowired
private MqTransactionClient mqTransactionClient;

@Transactional
public void orderCommit(Order order){
    //1. 本地库创建订单
    orderMapper.insert(order);
    //2. MQ发送支付请求到外部服务
    mqTransactionClient.send(new MqMessage("payment_request_queue", JSON.toJSONString(order)));
}
```