package com.internship.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.internship.entity.ProjectBatch;

public interface BatchService extends IService<ProjectBatch> {
    
    ProjectBatch createBatch(ProjectBatch batch, Integer creatorId);
    
    IPage<ProjectBatch> listBatches(Integer page, Integer size, Integer majorId, String status);
    
    ProjectBatch getBatchDetail(Integer batchId);
    
    void updateBatch(Integer batchId, ProjectBatch batch);
    
    void finishBatch(Integer batchId);
}
