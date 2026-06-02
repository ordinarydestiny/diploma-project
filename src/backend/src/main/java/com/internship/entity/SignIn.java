package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("sign_ins")
public class SignIn {
    @TableId(type = IdType.AUTO)
    private Integer signId;
    private Integer studentId;
    private Integer batchId;
    private LocalDate signDate;
    private String signTime;
    private String dailyReport;
    private Boolean isMakeup;
    private String makeupReason;
    private String signStatus;
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
