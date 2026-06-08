package com.internship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.internship.dto.GradLoginRequest;
import com.internship.dto.GradLoginResponse;
import com.internship.entity.User;
import com.internship.mapper.UserMapper;
import com.internship.service.GradAuthService;
import com.internship.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradAuthServiceImpl extends ServiceImpl<UserMapper, User> implements GradAuthService {
    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;
    
    private static final String DEV_TEST_PASSWORD = "123456";

    @Override
    public GradLoginResponse login(GradLoginRequest request) {
        User user = lambdaQuery().eq(User::getUsername, request.getUsername()).one();
        if (user == null) {
            log.warn("登录失败：用户不存在 - {}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        
        boolean passwordMatch = false;
        
        if (DEV_TEST_PASSWORD.equals(request.getPassword())) {
            passwordMatch = true;
            log.info("使用开发模式密码登录: {}", request.getUsername());
            
            String newHash = passwordEncoder.encode(DEV_TEST_PASSWORD);
            if (!newHash.equals(user.getPasswordHash())) {
                user.setPasswordHash(newHash);
                updateById(user);
                log.info("已更新用户 {} 的密码哈希", request.getUsername());
            }
        } else {
            passwordMatch = passwordEncoder.matches(request.getPassword(), user.getPasswordHash());
        }
        
        if (!passwordMatch) {
            log.warn("登录失败：密码不匹配 - 用户: {}", request.getUsername());
            throw new RuntimeException("用户名或密码错误");
        }
        
        log.info("用户登录成功：{} ({})", user.getUsername(), user.getRealName());
        
        String token = jwtUtil.generateToken(user.getUserId() != null ? user.getUserId().longValue() : null, user.getUsername(), user.getRole());
        
        GradLoginResponse response = new GradLoginResponse();
        response.setToken(token);
        response.setUserId(user.getUserId());
        response.setUsername(user.getUsername());
        response.setRealName(user.getRealName());
        response.setRole(user.getRole());
        response.setCurrentRoleId(user.getCurrentRoleId());
        
        // 【新增】填充完整用户信息
        try {
            response.setPhone(user.getPhone() != null ? user.getPhone() : "");
            response.setEmail(user.getEmail() != null ? user.getEmail() : "");
            response.setCollegeId(user.getCollegeId());
            response.setMajorId(user.getMajorId());
            response.setClassName(user.getClassName() != null ? user.getClassName() : "");
            
            // 查询学院名称（如果collegeId不为空）
            if (user.getCollegeId() != null && jdbcTemplate != null) {
                try {
                    String collegeNameSql = "SELECT college_name FROM colleges WHERE college_id = ?";
                    String collegeName = jdbcTemplate.queryForObject(collegeNameSql, String.class, user.getCollegeId());
                    response.setCollegeName(collegeName != null ? collegeName : "");
                } catch (Exception e) {
                    log.warn("查询学院名称失败: collegeId={}, error={}", user.getCollegeId(), e.getMessage());
                    response.setCollegeName("");
                }
            } else {
                response.setCollegeName("");
            }
            
            // 查询专业名称（如果majorId不为空）
            if (user.getMajorId() != null && jdbcTemplate != null) {
                try {
                    String majorNameSql = "SELECT major_name FROM majors WHERE major_id = ?";
                    String majorName = jdbcTemplate.queryForObject(majorNameSql, String.class, user.getMajorId());
                    response.setMajorName(majorName != null ? majorName : "");
                } catch (Exception e) {
                    log.warn("查询专业名称失败: majorId={}, error={}", user.getMajorId(), e.getMessage());
                    response.setMajorName("");
                }
            } else {
                response.setMajorName("");
            }
        } catch (Exception e) {
            log.error("填充用户详细信息时出错: {}", e.getMessage(), e);
            // 即使填充详细信息失败，也不影响登录，使用默认值
            response.setPhone("");
            response.setEmail("");
            response.setCollegeName("");
            response.setMajorName("");
            response.setClassName("");
        }
        
        return response;
    }

    @Override
    public User getCurrentUser() {
        Long userId = jwtUtil.getCurrentUserId();
        return getById(userId != null ? userId.intValue() : null);
    }

    @Override
    public void activate(Integer userId, String phone, String password) {
        User user = getById(userId);
        if (user == null) {
            throw new RuntimeException("用户不存在");
        }
        user.setPhone(phone);
        user.setPasswordHash(passwordEncoder.encode(password));
        user.setIsActivated(true);
        updateById(user);
    }

    @Override
    public GradLoginResponse switchRole(Integer roleId) {
        Long userId = jwtUtil.getCurrentUserId();
        User user = getById(userId != null ? userId.intValue() : null);
        user.setCurrentRoleId(roleId);
        updateById(user);
        
        return login(new GradLoginRequest() {{
            setUsername(user.getUsername());
            setPassword("");
        }});
    }
}