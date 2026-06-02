package com.internship.dto;

import lombok.Data;

/**
 * 实习计划学生分配视图对象
 * 用于返回师生分配信息，包含学生和教师的关联名称
 */
@Data
public class InternshipPlanStudentVO {

    /** 分配记录ID */
    private Long id;

    /** 实习计划ID */
    private Long planId;

    /** 学生ID */
    private Long studentId;

    /** 指导教师ID */
    private Long teacherId;

    /** 学生姓名 */
    private String studentName;

    /** 学号 */
    private String studentNo;

    /** 班级名称 */
    private String className;

    /** 专业名称 */
    private String majorName;

    /** 教师姓名 */
    private String teacherName;

    /** 教师工号 */
    private String teacherNo;
}
