package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("system_configs")
public class SystemConfig {
    @TableId(type = IdType.AUTO)
    private Integer configId;
    private String configKey;
    private String configValue;
    private String configType;
    private String description;
    private Integer updatedBy;
    private LocalDateTime updateTime;
}
