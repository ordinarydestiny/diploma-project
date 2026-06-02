package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("files")
public class File {
    @TableId(type = IdType.AUTO)
    private Integer fileId;
    private String originalName;
    private String storagePath;
    private Long fileSize;
    private String mimeType;
    private String fileHash;
    private Integer uploaderId;
    private LocalDateTime uploadTime;
    private String ipAddress;
    private String relationType;
    private Integer relationId;
    private Boolean isDeleted;
    private LocalDateTime createdAt;
}
