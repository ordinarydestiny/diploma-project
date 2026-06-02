package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("operation_logs")
public class OperationLog {
    @TableId(type = IdType.AUTO)
    private Integer logId;
    private Integer userId;
    private String userRole;
    private String operation;
    private String targetType;
    private Integer targetId;
    private String detail;
    private String ipAddress;
    private String requestUrl;
    private String requestMethod;
    private String userAgent;
    private String result;
    private String errorMessage;
    private LocalDateTime createdAt;
}
