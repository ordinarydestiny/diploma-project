package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("users")
public class User {
    @TableId(type = IdType.AUTO)
    private Integer userId;
    private String username;
    private String realName;
    private String phone;
    private String email;
    private String passwordHash;
    private String role;
    private Integer currentRoleId;
    private Integer collegeId;
    private Integer majorId;
    private String className;  // 【新增】班级名称
    private Boolean isActivated;
    private Boolean status;
    private String signaturePath;
    private String title;
    private LocalDateTime lastLogin;
    private String loginIp;
    private Integer failedLoginCount;
    private LocalDateTime lockedUntil;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
