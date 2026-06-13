package com.internship.mapper;

import com.baomidou.mybatisplus.core.mapper.BaseMapper;
import com.internship.entity.File;
import org.apache.ibatis.annotations.Mapper;
import org.apache.ibatis.annotations.Select;

@Mapper
public interface FileMapper extends BaseMapper<File> {
    
    /**
     * 查询files表中最小的有效file_id
     * @return 文件实体（只包含fileId字段）
     */
    @Select("SELECT file_id FROM files WHERE is_deleted = 0 ORDER BY file_id ASC LIMIT 1")
    File selectMinFileId();
}
