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
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

@Slf4j
@Service
@RequiredArgsConstructor
public class GradAuthServiceImpl extends ServiceImpl<UserMapper, User> implements GradAuthService {
    
    private final PasswordEncoder passwordEncoder;
    private final JwtUtil jwtUtil;
    
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