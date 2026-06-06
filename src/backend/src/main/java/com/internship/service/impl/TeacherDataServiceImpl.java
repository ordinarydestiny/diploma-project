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
        String sql = """
            SELECT 
                tsr.id as relation_id,
                tsr.student_id,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                m.major_name,
                c.college_name,
                tsr.batch_id,
                pb.batch_name,
                tsr.created_at as assigned_time
            FROM teacher_student_relations tsr
            LEFT JOIN users u ON tsr.student_id = u.user_id
            LEFT JOIN majors m ON u.major_id = m.major_id
            LEFT JOIN colleges c ON m.college_id = c.college_id
            LEFT JOIN project_batches pb ON tsr.batch_id = pb.batch_id
            WHERE tsr.teacher_id = ?
            ORDER BY u.username ASC
        """;
        
        return jdbcTemplate.queryForList(sql, teacherId);
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
        String sql = """
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
                ) as teacher_name
            FROM task_books tb
            INNER JOIN student_selections ss ON tb.selection_id = ss.selection_id
            INNER JOIN users u ON ss.student_id = u.user_id
            INNER JOIN topics t ON ss.topic_id = t.topic_id
            LEFT JOIN users ui ON tb.issuer_id = ui.user_id
            INNER JOIN teacher_student_relations tsr 
                ON tsr.student_id = ss.student_id AND tsr.batch_id = ss.batch_id
            WHERE tsr.teacher_id = ?
            ORDER BY u.username ASC, COALESCE(tb.issued_at, tb.created_at) DESC
        """;
        
        return jdbcTemplate.queryForList(sql, teacherId);
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
                ) as teacher_name
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
            ORDER BY COALESCE(mc.submit_time, mc.created_at) DESC, mc.version DESC
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

    @Override
    public List<Map<String, Object>> getStudentSignIns(Integer teacherId, Integer studentId, String dateRange) {
        StringBuilder sqlBuilder = new StringBuilder("""
            SELECT
                u.user_id as student_id,
                u.username as student_no,
                u.real_name as student_name,
                u.class_name,
                (SELECT GROUP_CONCAT(DISTINCT tchr.real_name SEPARATOR '、')
                 FROM teacher_student_relations tsr2
                 INNER JOIN users tchr ON tsr2.teacher_id = tchr.user_id
                 WHERE tsr2.student_id = u.user_id AND tsr2.batch_id = (
                     SELECT batch_id FROM teacher_student_relations tsr3 
                     WHERE tsr3.student_id = u.user_id AND tsr3.teacher_id = ? LIMIT 1
                 )
                ) as teacher_name,
                pb.batch_name,
                DATEDIFF(pb.end_date, pb.start_date) + 1 as total_days,
                (SELECT COUNT(*) FROM sign_ins si2 
                 WHERE si2.student_id = u.user_id 
                 AND si2.sign_status = 'normal'
                 AND si2.batch_id = (
                     SELECT batch_id FROM teacher_student_relations tsr4 
                     WHERE tsr4.student_id = u.user_id AND tsr4.teacher_id = ? LIMIT 1
                 )
                ) as checked_days,
                (SELECT CONCAT(si3.sign_date, ' ', si3.sign_time) FROM sign_ins si3 
                 WHERE si3.student_id = u.user_id 
                 AND si3.batch_id = (
                     SELECT batch_id FROM teacher_student_relations tsr5 
                     WHERE tsr5.student_id = u.user_id AND tsr5.teacher_id = ? LIMIT 1
                 )
                 ORDER BY si3.created_at DESC LIMIT 1
                ) as last_sign_time,
                (SELECT si4.location FROM sign_ins si4 
                 WHERE si4.student_id = u.user_id 
                 AND si4.batch_id = (
                     SELECT batch_id FROM teacher_student_relations tsr6 
                     WHERE tsr6.student_id = u.user_id AND tsr6.teacher_id = ? LIMIT 1
                 )
                 ORDER BY si4.created_at DESC LIMIT 1
                ) as location
            FROM users u
            INNER JOIN teacher_student_relations tsr ON tsr.student_id = u.user_id
            LEFT JOIN project_batches pb ON tsr.batch_id = pb.batch_id
            WHERE tsr.teacher_id = ?
        """);
        
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
                return jdbcTemplate.queryForList(
                    sqlBuilder.toString(), 
                    teacherId, teacherId, teacherId, teacherId, teacherId, studentId, dateRange
                );
            } else {
                return jdbcTemplate.queryForList(
                    sqlBuilder.toString(), 
                    teacherId, teacherId, teacherId, teacherId, teacherId, dateRange
                );
            }
        } else {
            if (studentId != null) {
                return jdbcTemplate.queryForList(
                    sqlBuilder.toString(),
                    teacherId, teacherId, teacherId, teacherId, teacherId, studentId
                );
            } else {
                return jdbcTemplate.queryForList(
                    sqlBuilder.toString(),
                    teacherId, teacherId, teacherId, teacherId, teacherId
                );
            }
        }
    }

    @Override
    public List<Map<String, Object>> getAvailableSemesters(Integer teacherId) {
        String sql = """
            SELECT DISTINCT 
                pb.batch_id,
                pb.semester,
                pb.batch_name,
                pb.start_date,
                pb.end_date,
                pb.status as batch_status
            FROM project_batches pb
            WHERE pb.status IN ('active', 'completed')
            ORDER BY pb.start_date DESC
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
}
