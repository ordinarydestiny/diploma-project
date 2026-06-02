package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("colleges")
public class College {
    @TableId(type = IdType.AUTO)
    private Integer collegeId;
    private String collegeName;
    private String description;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
