package com.internship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.internship.annotation.LogOperation;
import com.internship.common.Result;
import com.internship.entity.MidtermCheck;
import com.internship.mapper.MidtermCheckMapper;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/midterm-checks")
@RequiredArgsConstructor
@Tag(name = "中期检查管理")
public class MidtermCheckController {

    private final MidtermCheckMapper midtermCheckMapper;
    private final JwtUtil jwtUtil;

    @PostMapping("/submit")
    @Operation(summary = "学生提交中期报告")
    @PreAuthorize("hasRole('student')")
    @LogOperation("提交中期检查报告")
    public Result<MidtermCheck> submit(
            @RequestParam Integer selectionId,
            @RequestParam(required = false) Integer fileId,
            @RequestParam(defaultValue = "0") Integer progress) {

        Integer studentId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;

        // 查找当前最新的记录
        LambdaQueryWrapper<MidtermCheck> wrapper = new LambdaQueryWrapper<MidtermCheck>()
            .eq(MidtermCheck::getSelectionId, selectionId)
            .eq(MidtermCheck::getIsCurrent, true);
        MidtermCheck currentCheck = midtermCheckMapper.selectOne(wrapper);

        // 只有已通过的报告才不允许重新提交
        if (currentCheck != null && "approved".equals(currentCheck.getStatus())) {
            return Result.fail(400, "已有通过的中期报告，无法重新提交。如需修改，请联系指导教师。");
        }

        // 如果存在当前记录（待审核/已驳回），将其标记为非最新
        if (currentCheck != null) {
            currentCheck.setIsCurrent(false);
            midtermCheckMapper.updateById(currentCheck);
        }

        // 查询最大版本号
        LambdaQueryWrapper<MidtermCheck> versionWrapper = new LambdaQueryWrapper<MidtermCheck>()
            .eq(MidtermCheck::getSelectionId, selectionId)
            .orderByDesc(MidtermCheck::getVersion)
            .last("LIMIT 1");
        MidtermCheck lastVersion = midtermCheckMapper.selectOne(versionWrapper);

        int newVersion = (lastVersion != null) ? lastVersion.getVersion() + 1 : 1;

        MidtermCheck check = new MidtermCheck();
        check.setSelectionId(selectionId);
        check.setFileId(fileId);
        check.setProgress(progress);
        check.setSubmitTime(java.time.LocalDateTime.now());
        check.setVersion(newVersion);
        check.setIsCurrent(true);
        check.setStatus("pending");

        midtermCheckMapper.insert(check);
        return Result.success(check);
    }

    @GetMapping("/selections/{selectionId}")
    @Operation(summary = "查看提交历史")
    public Result<List<MidtermCheck>> listBySelection(@PathVariable Integer selectionId) {
        var wrapper = new LambdaQueryWrapper<MidtermCheck>()
            .eq(MidtermCheck::getSelectionId, selectionId)
            .orderByDesc(MidtermCheck::getVersion);
        return Result.success(midtermCheckMapper.selectList(wrapper));
    }

    @PutMapping("/{checkId}/review")
    @Operation(summary = "教师审核中期报告")
    @PreAuthorize("hasRole('teacher')")
    @LogOperation("审核中期报告")
    public Result<Void> review(
            @PathVariable Integer checkId,
            @RequestParam String status,
            @RequestParam(required = false) String comment,
            @RequestParam(required = false) Integer guideFileId) {
        
        MidtermCheck check = midtermCheckMapper.selectById(checkId);
        if (check != null) {
            check.setStatus(status);
            check.setReviewerId(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
            check.setReviewTime(java.time.LocalDateTime.now());
            check.setReviewComment(comment);
            check.setGuideFileId(guideFileId);
            midtermCheckMapper.updateById(check);
        }
        
        return Result.success();
    }
}
