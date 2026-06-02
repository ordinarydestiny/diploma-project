package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("task_books")
public class TaskBook {
    @TableId(type = IdType.AUTO)
    private Integer taskId;
    private Integer selectionId;
    private String content;
    private Integer version;
    private String status;
    private Integer issuerId;
    private LocalDateTime issuedAt;
    private Integer rejectorId;
    private LocalDateTime rejectTime;
    private String rejectComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
