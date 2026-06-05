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
    private String signTime;          // 签到具体时间 (HH:MM:SS)
    private String location;         // 【新增】签到地点
    private String dailyReport;
    private Boolean isMakeup;
    private String makeupReason;
    private String signStatus;       // normal/late/absent
    private String signOutTime;      // 【新增】签退时间
    private Integer durationMinutes; // 【新增】停留时长(分钟)
    private String ipAddress;
    private String userAgent;
    private LocalDateTime createdAt;
}
