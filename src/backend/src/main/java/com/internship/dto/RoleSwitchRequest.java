package com.internship.dto;

import jakarta.validation.constraints.NotNull;
import lombok.Data;

/**
 * 角色切换请求DTO
 * 封装用户切换角色时提交的目标角色ID
 */
@Data
public class RoleSwitchRequest {

    /** 目标角色ID */
    @NotNull(message = "角色ID不能为空")
    private Long roleId;
}
