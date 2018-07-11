package com.balabala.spring.tx.dist.base;

import org.apache.ibatis.annotations.Param;

import java.util.Date;
import java.util.List;

public interface MqMessageMapper {

    int deleteByCreateTime(@Param("tableName") String tableName, @Param("createTime") Date createTime);

    int insert(MqMessage mqMessage);

    MqMessage selectById(String tableName, @Param("id") Long id);

    List<MqMessage> selectByNextRetryTime(@Param("tableName") String tableName, @Param("nextRetryTime") Date nextRetryTime, @Param("status") String status, @Param("start") Integer start, @Param("limit") Integer limit);

    int updateStatus(@Param("tableName") String tableName, MqMessage mqMessage);
}