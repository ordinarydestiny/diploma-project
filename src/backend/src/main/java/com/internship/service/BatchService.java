package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.internship.entity.ProjectBatch;

import java.util.List;
import java.util.Map;

public interface BatchService extends IService<ProjectBatch> {
    
    ProjectBatch createBatch(ProjectBatch batch, Integer creatorId);
    
    List<Map<String, Object>> listBatchesWithDetails(Integer majorId, String status);
    
    Map<String, Object> getBatchDetailWithInfo(Integer batchId);
    
    void updateBatch(Integer batchId, ProjectBatch batch);
    
    void deleteBatch(Integer batchId);
    
    void finishBatch(Integer batchId);
}
