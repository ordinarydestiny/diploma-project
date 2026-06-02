package com.internship.controller;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.internship.annotation.LogOperation;
import com.internship.common.Result;
import com.internship.entity.TaskBook;
import com.internship.mapper.TaskBookMapper;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/taskbooks")
@RequiredArgsConstructor
@Tag(name = "任务书管理")
public class TaskBookController {

    private final TaskBookMapper taskBookMapper;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "指导老师下达任务书")
    @PreAuthorize("hasRole('teacher')")
    @LogOperation("下达任务书")
    public Result<TaskBook> issue(@RequestBody TaskBook taskBook) {
        Integer issuerId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;
        
        // 查询当前最大版本号
        LambdaQueryWrapper<TaskBook> wrapper = new LambdaQueryWrapper<TaskBook>()
            .eq(TaskBook::getSelectionId, taskBook.getSelectionId())
            .orderByDesc(TaskBook::getVersion)
            .last("LIMIT 1");
        TaskBook lastVersion = taskBookMapper.selectOne(wrapper);
        
        int newVersion = (lastVersion != null) ? lastVersion.getVersion() + 1 : 1;
        
        taskBook.setVersion(newVersion);
        taskBook.setStatus("issued");
        taskBook.setIssuerId(issuerId);
        taskBook.setIssuedAt(java.time.LocalDateTime.now());
        
        taskBookMapper.insert(taskBook);
        return Result.success(taskBook);
    }

    @GetMapping("/selections/{selectionId}")
    @Operation(summary = "查看任务书（学生/关联用户）")
    public Result<List<TaskBook>> getBySelection(@PathVariable Integer selectionId) {
        var wrapper = new LambdaQueryWrapper<TaskBook>()
            .eq(TaskBook::getSelectionId, selectionId)
            .orderByDesc(TaskBook::getVersion);
        return Result.success(taskBookMapper.selectList(wrapper));
    }

    @PostMapping("/{taskId}/reissue")
    @Operation(summary = "修改后重新下达（被驳回后）")
    @PreAuthorize("hasRole('teacher')")
    @LogOperation("重新下达任务书")
    public Result<TaskBook> reissue(
            @PathVariable Integer taskId,
            @RequestBody TaskBook taskBook) {
        
        // 获取旧版本信息
        TaskBook oldTask = taskBookMapper.selectById(taskId);
        int newVersion = oldTask.getVersion() + 1;
        
        taskBook.setSelectionId(oldTask.getSelectionId());
        taskBook.setVersion(newVersion);
        taskBook.setStatus("issued");
        taskBook.setIssuerId(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
        taskBook.setIssuedAt(java.time.LocalDateTime.now());
        
        taskBookMapper.insert(taskBook);
        return Result.success(taskBook);
    }

    @PutMapping("/{taskId}/reject")
    @Operation(summary = "院级管理员驳回任务书")
    @PreAuthorize("hasRole('college_admin')")
    @LogOperation("驳回任务书")
    public Result<Void> reject(
            @PathVariable Integer taskId,
            @RequestParam String rejectComment) {
        
        TaskBook taskBook = taskBookMapper.selectById(taskId);
        if (taskBook != null) {
            taskBook.setStatus("rejected");
            taskBook.setRejectorId(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
            taskBook.setRejectTime(java.time.LocalDateTime.now());
            taskBook.setRejectComment(rejectComment);
            taskBookMapper.updateById(taskBook);
        }
        
        return Result.success();
    }
}
