package com.internship.service;

import com.internship.dto.MyGraduationDTO;

/**
 * 学生个人毕设信息查询服务接口
 * 提供学生视角的完整毕设数据聚合查询
 */
public interface StudentGraduationService {
    
    /**
     * 获取学生个人完整的毕设信息
     * 包含：批次、选题、任务书、中期检查、最终检查、答辩、成绩等全部信息
     *
     * @param studentId 学生用户ID
     * @return 完整的毕设信息DTO，如果没有选题记录则部分字段为null
     */
    MyGraduationDTO getMyGraduationInfo(Integer studentId);
}
