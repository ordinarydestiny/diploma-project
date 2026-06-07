package com.internship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.common.Result;
import com.internship.entity.ProjectBatch;
import com.internship.service.BatchService;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/batches")
@RequiredArgsConstructor
@Tag(name = "毕设批次管理")
public class BatchController {

    private final BatchService batchService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "新建批次")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    public Result<ProjectBatch> createBatch(@RequestBody ProjectBatch batch) {
        Long creatorId = jwtUtil.getCurrentUserId();
        return Result.success(batchService.createBatch(batch, creatorId != null ? creatorId.intValue() : null));
    }

    @GetMapping
    @Operation(summary = "批次列表")
    public Result<List<Map<String, Object>>> listBatches(
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) String status) {
        return Result.success(batchService.listBatchesWithDetails(majorId, status));
    }

    @GetMapping("/{batchId}")
    @Operation(summary = "批次详情")
    public Result<Map<String, Object>> getDetail(@PathVariable Integer batchId) {
        return Result.success(batchService.getBatchDetailWithInfo(batchId));
    }

    @PutMapping("/{batchId}")
    @Operation(summary = "修改批次")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    public Result<Void> updateBatch(@PathVariable Integer batchId, @RequestBody ProjectBatch batch) {
        batchService.updateBatch(batchId, batch);
        return Result.success();
    }

    @DeleteMapping("/{batchId}")
    @Operation(summary = "删除批次")
    @PreAuthorize("hasRole('college_admin')")
    public Result<Void> deleteBatch(@PathVariable Integer batchId) {
        batchService.deleteBatch(batchId);
        return Result.success();
    }

    @PutMapping("/{batchId}/finish")
    @Operation(summary = "结束批次")
    @PreAuthorize("hasRole('college_admin')")
    public Result<Void> finishBatch(@PathVariable Integer batchId) {
        batchService.finishBatch(batchId);
        return Result.success();
    }
}
