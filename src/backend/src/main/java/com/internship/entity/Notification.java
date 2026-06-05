package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Integer notifId;
    private Integer receiverId;       // NULL=全员广播
    private Integer publisherId;      // 【新增】发布人用户ID
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String status;            // 【新增】draft/published/revoked
    private LocalDateTime publishedAt; // 【新增】发布时间
    private String relatedType;
    private Integer relatedId;
    private String priority;          // low/normal/high/urgent
    private String category;          // system/business/reminder
    private LocalDateTime createdAt;
}
