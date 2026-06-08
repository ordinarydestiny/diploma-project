package com.internship.dto;

import lombok.Data;
import java.util.List;

@Data
public class GradLoginResponse {
    private String token;
    private Integer userId;
    private String username;
    private String realName;
    private String role;
    private Integer currentRoleId;
    
    // 【新增】完整用户信息字段
    private String phone;
    private String email;
    private Integer collegeId;
    private Integer majorId;
    private String className;
    private String gender;
    
    // 【新增】关联名称（用于显示）
    private String collegeName;  // 学院名称
    private String majorName;    // 专业名称
    
    private List<RoleInfo> availableRoles;
    
    @Data
    public static class RoleInfo {
        private Integer roleId;
        private String roleCode;
        private String roleName;
    }
}
