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

        // 先查询是否已存在未下达或草稿状态的任务书（避免重复创建）
        LambdaQueryWrapper<TaskBook> existingWrapper = new LambdaQueryWrapper<TaskBook>()
            .eq(TaskBook::getSelectionId, taskBook.getSelectionId())
            .in(TaskBook::getStatus, "unissued", "draft")
            .orderByDesc(TaskBook::getVersion)
            .last("LIMIT 1");
        TaskBook existingTask = taskBookMapper.selectOne(existingWrapper);

        if (existingTask != null) {
            // 更新现有记录
            existingTask.setContent(taskBook.getContent());
            existingTask.setDeadline(taskBook.getDeadline());
            existingTask.setRequirements(taskBook.getRequirements());
            existingTask.setTechParams(taskBook.getTechParams());
            existingTask.setReferences(taskBook.getReferences());
            existingTask.setStatus("issued");
            existingTask.setIssuerId(issuerId);
            existingTask.setIssuedAt(java.time.LocalDateTime.now());

            taskBookMapper.updateById(existingTask);
            return Result.success(existingTask);
        }

        // 查询当前最大版本号（创建新记录）
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

    @DeleteMapping("/{taskId}")
    @Operation(summary = "删除任务书（教师/管理员重置）")
    @PreAuthorize("hasAnyRole('teacher', 'college_admin')")
    @LogOperation("删除任务书")
    public Result<Void> delete(@PathVariable Integer taskId) {
        taskBookMapper.deleteById(taskId);
        return Result.success();
    }

    @PutMapping("/{taskId}/reset")
    @Operation(summary = "重置任务书状态为未下达")
    @PreAuthorize("hasAnyRole('teacher', 'college_admin')")
    @LogOperation("重置任务书状态")
    public Result<Void> resetStatus(@PathVariable Integer taskId) {
        try {
            TaskBook taskBook = taskBookMapper.selectById(taskId);

            if (taskBook == null) {
                return Result.fail(404, "任务书不存在，ID: " + taskId);
            }

            System.out.println("=== 开始重置任务书 ===");
            System.out.println("任务书ID: " + taskId);
            System.out.println("当前状态: " + taskBook.getStatus());
            System.out.println("当前确认人: " + taskBook.getConfirmBy());

            // 使用UpdateWrapper强制更新字段
            com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<TaskBook> updateWrapper =
                new com.baomidou.mybatisplus.core.conditions.update.UpdateWrapper<>();

            updateWrapper.eq("task_id", taskId);

            // 重置任务书状态和确认信息（⭐ 不清空issuer_id等有NOT NULL约束的字段）
            updateWrapper.set("status", "unissued");           // 任务书状态 → 未下达
            updateWrapper.set("confirm_by", null);             // ⭐ 清空确认人
            updateWrapper.set("confirm_at", null);             // ⭐ 清空确认时间
            updateWrapper.set("rejector_id", null);            // 清空驳回人
            updateWrapper.set("reject_time", null);            // 清空驳回时间
            updateWrapper.set("reject_comment", null);         // 清空驳回原因
            updateWrapper.set("updated_at", java.time.LocalDateTime.now()); // 更新时间

            int rows = taskBookMapper.update(null, updateWrapper);

            if (rows > 0) {
                System.out.println("✅ 重置成功! 影响行数: " + rows);
                return Result.success();
            } else {
                return Result.fail(500, "更新失败，数据可能已被删除");
            }
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "重置失败: " + e.getMessage());
        }
    }

    @PutMapping("/{taskId}/confirm")
    @Operation(summary = "学生确认接收任务书")
    @PreAuthorize("hasRole('student')")
    @LogOperation("确认接收任务书")
    public Result<Void> confirmReceive(@PathVariable Integer taskId) {
        try {
            TaskBook taskBook = taskBookMapper.selectById(taskId);

            if (taskBook == null) {
                return Result.fail(404, "任务书不存在，ID: " + taskId);
            }

            if (!"issued".equals(taskBook.getStatus())) {
                return Result.fail(400, "只能确认接收已下达的任务书，当前状态: " + taskBook.getStatus());
            }

            Integer studentId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;

            if (studentId == null) {
                return Result.fail(401, "无法获取当前用户信息，请重新登录");
            }

            taskBook.setStatus("confirmed");
            taskBook.setConfirmBy(studentId);
            taskBook.setConfirmAt(java.time.LocalDateTime.now());

            int rows = taskBookMapper.updateById(taskBook);

            if (rows == 0) {
                return Result.fail(500, "更新任务书失败，可能数据已被修改");
            }

            return Result.success();
        } catch (Exception e) {
            e.printStackTrace();
            return Result.fail(500, "确认接收任务书失败: " + e.getMessage());
        }
    }
}
