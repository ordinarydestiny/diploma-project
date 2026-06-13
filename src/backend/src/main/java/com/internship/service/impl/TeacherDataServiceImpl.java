package com.internship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.internship.service.TeacherDataService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.stereotype.Service;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

/**
 * 指导教师数据查询服务实现
 * 使用JdbcTemplate进行复杂的多表关联查询
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class TeacherDataServiceImpl implements TeacherDataService {

    private final JdbcTemplate jdbcTemplate;

    @Override
    public List<Map<String, Object>> getMyStudents(Integer teacherId) {
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String userRole = jdbcTemplate.queryForObject(roleSql, String.class, teacherId);

        String sql;

        if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
            sql = """
                WITH ranked_relations AS (
                    SELECT
                        tsr.id as relation_id,
                        tsr.student_id,
                        tsr.teacher_id,
                        u.username as student_no,
                        u.real_name as student_name,
                        u.class_name,
                        m.major_name,
                        c.college_name,
                        tsr.batch_id,
                        pb.batch_name,
                        tchr.real_name as teacher_name,
                        tsr.created_at as assigned_time,
                        ROW_NUMBER() OVER (PARTITION BY tsr.student_id ORDER BY tsr.created_at DESC) as rn
                    FROM teacher_student_relations tsr
                    LEFT JOIN users u ON tsr.student_id = u.user_id
                    LEFT JOIN majors m ON u.major_id = m.major_id
                    LEFT JOIN colleges c ON m.college_id = c.college_id
                    LEFT JOIN project_batches pb ON tsr.batch_id = pb.batch_id
                    LEFT JOIN users tchr ON tsr.teacher_id = tchr.user_id
                )
                SELECT 
                    relation_id, student_id, teacher_id, student_no, student_name,
                    class_name, major_name, college_name, batch_id, batch_name,
                    teacher_name, assigned_time
                FROM ranked_relations
                WHERE rn = 1
                ORDER BY student_no ASC
            """;

            return jdbcTemplate.queryForList(sql);

        } else {
            sql = """
                WITH ranked_relations AS (
                    SELECT
                        tsr.id as relation_id,
                        tsr.student_id,
                        tsr.teacher_id,
                        u.username as student_no,
                        u.real_name as student_name,
                        u.class_name,
                        m.major_name,
                        c.college_name,
                        tsr.batch_id,
                        pb.batch_name,
                        tchr.real_name as teacher_name,
                        tsr.created_at as assigned_time,
                        ROW_NUMBER() OVER (PARTITION BY tsr.student_id ORDER BY tsr.created_at DESC) as rn
                    FROM teacher_student_relations tsr
                    LEFT JOIN users u ON tsr.student_id = u.user_id
                    LEFT JOIN majors m ON u.major_id = m.major_id
                    LEFT JOIN colleges c ON m.college_id = c.college_id
                    LEFT JOIN project_batches pb ON tsr.batch_id = pb.batch_id
                    LEFT JOIN users tchr ON tsr.teacher_id = tchr.user_id
                    WHERE tsr.teacher_id = ?
                )
                SELECT
                    relation_id, student_id, teacher_id, student_no, student_name,
                    class_name, major_name, college_name, batch_id, batch_name,
                    teacher_name, assigned_time
                FROM ranked_relations
                WHERE rn = 1
                ORDER BY student_no ASC
            """;

            return jdbcTemplate.queryForList(sql, teacherId);
        }
    }

    @Override
    public List<Map<String, Object>> getMyTopics(Integer teacherId) {
        // 先获取当前用户的角色
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String userRole = jdbcTemplate.queryForObject(roleSql, String.class, teacherId);
        
        String sql;
        
        if ("college_admin".equals(userRole)) {
            // 院级管理员：可以看到所有题目
            sql = """
                SELECT 
                    t.topic_id,
                    t.topic_name,
                    t.description,
                    t.difficulty,
                    t.category,
                    t.topic_type,
                    t.source,
                    t.requirements,
                    t.references as ref_content,
                    t.selection_count,
                    t.max_students,
                    t.status,
                    t.creator_id,
                    u.real_name as creator_name,
                    t.created_at,
                    t.updated_at
                FROM topics t
                LEFT JOIN users u ON t.creator_id = u.user_id
                ORDER BY t.created_at DESC
            """;
            
            return jdbcTemplate.queryForList(sql);
            
        } else if ("major_admin".equals(userRole)) {
            // 专业负责人：可以看到本专业相关的所有题目 + 自己创建的 + 管理员创建的
            sql = """
                SELECT 
                    t.topic_id,
                    t.topic_name,
                    t.description,
                    t.difficulty,
                    t.category,
                    t.topic_type,
                    t.source,
                    t.requirements,
                    t.references as ref_content,
                    t.selection_count,
                    t.max_students,
                    t.status,
                    t.creator_id,
                    u.real_name as creator_name,
                    t.created_at,
                    t.updated_at
                FROM topics t
                LEFT JOIN users u ON t.creator_id = u.user_id
                WHERE t.creator_id = ?
                   OR t.creator_id IN (
                       SELECT user_id FROM users WHERE role IN ('college_admin', 'major_admin')
                   )
                   OR t.category IN (
                       SELECT m.major_name 
                       FROM majors m 
                       INNER JOIN users u ON u.major_id = m.major_id 
                       WHERE u.user_id = ?
                   )
                ORDER BY t.created_at DESC
            """;
            
            return jdbcTemplate.queryForList(sql, teacherId, teacherId);
            
        } else {
            // 教师或其他角色：只能看到自己创建的 + 管理员创建的
            sql = """
                SELECT 
                    t.topic_id,
                    t.topic_name,
                    t.description,
                    t.difficulty,
                    t.category,
                    t.topic_type,
                    t.source,
                    t.requirements,
                    t.references as ref_content,
                    t.selection_count,
                    t.max_students,
                    t.status,
                    t.creator_id,
                    u.real_name as creator_name,
                    t.created_at,
                    t.updated_at
                FROM topics t
                LEFT JOIN users u ON t.creator_id = u.user_id
                WHERE t.creator_id = ?
                   OR t.creator_id IN (
                       SELECT user_id FROM users WHERE role IN ('college_admin', 'major_admin')
                   )
                ORDER BY t.created_at DESC
            """;
            
            return jdbcTemplate.queryForList(sql, teacherId);
        }
    }

    @Override
    public List<Map<String, Object>> getPendingSelections(Integer teacherId) {
        String sql = """
            SELECT 
                ss.selection_id,
                ss.student_id,
                ss.topic_id,
                ss.topic_type,
                ss.self_topic_name,
                ss.version,
                ss.status,
                ss.review_comment,
                ss.created_at as select_time,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                t.topic_name,
                m.major_name,
                pb.batch_name
            FROM student_selections ss
            LEFT JOIN users u ON ss.student_id = u.user_id
            LEFT JOIN topics t ON ss.topic_id = t.topic_id
            LEFT JOIN majors m ON u.major_id = m.major_id
            LEFT JOIN project_batches pb ON ss.batch_id = pb.batch_id
            LEFT JOIN teacher_student_relations tsr 
                ON tsr.student_id = ss.student_id AND tsr.batch_id = ss.batch_id
            WHERE tsr.teacher_id = ? 
              AND ss.status = 'pending'
            ORDER BY u.username ASC
        """;
        
        return jdbcTemplate.queryForList(sql, teacherId);
    }

    @Override
    public List<Map<String, Object>> getAllStudentSelections(Integer teacherId, String status) {
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String userRole = jdbcTemplate.queryForObject(roleSql, String.class, teacherId);
        
        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT 
                ss.selection_id,
                ss.student_id,
                ss.topic_id,
                ss.topic_type,
                ss.self_topic_name,
                ss.version,
                ss.status,
                ss.review_comment,
                ss.review_time,
                ss.created_at as select_time,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                u.phone as student_phone,
                u.email as student_email,
                t.topic_name,
                t.description as topic_description,
                t.difficulty,
                t.category,
                m.major_name,
                pb.batch_name,
                pb.current_phase,
                (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                 FROM teacher_student_relations tsr2
                 INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                 WHERE tsr2.student_id = ss.student_id AND tsr2.batch_id = ss.batch_id
                ) as teacher_name
            FROM student_selections ss
            LEFT JOIN users u ON ss.student_id = u.user_id
            LEFT JOIN topics t ON ss.topic_id = t.topic_id
            LEFT JOIN majors m ON u.major_id = m.major_id
            LEFT JOIN project_batches pb ON ss.batch_id = pb.batch_id
        """);
        
        if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
            // 院管/专业负责人：查看所有学生选题
            sqlBuilder.append(" WHERE 1=1");
            
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                sqlBuilder.append(" AND ss.status = ?");
                return jdbcTemplate.queryForList(sqlBuilder.toString(), status);
            } else {
                return jdbcTemplate.queryForList(sqlBuilder.toString());
            }
        } else {
            // 普通教师：只查看自己指导的学生选题
            sqlBuilder.append("""
                LEFT JOIN teacher_student_relations tsr 
                    ON tsr.student_id = ss.student_id AND tsr.batch_id = ss.batch_id
                WHERE tsr.teacher_id = ?
            """);
            
            if (status != null && !status.isEmpty() && !"all".equals(status)) {
                sqlBuilder.append(" AND ss.status = ?");
                return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId, status);
            } else {
                return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getStudentsNeedTaskBook(Integer teacherId) {
        String sql = """
            SELECT DISTINCT
                ss.selection_id,
                ss.student_id,
                ss.topic_id,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                t.topic_name,
                m.major_name,
                pb.batch_name,
                CASE WHEN tb.task_id IS NOT NULL THEN 'issued' ELSE 'unissued' END as taskbook_status
            FROM student_selections ss
            INNER JOIN users u ON ss.student_id = u.user_id
            INNER JOIN topics t ON ss.topic_id = t.topic_id
            INNER JOIN majors m ON u.major_id = m.major_id
            INNER JOIN project_batches pb ON ss.batch_id = pb.batch_id
            INNER JOIN teacher_student_relations tsr 
                ON tsr.student_id = ss.student_id AND tsr.batch_id = ss.batch_id
            LEFT JOIN task_books tb ON tb.selection_id = ss.selection_id
            WHERE tsr.teacher_id = ? 
              AND ss.status = 'approved'
            ORDER BY u.username ASC
        """;
        
        return jdbcTemplate.queryForList(sql, teacherId);
    }

    @Override
    public List<Map<String, Object>> getMyTaskBooks(Integer teacherId) {
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String userRole = jdbcTemplate.queryForObject(roleSql, String.class, teacherId);

        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT * FROM (
                SELECT
                    tb.task_id,
                    tb.selection_id,
                    tb.content,
                    tb.deadline,
                    tb.version,
                    tb.requirements,
                    tb.tech_params,
                    tb.references as ref_content,
                    tb.status as taskbook_status,
                    tb.issuer_id,
                    tb.issued_at,
                    tb.confirm_by,
                    tb.confirm_at,
                    tb.rejector_id,
                    tb.reject_time,
                    tb.reject_comment,
                    tb.created_at,
                    tb.updated_at,
                    u.username as student_no,
                    u.real_name as student_name,
                    u.class_name,
                    t.topic_name,
                    ss.status as selection_status,
                    ui.real_name as issuer_name,
                    (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                     FROM teacher_student_relations tsr2
                     INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                     WHERE tsr2.student_id = ss.student_id AND tsr2.batch_id = ss.batch_id
                    ) as teacher_name,
                    ROW_NUMBER() OVER (PARTITION BY ss.student_id ORDER BY tb.version DESC) as rn
                FROM task_books tb
                INNER JOIN student_selections ss ON tb.selection_id = ss.selection_id
                INNER JOIN users u ON ss.student_id = u.user_id
                INNER JOIN topics t ON ss.topic_id = t.topic_id
                LEFT JOIN users ui ON tb.issuer_id = ui.user_id
        """);

        if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
            // 院管/专业负责人：查看所有任务书（只显示最新版本）
            sqlBuilder.append(" WHERE 1=1");

            sqlBuilder.append("""
            ) t WHERE rn = 1
            ORDER BY student_no ASC
            """);

            return jdbcTemplate.queryForList(sqlBuilder.toString());
        } else {
            // 普通教师：只查看自己指导的学生的任务书（只显示最新版本）
            sqlBuilder.append("""
                INNER JOIN teacher_student_relations tsr
                    ON tsr.student_id = ss.student_id AND tsr.batch_id = ss.batch_id
                WHERE tsr.teacher_id = ?
            ) t WHERE rn = 1
            ORDER BY student_no ASC
            """);

            return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId);
        }
    }

    @Override
    public List<Map<String, Object>> getPendingMidtermChecks(Integer teacherId) {
        String sql = """
            SELECT 
                mc.check_id,
                mc.selection_id,
                mc.file_id,
                mc.submit_time,
                mc.version,
                mc.progress,
                mc.status,
                mc.reviewer_id,
                mc.review_time,
                mc.review_comment,
                mc.guide_file_id,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                t.topic_name,
                f.original_name as file_name
            FROM midterm_checks mc
            INNER JOIN student_selections ss ON mc.selection_id = ss.selection_id
            INNER JOIN users u ON ss.student_id = u.user_id
            INNER JOIN topics t ON ss.topic_id = t.topic_id
            LEFT JOIN files f ON mc.file_id = f.file_id
            INNER JOIN teacher_student_relations tsr 
                ON tsr.student_id = ss.student_id AND tsr.batch_id = ss.batch_id
            WHERE tsr.teacher_id = ? 
              AND mc.status IN ('submitted', 'pending')
            ORDER BY COALESCE(mc.submit_time, mc.created_at) DESC, u.username ASC
        """;
        
        return jdbcTemplate.queryForList(sql, teacherId);
    }

    @Override
    public List<Map<String, Object>> getAllMidtermChecks(Integer teacherId) {
        String sql = """
            SELECT * FROM (
                SELECT 
                    mc.check_id,
                    mc.selection_id,
                    mc.file_id,
                    mc.submit_time,
                    mc.version,
                    mc.progress,
                    mc.status,
                    mc.reviewer_id,
                    mc.review_time,
                    mc.review_comment,
                    mc.guide_file_id,
                    mc.created_at,
                    mc.updated_at,
                    u.real_name as student_name,
                    u.username as student_no,
                    u.class_name,
                    t.topic_name,
                    f.original_name as file_name,
                    ui.real_name as reviewer_name,
                    (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                     FROM teacher_student_relations tsr2
                     INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                     WHERE tsr2.student_id = ss.student_id AND tsr2.batch_id = ss.batch_id
                    ) as teacher_name,
                    ROW_NUMBER() OVER (PARTITION BY ss.student_id ORDER BY mc.version DESC) as rn
                FROM midterm_checks mc
                INNER JOIN student_selections ss ON mc.selection_id = ss.selection_id
                INNER JOIN users u ON ss.student_id = u.user_id
                INNER JOIN topics t ON ss.topic_id = t.topic_id
                LEFT JOIN files f ON mc.file_id = f.file_id
                LEFT JOIN users ui ON mc.reviewer_id = ui.user_id
                WHERE EXISTS (
                    SELECT 1 FROM teacher_student_relations tsr 
                    WHERE tsr.student_id = ss.student_id 
                    AND tsr.batch_id = ss.batch_id 
                    AND tsr.teacher_id = ?
                )
            ) t WHERE rn = 1
            ORDER BY COALESCE(t.submit_time, t.created_at) DESC, t.student_no ASC
        """;

        return jdbcTemplate.queryForList(sql, teacherId);
    }

    @Override
    public List<Map<String, Object>> getPendingFinalChecks(Integer teacherId) {
        String sql = """
            SELECT 
                fc.check_id,
                fc.selection_id,
                fc.file_id,
                f.original_name as file_name,
                fc.submit_time,
                fc.version,
                fc.is_current,
                fc.is_final,
                fc.status as check_status,
                fc.created_at,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                t.topic_name
            FROM final_checks fc
            INNER JOIN student_selections ss ON fc.selection_id = ss.selection_id
            INNER JOIN users u ON ss.student_id = u.user_id
            INNER JOIN topics t ON ss.topic_id = t.topic_id
            LEFT JOIN files f ON fc.file_id = f.file_id
            INNER JOIN teacher_student_relations tsr 
                ON tsr.student_id = ss.student_id AND tsr.batch_id = ss.batch_id
            WHERE tsr.teacher_id = ? 
              AND fc.status = 'pending'
            ORDER BY COALESCE(fc.submit_time, fc.created_at) DESC, u.username ASC
        """;
        
        return jdbcTemplate.queryForList(sql, teacherId);
    }

    @Override
    public List<Map<String, Object>> getAllFinalChecks(Integer teacherId) {
        String sql = """
            SELECT 
                fc.check_id,
                fc.selection_id,
                fc.file_id,
                fc.submit_time,
                fc.version,
                fc.is_current,
                fc.is_final,
                fc.finalized_at,
                fc.status,
                fc.reviewer_id,
                fc.review_time,
                fc.review_comment,
                fc.report_score,
                fc.guide_file_id,
                fc.created_at,
                fc.updated_at,
                u.real_name as student_name,
                u.username as student_no,
                u.class_name,
                t.topic_name,
                f.original_name as file_name,
                ui.real_name as reviewer_name,
                (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                 FROM teacher_student_relations tsr2
                 INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                 WHERE tsr2.student_id = ss.student_id AND tsr2.batch_id = ss.batch_id
                ) as teacher_name
            FROM final_checks fc
            INNER JOIN student_selections ss ON fc.selection_id = ss.selection_id
            INNER JOIN users u ON ss.student_id = u.user_id
            INNER JOIN topics t ON ss.topic_id = t.topic_id
            LEFT JOIN files f ON fc.file_id = f.file_id
            LEFT JOIN users ui ON fc.reviewer_id = ui.user_id
            WHERE EXISTS (
                SELECT 1 FROM teacher_student_relations tsr 
                WHERE tsr.student_id = ss.student_id 
                AND tsr.batch_id = ss.batch_id 
                AND tsr.teacher_id = ?
            )
            ORDER BY COALESCE(fc.submit_time, fc.created_at) DESC, fc.version DESC
        """;
        
        return jdbcTemplate.queryForList(sql, teacherId);
    }

    @Override
    public List<Map<String, Object>> getDefenseRecords(Integer teacherId, String batchId) {
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String userRole = jdbcTemplate.queryForObject(roleSql, String.class, teacherId);
        
        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT 
                d.defense_id,
                d.selection_id,
                d.defense_score,
                d.defense_score_num,
                d.record_file_id,
                d.submitter_type,
                d.submitter_id,
                d.submit_time,
                d.defense_datetime,
                d.location,
                d.committee,
                d.status,
                d.reviewer_id,
                d.review_time,
                d.review_comment,
                d.created_at,
                d.updated_at,
                u.real_name as student_name,
                u.username as student_no,
                u.class_name,
                t.topic_name,
                f.original_name as record_file_name,
                fs.total_score,
                fs.grade_level,
                (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                 FROM teacher_student_relations tsr2
                 INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                 WHERE tsr2.student_id = ss.student_id AND tsr2.batch_id = ss.batch_id
                ) as teacher_name
            FROM defenses d
            INNER JOIN student_selections ss ON d.selection_id = ss.selection_id
            INNER JOIN users u ON ss.student_id = u.user_id
            INNER JOIN topics t ON ss.topic_id = t.topic_id
            LEFT JOIN files f ON d.record_file_id = f.file_id
            LEFT JOIN final_scores fs ON d.selection_id = fs.selection_id
        """);
        
        if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
            // 院管/专业负责人：查看所有答辩记录
            sqlBuilder.append(" WHERE 1=1");
            
            if (batchId != null && !batchId.isEmpty()) {
                sqlBuilder.append(" AND ss.batch_id = ?");
                return jdbcTemplate.queryForList(sqlBuilder.toString(), batchId);
            } else {
                return jdbcTemplate.queryForList(sqlBuilder.toString());
            }
        } else {
            // 普通教师：只查看自己指导的学生的答辩记录
            sqlBuilder.append("""
                WHERE EXISTS (
                    SELECT 1 FROM teacher_student_relations tsr 
                    WHERE tsr.student_id = ss.student_id 
                    AND tsr.batch_id = ss.batch_id 
                    AND tsr.teacher_id = ?
                )
            """);
            
            if (batchId != null && !batchId.isEmpty()) {
                sqlBuilder.append(" AND ss.batch_id = ?");
                return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId, batchId);
            } else {
                return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId);
            }
        }
    }

    @Override
    public List<Map<String, Object>> getStudentSignIns(Integer teacherId, Integer studentId, String dateRange) {
        String roleSql = "SELECT role FROM users WHERE user_id = ?";
        String userRole = jdbcTemplate.queryForObject(roleSql, String.class, teacherId);
        
        StringBuilder sqlBuilder;
        
        if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
            // 院管/专业负责人：查看所有学生的签到记录（去重）
            sqlBuilder = new StringBuilder("""
                SELECT * FROM (
                    SELECT
                        u.user_id as student_id,
                        u.username as student_no,
                        u.real_name as student_name,
                        u.class_name,
                        (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                         FROM teacher_student_relations tsr2
                         INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                         WHERE tsr2.student_id = u.user_id
                        ) as teacher_name,
                        (SELECT pb2.batch_name FROM teacher_student_relations tsr_main 
                         LEFT JOIN project_batches pb2 ON tsr_main.batch_id = pb2.batch_id 
                         WHERE tsr_main.student_id = u.user_id LIMIT 1) as batch_name,
                        (SELECT DATEDIFF(pb3.end_date, pb3.start_date) + 1 FROM teacher_student_relations tsr_b 
                         LEFT JOIN project_batches pb3 ON tsr_b.batch_id = pb3.batch_id 
                         WHERE tsr_b.student_id = u.user_id LIMIT 1) as total_days,
                        (SELECT COUNT(*) FROM sign_ins si WHERE si.student_id = u.user_id AND si.sign_status = 'normal') as checked_days,
                        (SELECT CONCAT(si_last.sign_date, ' ', si_last.sign_time) FROM sign_ins si_last 
                         WHERE si_last.student_id = u.user_id ORDER BY si_last.created_at DESC LIMIT 1) as last_sign_time,
                        (SELECT si_loc.location FROM sign_ins si_loc 
                         WHERE si_loc.student_id = u.user_id ORDER BY si_loc.created_at DESC LIMIT 1) as location,
                        ROW_NUMBER() OVER (PARTITION BY u.user_id ORDER BY u.user_id) as rn
                    FROM users u
                    INNER JOIN teacher_student_relations tsr ON tsr.student_id = u.user_id
                    WHERE u.role = 'student'
                ) t WHERE rn = 1
            """);
        } else {
            // 普通教师：只查看自己指导的学生的签到记录（去重）
            sqlBuilder = new StringBuilder("""
                SELECT * FROM (
                    SELECT
                        u.user_id as student_id,
                        u.username as student_no,
                        u.real_name as student_name,
                        u.class_name,
                        (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                         FROM teacher_student_relations tsr2
                         INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                         WHERE tsr2.student_id = u.user_id AND tsr2.teacher_id = ?
                        ) as teacher_name,
                        pb.batch_name,
                        DATEDIFF(pb.end_date, pb.start_date) + 1 as total_days,
                        (SELECT COUNT(*) FROM sign_ins si2 
                         WHERE si2.student_id = u.user_id AND si2.sign_status = 'normal') as checked_days,
                        (SELECT CONCAT(si3.sign_date, ' ', si3.sign_time) FROM sign_ins si3 
                         WHERE si3.student_id = u.user_id ORDER BY si3.created_at DESC LIMIT 1) as last_sign_time,
                        (SELECT si4.location FROM sign_ins si4 
                         WHERE si4.student_id = u.user_id ORDER BY si4.created_at DESC LIMIT 1) as location,
                        ROW_NUMBER() OVER (PARTITION BY u.user_id ORDER BY u.user_id) as rn
                    FROM users u
                    INNER JOIN teacher_student_relations tsr ON tsr.student_id = u.user_id
                    LEFT JOIN project_batches pb ON tsr.batch_id = pb.batch_id
                    WHERE tsr.teacher_id = ?
                ) t WHERE rn = 1
            """);
        }
        
        if (studentId != null) {
            sqlBuilder.append(" AND u.user_id = ?");
        }
        
        if (dateRange != null && !dateRange.isEmpty()) {
            if (dateRange.length() == 7) {
                sqlBuilder.append(" AND EXISTS (SELECT 1 FROM sign_ins si_filter WHERE si_filter.student_id = u.user_id AND DATE_FORMAT(si_filter.sign_date, '%%Y-%%m') = ?)");
            } else if (dateRange.length() == 10) {
                sqlBuilder.append(" AND EXISTS (SELECT 1 FROM sign_ins si_filter WHERE si_filter.student_id = u.user_id AND si_filter.sign_date = ?)");
            }
            
            if (studentId != null) {
                if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
                    return jdbcTemplate.queryForList(sqlBuilder.toString(), studentId, dateRange);
                } else {
                    return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId, teacherId, studentId, dateRange);
                }
            } else {
                if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
                    return jdbcTemplate.queryForList(sqlBuilder.toString(), dateRange);
                } else {
                    return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId, teacherId, dateRange);
                }
            }
        } else {
            if (studentId != null) {
                if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
                    return jdbcTemplate.queryForList(sqlBuilder.toString(), studentId);
                } else {
                    return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId, teacherId, studentId);
                }
            } else {
                if ("college_admin".equals(userRole) || "major_admin".equals(userRole)) {
                    return jdbcTemplate.queryForList(sqlBuilder.toString());
                } else {
                    return jdbcTemplate.queryForList(sqlBuilder.toString(), teacherId, teacherId);
                }
            }
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableSemesters(Integer teacherId) {
        String sql = """
            SELECT DISTINCT 
                CAST(SUBSTRING(batch_name, 1, 4) AS UNSIGNED) as grade,
                CONCAT(CAST(SUBSTRING(batch_name, 1, 4) AS UNSIGNED), '届') as label
            FROM project_batches
            WHERE batch_name REGEXP '^[0-9]{4}届'
            ORDER BY grade DESC
        """;

        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<String> getTopicCategories(Integer teacherId) {
        String sql = """
            SELECT DISTINCT category 
            FROM topics 
            WHERE category IS NOT NULL AND category != ''
            ORDER BY category ASC
        """;
        
        return jdbcTemplate.queryForList(sql, String.class);
    }

    @Override
    public List<Map<String, Object>> getAvailableStudentsForRelation(Integer teacherId) {
        String sql = """
            SELECT
                u.user_id as student_id,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                m.major_name,
                c.college_name
            FROM users u
            LEFT JOIN majors m ON u.major_id = m.major_id
            LEFT JOIN colleges c ON m.college_id = c.college_name
            WHERE u.role = 'student'
              AND u.status = 1
              AND NOT EXISTS (
                  SELECT 1 FROM teacher_student_relations tsr
                  WHERE tsr.student_id = u.user_id
              )
            ORDER BY u.username ASC
        """;

        return jdbcTemplate.queryForList(sql);
    }

    @Override
    public List<Map<String, Object>> getAllStudentScores() {
        List<Map<String, Object>> result = new java.util.ArrayList<>();
        
        try {
            // 第1步：查询所有学生基本信息
            String sqlStudents = "SELECT user_id, username, real_name, class_name FROM users WHERE role = 'student' ORDER BY username ASC LIMIT 100";
            log.info("【成绩查询】第1步：查询学生列表...");
            List<Map<String, Object>> students = jdbcTemplate.queryForList(sqlStudents);
            log.info("【成绩查询】找到 {} 个学生", students.size());
            
            // 第2步：为每个学生补充信息
            for (Map<String, Object> student : students) {
                Map<String, Object> score = new java.util.HashMap<>();
                
                // 基本信息
                score.put("id", student.get("user_id"));
                score.put("studentNo", student.get("username"));
                score.put("studentName", student.get("real_name"));
                score.put("className", student.get("class_name"));
                
                try {
                    // 第3步：查询该学生的选题信息（获取最新一条）
                    Integer studentId = (Integer) student.get("user_id");
                    String sqlTopic = "SELECT t.topic_name FROM student_selections ss INNER JOIN topics t ON ss.topic_id = t.topic_id WHERE ss.student_id = ? ORDER BY ss.selection_id DESC LIMIT 1";
                    
                    List<Map<String, Object>> topics = jdbcTemplate.queryForList(sqlTopic, studentId);
                    if (topics != null && !topics.isEmpty()) {
                        score.put("topicName", topics.get(0).get("topic_name"));
                    } else {
                        score.put("topicName", null);
                    }
                    
                    // 第4步：查询最终检查报告分（定稿版本）
                    String sqlReport = "SELECT report_score FROM final_checks WHERE selection_id IN (SELECT selection_id FROM student_selections WHERE student_id = ?) AND is_final = 1 LIMIT 1";
                    List<Map<String, Object>> reports = jdbcTemplate.queryForList(sqlReport, studentId);
                    if (reports != null && !reports.isEmpty() && reports.get(0).get("report_score") != null) {
                        Double reportScore = ((Number) reports.get(0).get("report_score")).doubleValue();
                        if (reportScore > 0) {
                            score.put("reportScore", String.format("%.1f", reportScore));
                        } else {
                            score.put("reportScore", null);
                        }
                    } else {
                        score.put("reportScore", null);
                    }
                    
                    // 第5步：查询答辩分数和状态（重要：必须审核通过才显示）
                    String sqlDefense = """ 
                        SELECT defense_score_num, status 
                        FROM defenses 
                        WHERE selection_id IN (
                            SELECT selection_id FROM student_selections WHERE student_id = ?
                        ) 
                        ORDER BY created_at DESC 
                        LIMIT 1
                    """;
                    List<Map<String, Object>> defenses = jdbcTemplate.queryForList(sqlDefense, studentId);
                    
                    String defenseStatus = null;  // 当前答辩状态
                    if (defenses != null && !defenses.isEmpty()) {
                        defenseStatus = (String) defenses.get(0).get("status");
                        
                        // 只有"已通过"状态才显示分数
                        if ("approved".equals(defenseStatus) && defenses.get(0).get("defense_score_num") != null) {
                            Double defenseScore = ((Number) defenses.get(0).get("defense_score_num")).doubleValue();
                            if (defenseScore > 0) {
                                score.put("defenseScore", String.format("%.1f", defenseScore));
                            } else {
                                score.put("defenseScore", null);
                            }
                        } else {
                            // 待审核、未开始、已驳回等状态都不显示分数
                            score.put("defenseScore", null);
                            log.info("【成绩查询】学生 {} 答辩状态为 {}，不显示分数", student.get("username"), defenseStatus);
                        }
                    } else {
                        score.put("defenseScore", null);
                    }
                    
                    // 第6步：查询总成绩（只有答辩通过后才计算和显示）
                    String sqlTotal = "SELECT total_score, grade_level FROM final_scores WHERE selection_id IN (SELECT selection_id FROM student_selections WHERE student_id = ?) LIMIT 1";
                    List<Map<String, Object>> totals = jdbcTemplate.queryForList(sqlTotal, studentId);
                    
                    Double dbTotalScore = null;
                    String dbGradeLevel = null;
                    
                    // 【核心修复】只有答辩已通过才允许显示总成绩
                    boolean canShowScore = "approved".equals(defenseStatus);
                    
                    if (canShowScore && totals != null && !totals.isEmpty() && totals.get(0).get("total_score") != null) {
                        dbTotalScore = ((Number) totals.get(0).get("total_score")).doubleValue();
                        dbGradeLevel = (String) totals.get(0).get("grade_level");
                    }
                    
                    Double reportScoreNum = null;
                    Double defenseScoreNum = null;
                    
                    if (score.get("reportScore") != null) {
                        reportScoreNum = Double.parseDouble((String) score.get("reportScore"));
                    }
                    if (score.get("defenseScore") != null) {
                        defenseScoreNum = Double.parseDouble((String) score.get("defenseScore"));
                    }
                    
                    Double finalTotalScore = null;
                    String finalGradeLevel = null;
                    
                    // 判断是否需要计算并显示成绩
                    if (canShowScore) {
                        boolean needRecalculate = false;
                        if (dbTotalScore == null || dbTotalScore <= 0) {
                            if (reportScoreNum != null && defenseScoreNum != null) {
                                needRecalculate = true;
                            }
                        }
                        
                        if (needRecalculate) {
                            double reportRatio = 0.6;
                            double defenseRatio = 0.4;
                            
                            try {
                                String sqlBatch = """ 
                                    SELECT pb.report_ratio, pb.defense_ratio 
                                    FROM project_batches pb 
                                    INNER JOIN student_selections ss ON pb.batch_id = ss.batch_id 
                                    WHERE ss.student_id = ? 
                                    LIMIT 1
                                """;
                                List<Map<String, Object>> batches = jdbcTemplate.queryForList(sqlBatch, studentId);
                                if (batches != null && !batches.isEmpty()) {
                                    if (batches.get(0).get("report_ratio") != null) {
                                        reportRatio = ((Number) batches.get(0).get("report_ratio")).doubleValue();
                                    }
                                    if (batches.get(0).get("defense_ratio") != null) {
                                        defenseRatio = ((Number) batches.get(0).get("defense_ratio")).doubleValue();
                                    }
                                }
                            } catch (Exception e) {
                                log.warn("【成绩查询】获取批次权重失败，使用默认值: {}", e.getMessage());
                            }
                            
                            finalTotalScore = reportScoreNum * reportRatio + defenseScoreNum * defenseRatio;
                            finalTotalScore = Math.round(finalTotalScore * 100.0) / 100.0;
                            finalGradeLevel = determineGradeLevel(finalTotalScore);
                            
                            log.info("【成绩查询】学生 {} 实时计算总成绩: 报告={}×{} + 答辩={}×{} = {} ({})", 
                                student.get("username"), reportScoreNum, reportRatio, defenseScoreNum, defenseRatio, finalTotalScore, finalGradeLevel);
                        } else {
                            finalTotalScore = dbTotalScore;
                            finalGradeLevel = dbGradeLevel;
                        }
                    } else {
                        // 答辩未通过 → 不显示成绩和等级
                        log.info("【成绩查询】学生 {} 答辩未通过（状态={}），不显示总成绩", student.get("username"), defenseStatus);
                    }
                    
                    // 设置最终结果
                    if (finalTotalScore != null && finalTotalScore > 0) {
                        score.put("totalScore", String.format("%.1f", finalTotalScore));
                        score.put("gradeLevel", finalGradeLevel);
                    } else {
                        score.put("totalScore", null);
                        score.put("gradeLevel", null);
                    }
                    
                    // 根据答辩状态设置显示状态
                    if (defenseStatus != null) {
                        score.put("status", defenseStatus);  // 使用实际的答辩状态
                    } else if (score.get("totalScore") != null || score.get("defenseScore") != null) {
                        score.put("status", "approved");
                    } else {
                        score.put("status", "not_started");
                    }
                    
                } catch (Exception e) {
                    log.warn("【成绩查询】处理学生 {} 数据时出错: {}", student.get("username"), e.getMessage());
                    score.put("topicName", null);
                    score.put("reportScore", null);
                    score.put("defenseScore", null);
                    score.put("totalScore", null);
                    score.put("gradeLevel", null);
                    score.put("status", "not_started");
                }
                
                result.add(score);
            }
            
            log.info("【成绩查询】成功组装 {} 条完整成绩记录", result.size());
            
        } catch (Exception e) {
            log.error("【成绩查询】主流程失败: {}", e.getMessage(), e);
            
            // 降级方案：返回最简单的数据
            try {
                String sqlSimple = "SELECT user_id as id, username as studentNo, real_name as studentName, class_name as className FROM users WHERE role = 'student' LIMIT 50";
                result = jdbcTemplate.queryForList(sqlSimple);
                log.info("【成绩查询】使用降级方案，获取 {} 条", result.size());
            } catch (Exception e2) {
                log.error("【成绩查询】降级也失败: {}", e2.getMessage());
                result = new java.util.ArrayList<>();
            }
        }
        
        return result;
    }

    /**
     * 安全地格式化分数为字符串（保留1位小数）
     * 处理各种可能的输入类型：Number、String、null等
     */
    private String formatScore(Object value) {
        if (value == null) {
            return null;
        }
        
        try {
            double numValue;
            
            if (value instanceof Number) {
                numValue = ((Number) value).doubleValue();
            } else if (value instanceof String) {
                String strVal = ((String) value).trim();
                if (strVal.isEmpty() || "null".equalsIgnoreCase(strVal)) {
                    return null;
                }
                numValue = Double.parseDouble(strVal);
            } else {
                return value.toString();
            }
            
            // 如果值为0或负数，返回null表示无数据
            if (numValue <= 0) {
                return null;
            }
            
            return String.format("%.1f", numValue);
        } catch (Exception e) {
            log.warn("格式化分数失败: {} -> {}", value, e.getMessage());
            return value != null ? value.toString() : null;
        }
    }
    
    /**
     * 根据总分确定成绩等级
     */
    private String determineGradeLevel(Double totalScore) {
        if (totalScore == null) {
            return null;
        }
        
        if (totalScore >= 90) {
            return "A";
        } else if (totalScore >= 80) {
            return "B";
        } else if (totalScore >= 70) {
            return "C";
        } else if (totalScore >= 60) {
            return "D";
        } else {
            return "F";
        }
    }
}
