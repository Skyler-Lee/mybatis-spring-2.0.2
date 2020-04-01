package org.mybatis.study.app;

import org.apache.ibatis.datasource.pooled.PooledDataSource;
import org.mybatis.spring.SqlSessionFactoryBean;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.ComponentScan;
import org.springframework.context.annotation.Configuration;

import javax.sql.DataSource;

@Configuration
@ComponentScan("org.mybatis.study")
@MapperScan("org.mybatis.study.mapper")
public class AppConfig {

  @Bean
  public DataSource getDataSource(){
    PooledDataSource dataSource = new PooledDataSource();
    dataSource.setDriver("com.mysql.cj.jdbc.Driver");
    dataSource.setUrl("jdbc:mysql://localhost:3306/exam?useUnicode=true&serverTimezone=UTC&useSSL=false&characterEncoding=utf8");
    dataSource.setUsername("root");
    dataSource.setPassword("lixujia");
    return dataSource;
  }

  @Bean
  @Autowired
  public SqlSessionFactoryBean getSqlSessionFactoryBean(DataSource dataSource){
    SqlSessionFactoryBean sqlSessionFactory =  new SqlSessionFactoryBean();
    sqlSessionFactory.setDataSource(dataSource);
    return sqlSessionFactory;
  }
}
