package com.internship.controller;

import com.internship.annotation.LogOperation;
import com.internship.common.Result;
import com.internship.entity.FinalCheck;
import com.internship.service.FinalCheckService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/final-checks")
@RequiredArgsConstructor
@Tag(name = "最终检查管理")
public class FinalCheckController {

    private final FinalCheckService finalCheckService;

    @PostMapping("/submit")
    @Operation(summary = "学生提交最终报告")
    @PreAuthorize("hasRole('student')")
    public Result<Void> submit(@RequestParam Integer selectionId,
                                @RequestParam Integer fileId) {
        finalCheckService.submitFinalCheck(selectionId, fileId);
        return Result.success();
    }

    @GetMapping("/selections/{selectionId}")
    @Operation(summary = "查看提交历史")
    public Result<FinalCheck> getBySelection(@PathVariable Integer selectionId) {
        return Result.success(finalCheckService.getFinalVersion(selectionId));
    }

    @PutMapping("/{checkId}/review")
    @Operation(summary = "教师审核最终报告（定稿逻辑）")
    @PreAuthorize("hasRole('teacher')")
    @LogOperation("审核最终报告")
    public Result<Void> review(@PathVariable Integer checkId,
                                @RequestParam String status,
                                @RequestParam String comment,
                                @RequestParam Integer score) {
        finalCheckService.reviewFinalCheck(checkId, status, comment, score);
        return Result.success();
    }
}
