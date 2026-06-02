package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.College;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface CollegeMapper extends BaseMapper<College> {
}
