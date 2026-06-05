package com.internship.config;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.common.Result;
import com.internship.common.ResultCode;
import jakarta.servlet.http.HttpServletResponse;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.http.HttpMethod;
import org.springframework.security.config.annotation.method.configuration.EnableMethodSecurity;
import org.springframework.security.config.annotation.web.builders.HttpSecurity;
import org.springframework.security.config.annotation.web.configuration.EnableWebSecurity;
import org.springframework.security.config.annotation.web.configurers.AbstractHttpConfigurer;
import org.springframework.security.config.http.SessionCreationPolicy;
import org.springframework.security.crypto.bcrypt.BCryptPasswordEncoder;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.security.web.SecurityFilterChain;
import org.springframework.security.web.authentication.UsernamePasswordAuthenticationFilter;

/**
 * Spring Security安全配置类
 * 配置JWT无状态认证、路径权限控制、异常处理等安全策略
 */
@Slf4j
@Configuration
@EnableWebSecurity
@EnableMethodSecurity
@RequiredArgsConstructor
public class SecurityConfig {

    private final JwtAuthenticationFilter jwtAuthenticationFilter;
    private final ObjectMapper objectMapper;

    /**
     * 密码编码器Bean
     * 使用BCrypt算法进行密码哈希，强度为10轮
     */
    @Bean
    public PasswordEncoder passwordEncoder() {
        log.info("初始化BCrypt密码编码器");
        return new BCryptPasswordEncoder();
    }

    /**
     * 安全过滤链配置
     * 1. 禁用CSRF（前后端分离项目使用JWT，无需CSRF保护）
     * 2. 无状态Session管理（不使用服务端Session）
     * 3. 配置路径权限：白名单路径放行，管理接口需ADMIN角色，其余需认证
     * 4. 配置认证失败和权限不足的JSON响应
     * 5. 在UsernamePasswordAuthenticationFilter之前插入JWT过滤器
     */
    @Bean
    public SecurityFilterChain securityFilterChain(HttpSecurity http) throws Exception {
        log.info("初始化Spring Security安全配置");

        http
                .csrf(AbstractHttpConfigurer::disable)
                .sessionManagement(session -> session.sessionCreationPolicy(SessionCreationPolicy.STATELESS))
                .authorizeHttpRequests(auth -> auth
                        .requestMatchers(
                                "/api/auth/**",
                                "/api/v1/auth/login",
                                "/api/v1/auth/register",
                                "/api/v1/test/**",
                                "/api/v1/files/download/**",
                                "/api/v1/files/templates/**",
                                "/doc.html",
                                "/swagger-ui.html",
                                "/swagger-ui/**",
                                "/swagger-ui/index.html",
                                "/swagger-resources/**",
                                "/v3/api-docs/**",
                                "/v3/api-docs/swagger-config",
                                "/webjars/**",
                                "/favicon.ico"
                        ).permitAll()
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/semesters/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/grades/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/majors/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/classes/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/teachers/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR")
                        .requestMatchers(HttpMethod.GET, "/api/v1/admin/students/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR")
                        .requestMatchers("/api/v1/admin/**").hasAnyRole("ADMIN", "DEPT_ADMIN")
                        .requestMatchers(HttpMethod.GET, "/api/v1/internship/plans/my").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR", "TEACHER", "STUDENT")
                        .requestMatchers(HttpMethod.GET, "/api/v1/internship/plans/my-plans").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/plans/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR", "TEACHER")
                        .requestMatchers("/api/v1/internship/plan-students/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "MAJOR_DIRECTOR", "TEACHER")
                        .requestMatchers("/api/v1/internship/applications/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/records/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/checkin/reminders/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/checkin/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/reports/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/final-reports/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/inner-assessments/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/outer-assessments/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/guidance-records/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER")
                        .requestMatchers("/api/v1/internship/visit-records/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER")
                        .requestMatchers("/api/v1/internship/student-changes/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/statistics/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER")
                        .requestMatchers(HttpMethod.GET, "/api/v1/internship/scores/my").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .requestMatchers("/api/v1/internship/scores/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER")
                        .requestMatchers("/api/v1/graduation/**").authenticated()
                        .requestMatchers("/api/v1/messages/**").hasAnyRole("ADMIN", "DEPT_ADMIN", "TEACHER", "STUDENT")
                        .anyRequest().authenticated()
                )
                .exceptionHandling(ex -> ex
                        .authenticationEntryPoint((request, response, authException) -> {
                            log.warn("未认证访问: {} {}", request.getMethod(), request.getRequestURI());
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_UNAUTHORIZED);
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    Result.fail(ResultCode.UNAUTHORIZED)
                            ));
                        })
                        .accessDeniedHandler((request, response, accessDeniedException) -> {
                            log.warn("权限不足: {} {}", request.getMethod(), request.getRequestURI());
                            response.setContentType("application/json;charset=UTF-8");
                            response.setStatus(HttpServletResponse.SC_FORBIDDEN);
                            response.getWriter().write(objectMapper.writeValueAsString(
                                    Result.fail(ResultCode.FORBIDDEN)
                            ));
                        })
                )
                .addFilterBefore(jwtAuthenticationFilter, UsernamePasswordAuthenticationFilter.class);

        return http.build();
    }
}
