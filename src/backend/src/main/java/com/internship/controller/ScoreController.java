package com.internship.controller;

import com.internship.common.Result;
import com.internship.entity.FinalScore;
import com.internship.service.ScoreComputationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/scores")
@RequiredArgsConstructor
@Tag(name = "成绩管理")
public class ScoreController {

    private final ScoreComputationService scoreComputationService;

    @GetMapping("/selections/{selectionId}")
    @Operation(summary = "获取某学生总成绩")
    public Result<FinalScore> getScore(@PathVariable Integer selectionId) {
        // 实现查询逻辑
        return Result.success(null);
    }

    @PutMapping("/selections/{selectionId}/publish")
    @Operation(summary = "公布成绩")
    @PreAuthorize("hasRole('college_admin')")
    public Result<Void> publish(@PathVariable Integer selectionId) {
        scoreComputationService.publishScore(selectionId);
        return Result.success();
    }

    @PostMapping("/batches/{batchId}/calculate")
    @Operation(summary = "批量计算批次成绩")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    public Result<Void> batchCalculate(@PathVariable Integer batchId) {
        scoreComputationService.batchRecompute(batchId);
        return Result.success();
    }
}
