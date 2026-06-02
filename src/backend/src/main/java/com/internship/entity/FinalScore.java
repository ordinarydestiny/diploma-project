package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("final_scores")
public class FinalScore {
    @TableId(type = IdType.AUTO)
    private Integer scoreId;
    private Integer selectionId;
    private Integer reportScore;
    private Integer defenseScore;
    private BigDecimal totalScore;
    private String gradeLevel;
    private Boolean isPublished;
    private Integer publishedBy;
    private LocalDateTime publishedAt;
    private LocalDateTime calculatedAt;
    private String calculationMode;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
