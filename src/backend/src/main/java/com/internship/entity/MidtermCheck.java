package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("midterm_checks")
public class MidtermCheck {
    @TableId(type = IdType.AUTO)
    private Integer checkId;
    private Integer selectionId;
    private Integer fileId;
    private LocalDateTime submitTime;
    private Integer version;
    private Boolean isCurrent;
    private String status;
    private Integer reviewerId;
    private LocalDateTime reviewTime;
    private String reviewComment;
    private Integer guideFileId;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
