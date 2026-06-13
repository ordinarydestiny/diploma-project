package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.Defense;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface DefenseMapper extends BaseMapper<Defense> {
    
    /**
     * 根据选题ID查询答辩记录
     * @param selectionId 选题ID
     * @return 答辩记录（如果存在）
     */
    @Select("SELECT * FROM defenses WHERE selection_id = #{selectionId} LIMIT 1")
    Defense selectBySelectionId(Integer selectionId);
    
    /**
     * 根据学生ID和批次ID查询答辩记录
     * @param studentId 学生ID
     * @param batchId 批次ID
     * @return 答辩记录（如果存在）
     */
    @Select("SELECT d.* FROM defenses d " +
            "INNER JOIN student_selections ss ON d.selection_id = ss.selection_id " +
            "WHERE ss.student_id = #{studentId} AND ss.batch_id = #{batchId} " +
            "ORDER BY d.created_at DESC LIMIT 1")
    Defense selectByStudentAndBatch(@Param("studentId") Integer studentId, 
                                    @Param("batchId") Integer batchId);
}
