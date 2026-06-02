package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("student_selections")
public class StudentSelection {
    @TableId(type = IdType.AUTO)
    private Integer selectionId;
    private Integer batchId;
    private Integer studentId;
    private Integer topicId;
    private String topicType;
    private String selfTopicName;
    private String selfTopicDesc;
    private Integer version;
    private Integer previousSelectionId;
    private String status;
    private Integer reviewerId;
    private LocalDateTime reviewTime;
    private String reviewComment;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
