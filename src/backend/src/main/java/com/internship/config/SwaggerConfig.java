package com.internship.config;

import io.swagger.v3.oas.models.OpenAPI;
import io.swagger.v3.oas.models.info.Info;
import io.swagger.v3.oas.models.info.Contact;
import io.swagger.v3.oas.models.security.SecurityRequirement;
import io.swagger.v3.oas.models.security.SecurityScheme;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;

/**
 * Swagger/OpenAPI文档配置类
 * 配置API文档基本信息和JWT安全认证方案，通过Knife4j提供可视化文档界面
 * 访问地址：http://localhost:8080/doc.html
 */
@Slf4j
@Configuration
public class SwaggerConfig {

    /**
     * 创建OpenAPI配置Bean
     * 设置文档标题、描述、版本号，以及Bearer Token安全方案
     */
    @Bean
    public OpenAPI openAPI() {
        log.info("初始化Swagger API文档配置");

        return new OpenAPI()
                .info(new Info()
                        .title("实习与毕设管理系统 API")
                        .description("实习与毕设管理系统后端接口文档")
                        .version("1.0.0")
                        .contact(new Contact()
                                .name("开发团队")))
                .addSecurityItem(new SecurityRequirement().addList("Bearer"))
                .schemaRequirement("Bearer", new SecurityScheme()
                        .type(SecurityScheme.Type.HTTP)
                        .scheme("bearer")
                        .bearerFormat("JWT")
                        .in(SecurityScheme.In.HEADER)
                        .name("Authorization"));
    }
}
