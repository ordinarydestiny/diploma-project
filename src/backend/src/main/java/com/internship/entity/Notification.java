package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("notifications")
public class Notification {
    @TableId(type = IdType.AUTO)
    private Integer notifId;
    private Integer receiverId;
    private String title;
    private String content;
    private Boolean isRead;
    private LocalDateTime readAt;
    private String relatedType;
    private Integer relatedId;
    private String priority;
    private String category;
    private LocalDateTime createdAt;
}
