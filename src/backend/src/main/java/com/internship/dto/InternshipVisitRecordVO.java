package com.internship.dto;

import lombok.Data;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 实习巡访记录视图对象
 * 包含联查的教师姓名、学生姓名列表、计划名称
 */
@Data
public class InternshipVisitRecordVO {

    private Long id;

    private Long planId;

    private Long teacherId;

    private LocalDate visitDate;

    private String visitType;

    private String location;

    private String topic;

    private String content;

    private String evaluation;

    private String studentFeedback;

    private String photoUrl;

    private String attachmentPath;

    private Integer deleted;

    private LocalDateTime createTime;

    private LocalDateTime updateTime;

    /** 教师姓名 */
    private String teacherName;

    /** 学生姓名列表（逗号分隔） */
    private String studentNames;

    /** 学生姓名 */
    private String studentName;

    /** 学生ID列表 */
    private List<Long> studentIds;

    /** 计划名称 */
    private String planName;

    /** 实习单位名称 */
    private String companyName;
}
