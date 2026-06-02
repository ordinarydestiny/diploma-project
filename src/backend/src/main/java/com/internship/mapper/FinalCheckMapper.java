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
}
