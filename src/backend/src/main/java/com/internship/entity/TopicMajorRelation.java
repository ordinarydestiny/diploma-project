package com.internship.entity;

import com.baomidou.mybatisplus.annotation.*;
import lombok.Data;
import java.time.LocalDateTime;

@Data
@TableName("topic_major_relations")
public class TopicMajorRelation {
    @TableId(type = IdType.AUTO)
    private Integer id;
    private Integer topicId;
    private Integer majorId;
    private Integer includedBy;
    private LocalDateTime includedAt;
    private String remark;
}
