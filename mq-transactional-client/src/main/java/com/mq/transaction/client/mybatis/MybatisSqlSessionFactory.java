package com.mq.transaction.client.mybatis;

import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.session.ExecutorType;
import org.apache.ibatis.session.SqlSession;
import org.apache.ibatis.session.SqlSessionFactory;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.SqlSessionTemplate;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.support.PathMatchingResourcePatternResolver;
import org.springframework.core.io.support.ResourcePatternResolver;

import javax.sql.DataSource;

@Slf4j
public class MybatisSqlSessionFactory {

    private SqlSessionTemplate sqlSessionTemplate;

    public MybatisSqlSessionFactory(DataSource dataSource){
        SqlSessionFactoryBean sqlSessionFactoryBean = new SqlSessionFactoryBean();
        sqlSessionFactoryBean.setDataSource(dataSource);
        ResourcePatternResolver resolver = new PathMatchingResourcePatternResolver();
        sqlSessionFactoryBean.setConfigLocation(resolver.getResource("classpath:mybatis-conf.xml"));
        ClassPathResource[] classPathResources = new ClassPathResource[]{new ClassPathResource("com/mq/transaction/client/mapper/MqMessageMapper.xml")};
        sqlSessionFactoryBean.setMapperLocations(classPathResources);
        sqlSessionFactoryBean.setTypeAliasesPackage("typeAliasesPackage");
        try {
            SqlSessionFactory sqlSessionFactory = sqlSessionFactoryBean.getObject();
            this.sqlSessionTemplate = new SqlSessionTemplate(sqlSessionFactory);
        } catch (Exception e) {
            log.error("create mybatis sql-session factory failed, mq-client will exit(-1):", e);
            System.exit(-1);
        }
    }

    public SqlSessionTemplate getSessionTemplate() {
        return sqlSessionTemplate;
    }

    public SqlSession getSqlSession(){
        SqlSession sqlSession = sqlSessionTemplate.getSqlSessionFactory().openSession(ExecutorType.SIMPLE, false);
        return sqlSession;
    }
}
