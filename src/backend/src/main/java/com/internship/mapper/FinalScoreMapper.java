package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.FinalScore;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Param;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FinalScoreMapper extends BaseMapper<FinalScore> {
    
    @Select("SELECT * FROM final_scores WHERE selection_id = #{selectionId}")
    FinalScore getBySelectionId(@Param("selectionId") Integer selectionId);
}
