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
    private String defenseScore;       // 五级制: excellent/good/medium/pass/fail
    private BigDecimal defenseScoreNum; // 数值分数
    private Integer recordFileId;
    private String submitterType;      // student/teacher
    private Integer submitterId;
    private LocalDateTime submitTime;
    private LocalDateTime defenseDatetime; // 【新增】答辩日期时间
    private String location;           // 【新增】答辩地点
    private String committee;          // 【新增】答辩委员会成员
    private String status;             // not_started/submitted/pending/approved/rejected
    private Integer reviewerId;
    private LocalDateTime reviewTime;
    private String reviewComment;      // 【修改】改为TEXT类型(≥10字)
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
