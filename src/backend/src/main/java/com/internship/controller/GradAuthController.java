package com.internship.controller;

import com.internship.common.Result;
import com.internship.dto.GradLoginRequest;
import com.internship.dto.GradLoginResponse;
import com.internship.entity.User;
import com.internship.service.GradAuthService;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api/auth")
@RequiredArgsConstructor
@Tag(name = "毕设认证管理")
public class GradAuthController {

    private final GradAuthService authService;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    @PostMapping("/login")
    @Operation(summary = "用户登录")
    public Result<GradLoginResponse> login(@RequestBody GradLoginRequest request) {
        return Result.success(authService.login(request));
    }

    @GetMapping("/me")
    @Operation(summary = "获取当前用户信息")
    public Result<User> getCurrentUser() {
        return Result.success(authService.getCurrentUser());
    }

    @GetMapping("/stats")
    @Operation(summary = "获取当前用户统计数据", description = "根据角色返回不同的统计数据：学生-选题/任务书/答辩，教师-指导学生/审核任务等")
    @PreAuthorize("isAuthenticated()")
    public Result<Map<String, Object>> getUserStats() {
        Long userIdLong = jwtUtil.getCurrentUserId();
        if (userIdLong == null) {
            return Result.fail(401, "未登录或登录已过期");
        }
        
        Integer userId = userIdLong.intValue();
        
        // 获取用户角色
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String userRole = jdbcTemplate.queryForObject(roleSql, String.class, userId);
        
        Map<String, Object> stats = new HashMap<>();
        
        if ("student".equals(userRole)) {
            // 学生统计：选题数、任务书数、答辩数、中期检查数、签到数
            stats.putAll(getStudentStats(userId));
        } else if ("teacher".equals(userRole)) {
            // 教师统计：指导学生数、待审核选题数、待审核中期检查数、待审核最终检查数
            stats.putAll(getTeacherStats(userId));
        } else if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
            // 管理员统计：总学生数、总教师数、进行中批次数、待审核题目数
            stats.putAll(getAdminStats(userRole));
        }
        
        stats.put("role", userRole);
        return Result.success(stats);
    }
    
    private Map<String, Object> getStudentStats(Integer studentId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 选题数（已通过的）
        String topicSql = "SELECT COUNT(*) FROM student_selections WHERE student_id = ? AND status = 'approved'";
        Integer topicCount = jdbcTemplate.queryForObject(topicSql, Integer.class, studentId);
        stats.put("topicCount", topicCount != null ? topicCount : 0);
        
        // 任务书数（已下达的）
        String taskbookSql = """
            SELECT COUNT(*) FROM task_books tb 
            INNER JOIN student_selections ss ON tb.selection_id = ss.selection_id 
            WHERE ss.student_id = ? AND tb.status IN ('issued', 'confirmed')
        """;
        Integer taskbookCount = jdbcTemplate.queryForObject(taskbookSql, Integer.class, studentId);
        stats.put("taskbookCount", taskbookCount != null ? taskbookCount : 0);
        
        // 答辩数（已完成的）
        String defenseSql = """
            SELECT COUNT(*) FROM defenses d 
            INNER JOIN student_selections ss ON d.selection_id = ss.selection_id 
            WHERE ss.student_id = ? AND d.status IN ('approved', 'submitted')
        """;
        Integer defenseCount = jdbcTemplate.queryForObject(defenseSql, Integer.class, studentId);
        stats.put("defenseCount", defenseCount != null ? defenseCount : 0);
        
        // 中期检查数（已提交的）
        String midtermSql = """
            SELECT COUNT(*) FROM midterm_checks mc 
            INNER JOIN student_selections ss ON mc.selection_id = ss.selection_id 
            WHERE ss.student_id = ? AND mc.status != 'draft'
        """;
        Integer midtermCount = jdbcTemplate.queryForObject(midtermSql, Integer.class, studentId);
        stats.put("midtermCount", midtermCount != null ? midtermCount : 0);
        
        // 签到数（正常签到的）
        String signinSql = "SELECT COUNT(*) FROM sign_ins WHERE student_id = ? AND sign_status = 'normal'";
        Integer signinCount = jdbcTemplate.queryForObject(signinSql, Integer.class, studentId);
        stats.put("signinCount", signinCount != null ? signinCount : 0);
        
        return stats;
    }
    
    private Map<String, Object> getTeacherStats(Integer teacherId) {
        Map<String, Object> stats = new HashMap<>();
        
        // 指导学生数
        String studentSql = "SELECT COUNT(DISTINCT student_id) FROM teacher_student_relations WHERE teacher_id = ?";
        Integer studentCount = jdbcTemplate.queryForObject(studentSql, Integer.class, teacherId);
        stats.put("studentCount", studentCount != null ? studentCount : 0);
        
        // 待审核选题数
        String pendingTopicSql = """
            SELECT COUNT(*) FROM student_selections ss 
            INNER JOIN teacher_student_relations tsr ON ss.student_id = tsr.student_id AND ss.batch_id = tsr.batch_id 
            WHERE tsr.teacher_id = ? AND ss.status = 'pending'
        """;
        Integer pendingTopicCount = jdbcTemplate.queryForObject(pendingTopicSql, Integer.class, teacherId);
        stats.put("pendingTopicCount", pendingTopicCount != null ? pendingTopicCount : 0);
        
        // 待审核中期检查数
        String pendingMidtermSql = """
            SELECT COUNT(*) FROM midterm_checks mc 
            INNER JOIN student_selections ss ON mc.selection_id = ss.selection_id 
            INNER JOIN teacher_student_relations tsr ON ss.student_id = tsr.student_id AND ss.batch_id = tsr.batch_id 
            WHERE tsr.teacher_id = ? AND mc.status = 'pending'
        """;
        Integer pendingMidtermCount = jdbcTemplate.queryForObject(pendingMidtermSql, Integer.class, teacherId);
        stats.put("pendingMidtermCount", pendingMidtermCount != null ? pendingMidtermCount : 0);
        
        // 待审核最终检查数
        String pendingFinalSql = """
            SELECT COUNT(*) FROM final_checks fc 
            INNER JOIN student_selections ss ON fc.selection_id = ss.selection_id 
            INNER JOIN teacher_student_relations tsr ON ss.student_id = tsr.student_id AND ss.batch_id = tsr.batch_id 
            WHERE tsr.teacher_id = ? AND fc.status = 'pending'
        """;
        Integer pendingFinalCount = jdbcTemplate.queryForObject(pendingFinalSql, Integer.class, teacherId);
        stats.put("pendingFinalCount", pendingFinalCount != null ? pendingFinalCount : 0);
        
        // 已完成任务书数
        String completedTaskbookSql = """
            SELECT COUNT(*) FROM task_books tb 
            INNER JOIN student_selections ss ON tb.selection_id = ss.selection_id 
            INNER JOIN teacher_student_relations tsr ON ss.student_id = tsr.student_id AND ss.batch_id = tsr.batch_id 
            WHERE tsr.teacher_id = ? AND tb.status = 'confirmed'
        """;
        Integer completedTaskbookCount = jdbcTemplate.queryForObject(completedTaskbookSql, Integer.class, teacherId);
        stats.put("completedTaskbookCount", completedTaskbookCount != null ? completedTaskbookCount : 0);
        
        return stats;
    }
    
    private Map<String, Object> getAdminStats(String role) {
        Map<String, Object> stats = new HashMap<>();
        
        // 总学生数
        String studentSql = "SELECT COUNT(*) FROM users WHERE role = 'student'";
        Integer totalStudents = jdbcTemplate.queryForObject(studentSql, Integer.class);
        stats.put("totalStudents", totalStudents != null ? totalStudents : 0);
        
        // 总教师数
        String teacherSql = "SELECT COUNT(*) FROM users WHERE role = 'teacher'";
        Integer totalTeachers = jdbcTemplate.queryForObject(teacherSql, Integer.class);
        stats.put("totalTeachers", totalTeachers != null ? totalTeachers : 0);
        
        // 进行中批次数
        String activeBatchSql = "SELECT COUNT(*) FROM project_batches WHERE status = 'active'";
        Integer activeBatches = jdbcTemplate.queryForObject(activeBatchSql, Integer.class);
        stats.put("activeBatches", activeBatches != null ? activeBatches : 0);
        
        // 待审核题目数（如果是专业负责人或院管）
        String pendingTopicSql = "SELECT COUNT(*) FROM topics WHERE status = 'available' OR (source = 'student' AND status IS NULL)";
        Integer pendingTopics = jdbcTemplate.queryForObject(pendingTopicSql, Integer.class);
        stats.put("pendingTopics", pendingTopics != null ? pendingTopics : 0);
        
        // 今日新增选题数
        String todayTopicSql = "SELECT COUNT(*) FROM student_selections WHERE DATE(created_at) = CURDATE()";
        Integer todayTopics = jdbcTemplate.queryForObject(todayTopicSql, Integer.class);
        stats.put("todayTopics", todayTopics != null ? todayTopics : 0);
        
        return stats;
    }

    @PostMapping("/activate")
    @Operation(summary = "账号激活")
    public Result<Void> activate(@RequestParam Integer userId, 
                                  @RequestParam String phone,
                                  @RequestParam String password) {
        authService.activate(userId, phone, password);
        return Result.success();
    }

    @PostMapping("/switch-role")
    @Operation(summary = "角色切换")
    public Result<GradLoginResponse> switchRole(@RequestParam Integer roleId) {
        return Result.success(authService.switchRole(roleId));
    }

    @PostMapping("/logout")
    @Operation(summary = "用户登出")
    public Result<Void> logout() {
        // 登出逻辑：前端会清除token，后端只需返回成功
        return Result.success();
    }
}
