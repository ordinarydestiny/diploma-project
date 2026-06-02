package com.internship.controller;

import com.internship.common.Result;
import com.internship.entity.Defense;
import com.internship.service.DefenseService;
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

    @PostMapping("/submitByStudent")
    @Operation(summary = "学生提交答辩记录")
    @PreAuthorize("hasRole('student')")
    public Result<Defense> submitByStudent(@RequestParam Integer selectionId,
                                           @RequestParam String defenseScore,
                                           @RequestParam Integer recordFileId) {
        return Result.success(defenseService.submitByStudent(selectionId, defenseScore, recordFileId));
    }

    @PostMapping("/submitByTeacher")
    @Operation(summary = "教师直接录入答辩成绩")
    @PreAuthorize("hasRole('teacher')")
    public Result<Defense> submitByTeacher(@RequestParam Integer selectionId,
                                          @RequestParam String defenseScore,
                                          @RequestParam BigDecimal defenseScoreNum,
                                          @RequestParam Integer recordFileId) {
        return Result.success(defenseService.submitByTeacher(selectionId, defenseScore, defenseScoreNum, recordFileId));
    }

    @GetMapping("/selections/{selectionId}")
    @Operation(summary = "查看答辩记录")
    public Result<Defense> getBySelection(@PathVariable Integer selectionId) {
        // 实现查询逻辑
        return Result.success(null);
    }

    @PutMapping("/{defenseId}/review")
    @Operation(summary = "教师审核/修改答辩成绩")
    @PreAuthorize("hasRole('teacher')")
    public Result<Void> review(@PathVariable Integer defenseId,
                                @RequestParam String status,
                                @RequestParam String comment) {
        defenseService.reviewDefense(defenseId, status, comment);
        return Result.success();
    }
}
