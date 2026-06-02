package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.StudentSelection;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface StudentSelectionMapper extends BaseMapper<StudentSelection> {
    
    @Select("SELECT MAX(version) FROM student_selections WHERE batch_id = #{batchId} AND student_id = #{studentId} AND deleted = 0")
    Integer getMaxVersion(@Param("batchId") Integer batchId, @Param("studentId") Integer studentId);
}
