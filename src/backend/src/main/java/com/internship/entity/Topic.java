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
    private Integer difficulty;  // 【修改】从String改为Integer (1-5星)
    private String category;
    private String topicType;   // 【新增】题目类型: research/engineering/thesis
    private String source;      // 【新增】题目来源: teacher/student/enterprise
    private String requirements;// 【新增】技术要求详情
    @TableField("`references`")
    private String references;  // 【新增】参考文献（references是MySQL保留字，需加反引号）
    private Integer creatorId;
    private Integer selectionCount;
    private Integer maxStudents;
    private String status;       // available/unavailable/archived
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
