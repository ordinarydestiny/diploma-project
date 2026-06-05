package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("task_books")
public class TaskBook {
    @TableId(type = IdType.AUTO)
    private Integer taskId;
    private Integer selectionId;
    private String content;
    private LocalDate deadline;      // 【新增】完成期限
    private Integer version;
    private String requirements;     // 【新增】基本要求(JSON数组格式)
    private String techParams;       // 【新增】技术参数(JSON数组格式)
    @TableField("`references`")
    private String references;       // 【新增】参考资料（MySQL保留字，需加反引号）
    private String status;            // unissued/issued/confirmed/rejected
    private Integer issuerId;
    private LocalDateTime issuedAt;
    private Integer confirmBy;        // 【新增】确认接收的学生ID
    private LocalDateTime confirmAt;   // 【新增】确认接收时间
    private Integer rejectorId;
    private LocalDateTime rejectTime;
    private String rejectComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
