package com.internship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.common.Result;
import com.internship.entity.SignIn;
import com.internship.mapper.SignInMapper;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.List;

@RestController
@RequestMapping("/api/sign-ins")
@RequiredArgsConstructor
@Tag(name = "每日签到管理")
public class SignInController {

    private final SignInMapper signInMapper;
    private final JwtUtil jwtUtil;

    @PostMapping
    @Operation(summary = "学生每日签到")
    @PreAuthorize("hasRole('student')")
    public Result<SignIn> signIn(@RequestBody SignIn signIn) {
        Integer studentId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;
        
        signIn.setStudentId(studentId);
        signIn.setSignDate(LocalDate.now());
        signIn.setSignTime(LocalTime.now().toString());
        signIn.setSignStatus("normal");
        signIn.setIsMakeup(false);
        
        // 检查是否已签到
        var checkWrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SignIn>()
            .eq(SignIn::getStudentId, studentId)
            .eq(SignIn::getBatchId, signIn.getBatchId())
            .eq(SignIn::getSignDate, LocalDate.now());
        
        if (signInMapper.selectCount(checkWrapper) > 0) {
            throw new RuntimeException("今日已签到，请勿重复提交");
        }
        
        signInMapper.insert(signIn);
        return Result.success(signIn);
    }

    @PostMapping("/makeup")
    @Operation(summary = "补签")
    @PreAuthorize("hasRole('student')")
    public Result<SignIn> makeup(
            @RequestParam Integer batchId,
            @RequestParam LocalDate signDate,
            @RequestParam String dailyReport,
            @RequestParam String makeupReason) {
        
        Integer studentId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;
        
        // TODO: 检查补签天数限制（从system_configs读取max_makeup_days）
        
        SignIn signIn = new SignIn();
        signIn.setStudentId(studentId);
        signIn.setBatchId(batchId);
        signIn.setSignDate(signDate);
        signIn.setSignTime(LocalTime.now().toString());
        signIn.setDailyReport(dailyReport);
        signIn.setIsMakeup(true);
        signIn.setMakeupReason(makeupReason);
        signIn.setSignStatus("normal");
        
        signInMapper.insert(signIn);
        return Result.success(signIn);
    }

    @GetMapping("/mine")
    @Operation(summary = "查看自己签到记录")
    @PreAuthorize("hasRole('student')")
    public Result<IPage<SignIn>> myRecords(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Integer studentId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;
        Page<SignIn> pageParam = new Page<>(page, size);
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SignIn>()
            .eq(SignIn::getStudentId, studentId)
            .orderByDesc(SignIn::getSignDate);
        
        return Result.success(signInMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/batches/{batchId}")
    @Operation(summary = "按批次查看学生签到（教师/管理员）")
    @PreAuthorize("hasAnyRole('teacher', 'college_admin', 'major_admin')")
    public Result<IPage<SignIn>> studentRecords(
            @PathVariable Integer batchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer studentId) {
        
        Page<SignIn> pageParam = new Page<>(page, size);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<SignIn>()
            .eq(SignIn::getBatchId, batchId)
            .eq(studentId != null, SignIn::getStudentId, studentId)
            .orderByDesc(SignIn::getSignDate);
        
        return Result.success(signInMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/batches/{batchId}/export")
    @Operation(summary = "导出签到数据Excel")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    public Result<String> export(@PathVariable Integer batchId) {
        // TODO: 使用Apache POI生成Excel文件并返回下载链接
        return Result.success("/downloads/signins_batch_" + batchId + "_" + System.currentTimeMillis() + ".xlsx");
    }
}
