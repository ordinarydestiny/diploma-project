package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.TaskBook;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface TaskBookMapper extends BaseMapper<TaskBook> {
}
