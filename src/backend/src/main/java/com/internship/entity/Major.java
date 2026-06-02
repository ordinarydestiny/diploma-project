package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("majors")
public class Major {
    @TableId(type = IdType.AUTO)
    private Integer majorId;
    private Integer collegeId;
    private String majorName;
    private String majorCode;
    private Integer duration;
    private String degreeType;
    private LocalDateTime createdAt;
    private LocalDateTime updatedAt;
}
