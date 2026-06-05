package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDate;
import java.time.LocalDateTime;

@Data
@TableName("project_batches")
public class ProjectBatch {
    @TableId(type = IdType.AUTO)
    private Integer batchId;
    private Integer majorId;
    private String batchName;
    private String batchCode;
    private String semester;        // 【新增】学期，如"2025-2026学年第1学期"
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal defenseRatio;
    private BigDecimal reportRatio;
    private String status;          // draft/active/finished
    private String currentPhase;    // 【新增】当前阶段: preparation/selection/taskbook/midterm/final/defense/finished
    private String description;
    private Integer creatorId;
    private LocalDateTime activatedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
