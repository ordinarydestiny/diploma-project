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
    private List<RoleInfo> availableRoles;
    
    @Data
    public static class RoleInfo {
        private Integer roleId;
        private String roleCode;
        private String roleName;
    }
}
