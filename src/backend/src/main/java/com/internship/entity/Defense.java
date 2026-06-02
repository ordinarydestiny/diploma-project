package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;

@Data
@TableName("defenses")
public class Defense {
    @TableId(type = IdType.AUTO)
    private Integer defenseId;
    private Integer selectionId;
    private String defenseScore;
    private BigDecimal defenseScoreNum;
    private Integer recordFileId;
    private String submitterType;
    private Integer submitterId;
    private LocalDateTime submitTime;
    private String status;
    private Integer reviewerId;
    private LocalDateTime reviewTime;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
