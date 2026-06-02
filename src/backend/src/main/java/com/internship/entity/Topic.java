package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("topics")
public class Topic {
    @TableId(type = IdType.AUTO)
    private Integer topicId;
    private String topicName;
    private String description;
    private String difficulty;
    private String category;
    private Integer creatorId;
    private Integer selectionCount;
    private Integer maxStudents;
    private String status;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
