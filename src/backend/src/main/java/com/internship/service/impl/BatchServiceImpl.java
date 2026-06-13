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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Map;

@Slf4j
@Service
@RequiredArgsConstructor
public class BatchServiceImpl extends ServiceImpl<ProjectBatchMapper, ProjectBatch> implements BatchService {

    private final ProjectBatchMapper batchMapper;
    private final JdbcTemplate jdbcTemplate;

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
    public List<Map<String, Object>> listBatchesWithDetails(Integer majorId, String status) {
        StringBuilder sql = new StringBuilder("""
            SELECT 
                pb.batch_id,
                pb.major_id,
                pb.batch_name,
                pb.batch_code,
                pb.semester,
                pb.start_date,
                pb.end_date,
                pb.defense_ratio,
                pb.report_ratio,
                pb.status,
                pb.current_phase,
                pb.description,
                pb.creator_id,
                pb.created_at,
                pb.updated_at,
                m.major_name,
                CASE 
                    WHEN pb.semester IS NOT NULL THEN SUBSTRING_INDEX(pb.semester, '-', 1)
                    ELSE NULL
                END as grade,
                CASE 
                    WHEN pb.defense_ratio IS NOT NULL THEN CAST(pb.defense_ratio * 100 AS SIGNED)
                    ELSE 40
                END as defense_weight,
                CASE 
                    WHEN pb.report_ratio IS NOT NULL THEN CAST(pb.report_ratio * 100 AS SIGNED)
                    ELSE 60
                END as preview_weight,
                (SELECT COUNT(*) FROM teacher_student_relations tsr WHERE tsr.batch_id = pb.batch_id) as student_count
            FROM project_batches pb
            LEFT JOIN majors m ON pb.major_id = m.major_id
            WHERE 1=1
        """);
        
        if (majorId != null) {
            sql.append(" AND pb.major_id = ").append(majorId);
        }
        if (status != null && !status.isEmpty()) {
            sql.append(" AND pb.status = '").append(status).append("'");
        }
        
        sql.append(" ORDER BY pb.created_at DESC");
        
        return jdbcTemplate.queryForList(sql.toString());
    }

    @Override
    public Map<String, Object> getBatchDetailWithInfo(Integer batchId) {
        String sql = """
            SELECT 
                pb.*,
                m.major_name,
                CASE 
                    WHEN pb.semester IS NOT NULL THEN SUBSTRING_INDEX(pb.semester, '-', 1)
                    ELSE NULL
                END as grade,
                (SELECT COUNT(*) FROM teacher_student_relations tsr WHERE tsr.batch_id = pb.batch_id) as student_count
            FROM project_batches pb
            LEFT JOIN majors m ON pb.major_id = m.major_id
            WHERE pb.batch_id = ?
        """;
        
        List<Map<String, Object>> result = jdbcTemplate.queryForList(sql, batchId);
        if (result.isEmpty()) {
            throw new RuntimeException("批次不存在");
        }
        return result.get(0);
    }

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
    public void deleteBatch(Integer batchId) {
        ProjectBatch batch = getById(batchId);
        if (batch == null) {
            throw new RuntimeException("批次不存在");
        }

        // 级联删除：先删除该批次下的所有关联数据
        
        // 1. 删除师生关系
        Long relationCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM teacher_student_relations WHERE batch_id = ?",
            Long.class,
            batchId
        );
        
        if (relationCount != null && relationCount > 0) {
            jdbcTemplate.update(
                "DELETE FROM teacher_student_relations WHERE batch_id = ?",
                batchId
            );
            log.info("已删除批次 {} 下的 {} 条师生关系数据", batchId, relationCount);
        }
        
        // 2. 删除学生选题记录
        Long selectionCount = jdbcTemplate.queryForObject(
            "SELECT COUNT(*) FROM student_selections WHERE batch_id = ?",
            Long.class,
            batchId
        );
        
        if (selectionCount != null && selectionCount > 0) {
            jdbcTemplate.update(
                "DELETE FROM student_selections WHERE batch_id = ?",
                batchId
            );
            log.info("已删除批次 {} 下的 {} 条学生选题记录", batchId, selectionCount);
        }
        
        removeById(batchId);
        log.info("删除批次成功，批次ID：{}", batchId);
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
