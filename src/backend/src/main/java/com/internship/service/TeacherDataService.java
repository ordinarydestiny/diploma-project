package com.internship.service;

import java.util.List;
import java.util.Map;

/**
 * 指导教师数据查询服务接口
 * 提供教师视角的各种数据聚合查询
 */
public interface TeacherDataService {
    
    /**
     * 获取教师管理的所有学生列表（基于师生关系）
     */
    List<Map<String, Object>> getMyStudents(Integer teacherId);
    
    /**
     * 获取教师负责的所有题目
     */
    List<Map<String, Object>> getMyTopics(Integer teacherId);
    
    /**
     * 获取待审核的学生选题列表
     */
    List<Map<String, Object>> getPendingSelections(Integer teacherId);
    
    /**
     * 获取所有学生的选题状态
     */
    List<Map<String, Object>> getAllStudentSelections(Integer teacherId, String status);
    
    /**
     * 获取需要下达任务书的学生列表
     */
    List<Map<String, Object>> getStudentsNeedTaskBook(Integer teacherId);
    
    /**
     * 获取已下达的任务书列表
     */
    List<Map<String, Object>> getMyTaskBooks(Integer teacherId);
    
    /**
     * 获取待审核的中期检查报告
     */
    List<Map<String, Object>> getPendingMidtermChecks(Integer teacherId);
    
    /**
     * 获取所有的中期检查记录
     */
    List<Map<String, Object>> getAllMidtermChecks(Integer teacherId);
    
    /**
     * 获取待审核的最终检查报告
     */
    List<Map<String, Object>> getPendingFinalChecks(Integer teacherId);
    
    /**
     * 获取所有的最终检查记录
     */
    List<Map<String, Object>> getAllFinalChecks(Integer teacherId);
    
    /**
     * 获取答辩管理数据（录入/审核）
     */
    List<Map<String, Object>> getDefenseRecords(Integer teacherId, String batchId);
    
    /**
     * 获取学生的签到记录
     */
    List<Map<String, Object>> getStudentSignIns(Integer teacherId, Integer studentId, String dateRange);

    /**
     * 获取所有可用的学期列表（从project_batches去重）
     */
    List<Map<String, Object>> getAvailableSemesters(Integer teacherId);

    /**
     * 获取所有题目分类列表（从topics表的category字段去重）
     */
    List<String> getTopicCategories(Integer teacherId);
}
