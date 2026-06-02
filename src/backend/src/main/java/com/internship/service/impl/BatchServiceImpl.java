package com.internship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.entity.ProjectBatch;
import com.internship.mapper.ProjectBatchMapper;
import com.internship.service.BatchService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl extends ServiceImpl<ProjectBatchMapper, ProjectBatch> implements BatchService {

    private final ProjectBatchMapper batchMapper;

    @Override
    public ProjectBatch createBatch(ProjectBatch batch, Integer creatorId) {
        batch.setCreatorId(creatorId);
        batch.setStatus("draft");
        batch.setCreatedAt(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());
        
        save(batch);
        log.info("创建批次成功：{}，创建者ID：{}", batch.getBatchName(), creatorId);
        return batch;
    }

    @Override
    public IPage<ProjectBatch> listBatches(Integer page, Integer size, Integer majorId, String status) {
        Page<ProjectBatch> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<ProjectBatch> wrapper = new LambdaQueryWrapper<>();
        
        if (majorId != null) {
            wrapper.eq(ProjectBatch::getMajorId, majorId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(ProjectBatch::getStatus, status);
        }
        
        wrapper.orderByDesc(ProjectBatch::getCreatedAt);
        return page(pageParam, wrapper);
    }

    @Override
    public ProjectBatch getBatchDetail(Integer batchId) {
        ProjectBatch batch = getById(batchId);
        if (batch == null) {
            throw new RuntimeException("批次不存在");
        }
        return batch;
    }

    @Override
    public void updateBatch(Integer batchId, ProjectBatch batch) {
        ProjectBatch existing = getById(batchId);
        if (existing == null) {
            throw new RuntimeException("批次不存在");
        }
        
        batch.setBatchId(batchId);
        batch.setUpdatedAt(LocalDateTime.now());
        updateById(batch);
        log.info("更新批次成功，批次ID：{}", batchId);
    }

    @Override
    public void finishBatch(Integer batchId) {
        ProjectBatch batch = getById(batchId);
        if (batch == null) {
            throw new RuntimeException("批次不存在");
        }
        
        batch.setStatus("finished");
        batch.setFinishedAt(LocalDateTime.now());
        batch.setUpdatedAt(LocalDateTime.now());
        updateById(batch);
        log.info("结束批次成功，批次ID：{}", batchId);
    }
}