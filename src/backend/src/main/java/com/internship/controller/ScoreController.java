package com.internship.controller;

import com.internship.common.Result;
import com.internship.entity.FinalScore;
import com.internship.service.ScoreComputationService;
import com.internship.service.TeacherDataService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/v1/scores")
@RequiredArgsConstructor
@Tag(name = "成绩管理")
public class ScoreController {

    private final ScoreComputationService scoreComputationService;
    private final TeacherDataService teacherDataService;

    @GetMapping("/list")
    @Operation(summary = "获取学生总成绩列表")
    public Result<List<Map<String, Object>>> getScoreList() {
        List<Map<String, Object>> scores = teacherDataService.getAllStudentScores();
        return Result.success(scores);
    }

    @GetMapping("/test")
    @Operation(summary = "测试接口（无需认证）")
    public Result<String> test() {
        return Result.success("成绩管理接口正常工作！");
    }

    @GetMapping("/selections/{selectionId}")
    @Operation(summary = "获取某学生总成绩")
    public Result<FinalScore> getScore(@PathVariable Integer selectionId) {
        return Result.success(null);
    }

    @PutMapping("/selections/{selectionId}/publish")
    @Operation(summary = "公布成绩")
    public Result<Void> publish(@PathVariable Integer selectionId) {
        scoreComputationService.publishScore(selectionId);
        return Result.success();
    }

    @PostMapping("/batches/{batchId}/calculate")
    @Operation(summary = "批量计算批次成绩")
    public Result<Void> batchCalculate(@PathVariable Integer batchId) {
        scoreComputationService.batchRecompute(batchId);
        return Result.success();
    }
}
