package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import com.baomidou.mybatisplus.extension.handlers.JacksonTypeHandler;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName(value = "roles", autoResultMap = true)
public class Role {
    @TableId(type = IdType.AUTO)
    private Integer roleId;
    private String roleCode;
    private String roleName;
    @TableField(typeHandler = JacksonTypeHandler.class)
    private String permissions;
    private String description;
    private Integer sortOrder;
    private LocalDateTime createdAt;
}
