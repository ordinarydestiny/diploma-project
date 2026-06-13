package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.FinalCheck;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FinalCheckMapper extends BaseMapper<FinalCheck> {
    
    @Select("SELECT COUNT(*) FROM final_checks WHERE selection_id = #{selectionId} AND is_final = 1")
    int existsBySelectionAndFinal(@Param("selectionId") Integer selectionId);
    
    /**
     * 根据学生ID和批次ID查询最终检查记录（已定稿版本）
     * @param studentId 学生ID
     * @param batchId 批次ID
     * @return 最终检查记录（如果存在）
     */
    @Select("SELECT fc.* FROM final_checks fc " +
            "INNER JOIN student_selections ss ON fc.selection_id = ss.selection_id " +
            "WHERE ss.student_id = #{studentId} AND ss.batch_id = #{batchId} " +
            "AND fc.is_final = 1 " +
            "ORDER BY fc.finalized_at DESC LIMIT 1")
    FinalCheck selectByStudentAndBatch(@Param("studentId") Integer studentId, 
                                        @Param("batchId") Integer batchId);
}
