package com.mq.transaction.client.base;

import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.session.SqlSession;

import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MqMessageMapper {

    private static final String STATEMENT_PREFIX = MqMessageMapper.class.getCanonicalName() + ".";

    private SqlSession sqlSession;

    public MqMessageMapper(SqlSession sqlSession){
        this.sqlSession = sqlSession;
    }

    public int deleteHistoryMqMessages(@Param("tableName") String tableName, @Param("createTime") Date createTime, @Param("status") String status){
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("createTime", createTime);
        param.put("status", status);

        int count = sqlSession.delete(STATEMENT_PREFIX + "deleteHistoryMqMessages", param);
        return count;
    }

    public int insert(MqMessage mqMessage){
        int count = sqlSession.insert(STATEMENT_PREFIX + "insert", mqMessage);
        return count;
    }

    public MqMessage selectById(@Param("tableName") String tableName, @Param("id") Long id){
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("id", id);

        MqMessage mqMessage = sqlSession.selectOne(STATEMENT_PREFIX + "selectById", param);
        return mqMessage;
    }

    public List<MqMessage> selectRetryMqMessageList(@Param("tableName") String tableName, @Param("nextRetryTime") Date nextRetryTime, @Param("status") String status, @Param("start") Integer start, @Param("limit") Integer limit){
        Map<String, Object> param = new HashMap<>();
        param.put("tableName", tableName);
        param.put("nextRetryTime", nextRetryTime);
        param.put("status", nextRetryTime);
        param.put("start", nextRetryTime);
        param.put("limit", nextRetryTime);

        List<MqMessage> mqMessages = sqlSession.selectList(STATEMENT_PREFIX + "selectRetryMqMessageList", param);
        return mqMessages;
    }

    public int updateRetryCount(MqMessage mqMessage){
        int count = sqlSession.update(STATEMENT_PREFIX + "updateRetryCount", mqMessage);
        return count;
    }

    public int updateStatus(MqMessage mqMessage){
        int count = sqlSession.update(STATEMENT_PREFIX + "updateStatus", mqMessage);
        return count;
    }
}