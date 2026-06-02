package com.internship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.List;

/**
 * 用户信息响应DTO
 * 封装获取当前用户信息接口返回的数据
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserInfoResponse {

    /** 用户ID */
    private Long userId;

    /** 用户名 */
    private String username;

    /** 真实姓名 */
    private String realName;

    /** 手机号 */
    private String phone;

    /** 邮箱 */
    private String email;

    /** 当前角色编码 */
    private String currentRoleCode;

    /** 当前角色名称 */
    private String currentRoleName;

    /** 当前角色ID */
    private Long currentRoleId;

    /** 所属院系ID（教师/院系管理员角色时有值） */
    private Long deptId;

    /** 用户拥有的所有角色列表 */
    private List<LoginResponse.RoleInfo> roles;
}
