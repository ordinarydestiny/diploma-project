package com.internship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.annotation.LogOperation;
import com.internship.common.Result;
import com.internship.dto.SelectionReviewDTO;
import com.internship.dto.SelectionSubmitDTO;
import com.internship.entity.StudentSelection;
import com.internship.service.SelectionService;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/selections")
@RequiredArgsConstructor
@Tag(name = "选题管理")
public class SelectionController {

    private final SelectionService selectionService;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "学生提交选题")
    @PreAuthorize("hasRole('student')")
    public Result<StudentSelection> submit(@RequestBody SelectionSubmitDTO dto) {
        return Result.success(selectionService.submitSelection(dto));
    }

    @GetMapping
    @Operation(summary = "查看选题列表")
    public Result<IPage<StudentSelection>> list(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer batchId,
            @RequestParam(required = false) String status) {
        return Result.success(selectionService.listSelections(page, size, batchId, status));
    }

    @GetMapping("/current")
    @Operation(summary = "查询当前有效选题")
    public Result<StudentSelection> getCurrent() {
        Long studentId = jwtUtil.getCurrentUserId();  // 获取当前学生ID
        Integer batchId = null;  // 从请求参数或上下文获取当前批次ID
        return Result.success(selectionService.getCurrentSelection(studentId != null ? studentId.intValue() : null, batchId));
    }

    @PutMapping("/{selectionId}/review")
    @Operation(summary = "教师/管理员审核选题")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin', 'teacher')")
    @LogOperation("审核选题")
    public Result<Void> review(@PathVariable Integer selectionId, 
                               @RequestBody SelectionReviewDTO dto) {
        selectionService.reviewSelection(selectionId, dto);
        return Result.success();
    }

    @PutMapping("/{selectionId}/reset")
    @Operation(summary = "重置选题状态（管理员或教师）")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin', 'teacher')")
    @LogOperation("重置选题")
    public Result<Void> reset(@PathVariable Integer selectionId,
                              @RequestParam String reason) {
        selectionService.resetSelection(selectionId, reason);
        return Result.success();
    }
}
