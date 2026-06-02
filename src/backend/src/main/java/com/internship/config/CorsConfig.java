package com.internship.config;

import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.web.cors.CorsConfiguration;
import org.springframework.web.cors.UrlBasedCorsConfigurationSource;
import org.springframework.web.filter.CorsFilter;

/**
 * 跨域配置类
 * 配置CORS过滤器，允许前端开发服务器跨域访问后端API
 */
@Slf4j
@Configuration
public class CorsConfig {

    /**
     * 创建CORS过滤器Bean
     * 允许所有来源、所有请求头、所有HTTP方法的跨域请求
     * 暴露Authorization响应头，供前端读取JWT Token
     *
     * @return CorsFilter实例
     */
    @Bean
    public CorsFilter corsFilter() {
        log.info("初始化CORS跨域配置");

        CorsConfiguration config = new CorsConfiguration();
        config.setAllowCredentials(true);
        config.addAllowedOriginPattern("*");
        config.addAllowedHeader("*");
        config.addAllowedMethod("*");
        config.addExposedHeader("Authorization");

        UrlBasedCorsConfigurationSource source = new UrlBasedCorsConfigurationSource();
        source.registerCorsConfiguration("/**", config);

        return new CorsFilter(source);
    }
}
