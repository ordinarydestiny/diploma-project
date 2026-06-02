package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.OperationLog;
import org.apache.ibatis.annotations.Mapper;

@Mapper
public interface OperationLogMapper extends BaseMapper<OperationLog> {
}
