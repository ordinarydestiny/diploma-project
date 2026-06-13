package com.internship.controller;

import com.internship.common.Result;
import com.internship.annotation.LogOperation;
import com.internship.entity.Defense;
import com.internship.service.DefenseService;
import com.internship.service.ScoreComputationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;
import java.math.BigDecimal;

@RestController
@RequestMapping("/api/defenses")
@RequiredArgsConstructor
@Tag(name = "答辩管理")
public class DefenseController {

    private final DefenseService defenseService;
    private final ScoreComputationService scoreComputationService;

    @PostMapping("/submitByStudent")
    @Operation(summary = "学生提交答辩记录（完整版）")
    @PreAuthorize("hasRole('student')")
    @LogOperation("学生提交答辩信息")
    public Result<Defense> submitByStudent(
            @RequestParam Integer selectionId,
            @RequestParam String defenseScore,
            @RequestParam(required = false) Integer recordFileId,
            @RequestParam(required = false) String selfEvaluation,
            @RequestParam(required = false) String pptFileId,
            @RequestParam(required = false) String defenseDatetime) {
        return Result.success(defenseService.submitByStudentFull(
                selectionId, defenseScore, recordFileId, 
                selfEvaluation, pptFileId, defenseDatetime
        ));
    }

    @PostMapping("/submitByTeacher")
    @Operation(summary = "教师直接录入答辩成绩（完整版）")
    @PreAuthorize("hasRole('teacher')")
    @LogOperation("教师录入答辩成绩")
    public Result<Defense> submitByTeacher(
            @RequestParam Integer selectionId,
            @RequestParam String defenseScore,
            @RequestParam BigDecimal defenseScoreNum,
            @RequestParam(required = false) Integer recordFileId,
            @RequestParam(required = false) String defenseDatetime,
            @RequestParam(required = false) String location,
            @RequestParam(required = false) String committee,
            @RequestParam(required = false) String comment) {
        return Result.success(defenseService.submitByTeacherFull(
                selectionId, defenseScore, defenseScoreNum, recordFileId,
                defenseDatetime, location, committee, comment
        ));
    }

    @GetMapping("/selections/{selectionId}")
    @Operation(summary = "查看答辩记录")
    public Result<Defense> getBySelection(@PathVariable Integer selectionId) {
        // 实现查询逻辑
        return Result.success(null);
    }

    @PutMapping("/{defenseId}/review")
    @Operation(summary = "教师/管理员审核/修改答辩成绩")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin', 'teacher')")
    public Result<Void> review(@PathVariable Integer defenseId,
                                @RequestParam String status,
                                @RequestParam String comment,
                                @RequestParam(required = false) String defenseScore,
                                @RequestParam(required = false) BigDecimal defenseScoreNum) {
        defenseService.reviewDefense(defenseId, status, comment, defenseScore, defenseScoreNum);
        return Result.success();
    }
    
    /**
     * 手动重新计算学生的总成绩
     * 用于修复历史数据或强制刷新成绩
     */
    @PostMapping("/recompute-score/{selectionId}")
    @Operation(summary = "手动重算总成绩")
    public Result<String> recomputeScore(@PathVariable Integer selectionId) {
        try {
            // 根据selectionId查询学生ID和批次ID
            // 这里简化处理，直接调用重算逻辑
            scoreComputationService.recomputeBySelectionId(selectionId);
            return Result.success("✅ 总成绩已成功重新计算！请刷新页面查看最新成绩");
        } catch (Exception e) {
            return Result.fail(500, "❌ 重算总成绩失败: " + e.getMessage());
        }
    }
}
