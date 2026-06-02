package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("teacher_student_relations")
public class TeacherStudentRelation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer batchId;
    private Integer teacherId;
    private Integer studentId;
    private Integer assignedBy;
    private String importSource;
    private String remark;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
