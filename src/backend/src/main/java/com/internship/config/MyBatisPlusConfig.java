package com.internship.config;

import com.baomidou.mybatisplus.annotation.DbType;
import com.baomidou.mybatisplus.extension.plugins.MybatisPlusInterceptor;
import com.baomidou.mybatisplus.extension.plugins.inner.PaginationInnerInterceptor;
import lombok.extern.slf4j.Slf4j;
import org.mybatis.spring.annotation.MapperScan;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * MyBatis-Plus配置类
 * 配置分页插件和Mapper扫描路径
 */
@Slf4j
@Configuration
@MapperScan("com.internship.mapper")
public class MyBatisPlusConfig {

    /**
     * MyBatis-Plus拦截器Bean
     * 添加MySQL分页插件，支持分页查询自动处理
     */
    @Bean
    public MybatisPlusInterceptor mybatisPlusInterceptor() {
        log.info("初始化MyBatis-Plus分页插件");
        MybatisPlusInterceptor interceptor = new MybatisPlusInterceptor();
        interceptor.addInnerInterceptor(new PaginationInnerInterceptor(DbType.MYSQL));
        return interceptor;
    }
}
