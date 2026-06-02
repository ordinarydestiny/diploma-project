package com.internship;

import lombok.extern.slf4j.Slf4j;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.scheduling.annotation.EnableScheduling;

/**
 * 实习与毕设管理系统启动类
 * 基于Spring Boot 3.2.5构建，集成Security、MyBatis-Plus、JWT等中间件
 */
@Slf4j
@EnableScheduling
@SpringBootApplication
public class InternshipApplication {

    public static void main(String[] args) {
        SpringApplication.run(InternshipApplication.class, args);
        log.info("====== 实习与毕设管理系统启动成功 ======");
    }
}
