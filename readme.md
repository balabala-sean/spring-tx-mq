### spring-tx-mq




initial.sql:(local message table)
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
