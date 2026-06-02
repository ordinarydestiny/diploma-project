package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.internship.dto.GradLoginRequest;
import com.internship.dto.GradLoginResponse;
import com.internship.entity.User;

public interface GradAuthService extends IService<User> {
    
    GradLoginResponse login(GradLoginRequest request);
    
    User getCurrentUser();
    
    void activate(Integer userId, String phone, String password);
    
    GradLoginResponse switchRole(Integer roleId);
}
