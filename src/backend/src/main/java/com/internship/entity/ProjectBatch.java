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
    private LocalDate startDate;
    private LocalDate endDate;
    private BigDecimal defenseRatio;
    private BigDecimal reportRatio;
    private String status;
    private String description;
    private Integer creatorId;
    private LocalDateTime activatedAt;
    private LocalDateTime finishedAt;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
