package com.internship.controller;

import com.internship.common.Result;
import com.internship.service.TeacherDataService;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

/**
 * 指导教师数据查询Controller
 * 提供教师视角的各种数据聚合查询接口
 */
@RestController
@RequestMapping("/api/v1/teacher")
@RequiredArgsConstructor
@Tag(name = "指导教师数据")
public class TeacherDataController {

    private final TeacherDataService teacherDataService;
    private final JwtUtil jwtUtil;

    /**
     * 获取当前登录的教师ID
     */
    private Integer getCurrentTeacherId() {
        Long userId = jwtUtil.getCurrentUserId();
        if (userId == null) {
            throw new RuntimeException("未登录或登录已过期");
        }
        return userId.intValue();
    }

    @GetMapping("/students")
    @Operation(summary = "获取我的学生列表", description = "获取当前教师负责的所有学生信息")
    public Result<List<Map<String, Object>>> getMyStudents() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> students = teacherDataService.getMyStudents(teacherId);
        return Result.success(students);
    }

    @GetMapping("/topics")
    @Operation(summary = "获取我负责的题目列表", description = "获取当前教师创建或管理的题目")
    public Result<List<Map<String, Object>>> getMyTopics() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> topics = teacherDataService.getMyTopics(teacherId);
        return Result.success(topics);
    }

    @GetMapping("/selections/pending")
    @Operation(summary = "获取待审核的选题申请", description = "获取需要审核的学生选题记录")
    public Result<List<Map<String, Object>>> getPendingSelections() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> selections = teacherDataService.getPendingSelections(teacherId);
        return Result.success(selections);
    }

    @GetMapping("/selections/all")
    @Operation(summary = "获取所有学生选题状态", description = "获取所有学生的选题记录，支持按状态筛选")
    public Result<List<Map<String, Object>>> getAllStudentSelections(
            @RequestParam(required = false) String status) {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> selections = teacherDataService.getAllStudentSelections(teacherId, status);
        return Result.success(selections);
    }

    @GetMapping("/taskbooks/needed")
    @Operation(summary = "获取需要下达任务书的学生", description = "获取已通过选题但尚未下达任务书的学生")
    public Result<List<Map<String, Object>>> getStudentsNeedTaskBook() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> students = teacherDataService.getStudentsNeedTaskBook(teacherId);
        return Result.success(students);
    }

    @GetMapping("/taskbooks/mine")
    @Operation(summary = "获取我下达的任务书列表", description="获取当前教师所有已下达的任务书")
    public Result<List<Map<String, Object>>> getMyTaskBooks() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> taskBooks = teacherDataService.getMyTaskBooks(teacherId);
        return Result.success(taskBooks);
    }

    @GetMapping("/midterm/pending")
    @Operation(summary = "获取待审核的中期报告", description = "获取需要审核的中期检查报告")
    public Result<List<Map<String, Object>>> getPendingMidtermChecks() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> checks = teacherDataService.getPendingMidtermChecks(teacherId);
        return Result.success(checks);
    }

    @GetMapping("/midterm/all")
    @Operation(summary = "获取所有中期检查记录", description = "获取所有学生的中期检查记录")
    public Result<List<Map<String, Object>>> getAllMidtermChecks() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> checks = teacherDataService.getAllMidtermChecks(teacherId);
        return Result.success(checks);
    }

    @GetMapping("/final/pending")
    @Operation(summary = "获取待审核的最终报告", description = "获取需要审核的最终检查报告")
    public Result<List<Map<String, Object>>> getPendingFinalChecks() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> checks = teacherDataService.getPendingFinalChecks(teacherId);
        return Result.success(checks);
    }

    @GetMapping("/final/all")
    @Operation(summary = "获取所有最终检查记录", description = "获取所有学生的最终检查记录")
    public Result<List<Map<String, Object>>> getAllFinalChecks() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> checks = teacherDataService.getAllFinalChecks(teacherId);
        return Result.success(checks);
    }

    @GetMapping("/defenses")
    @Operation(summary = "获取答辩管理数据", description = "获取学生的答辩记录，支持按批次筛选")
    public Result<List<Map<String, Object>>> getDefenseRecords(
            @RequestParam(required = false) String batchId) {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> defenses = teacherDataService.getDefenseRecords(teacherId, batchId);
        return Result.success(defenses);
    }

    @GetMapping("/signins")
    @Operation(summary = "获取学生签到记录", description = "获取学生的出勤情况，支持按学生和日期范围筛选")
    public Result<List<Map<String, Object>>> getStudentSignIns(
            @RequestParam(required = false) Integer studentId,
            @RequestParam(required = false) String dateRange) {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> signIns = teacherDataService.getStudentSignIns(teacherId, studentId, dateRange);
        return Result.success(signIns);
    }

    @GetMapping("/semesters")
    @Operation(summary = "获取可用学期列表", description = "获取所有可用的毕业设计学期（从批次表去重）")
    public Result<List<Map<String, Object>>> getAvailableSemesters() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> semesters = teacherDataService.getAvailableSemesters(teacherId);
        return Result.success(semesters);
    }

    @GetMapping("/topic-categories")
    @Operation(summary = "获取题目分类列表", description = "获取所有题目分类（从topics表的category字段去重）")
    public Result<List<String>> getTopicCategories() {
        Integer teacherId = getCurrentTeacherId();
        List<String> categories = teacherDataService.getTopicCategories(teacherId);
        return Result.success(categories);
    }

    @GetMapping("/available-students")
    @Operation(summary = "获取可纳入的学生列表", description = "获取所有还没有师生关系的学生，用于纳入学生功能")
    public Result<List<Map<String, Object>>> getAvailableStudentsForRelation() {
        Integer teacherId = getCurrentTeacherId();
        List<Map<String, Object>> students = teacherDataService.getAvailableStudentsForRelation(teacherId);
        return Result.success(students);
    }
}
