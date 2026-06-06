-- ============================================================
-- 毕业设计管理系统 - 学生信息完整查询视图
-- 功能：在所有业务表中展示对应的学生学号
-- 创建时间：2026-06-05
-- ============================================================

-- ============================================================
-- 1. 学生选题详情视图（包含学号）
-- ============================================================
CREATE OR REPLACE VIEW v_student_selections_detail AS
SELECT 
    ss.selection_id,
    ss.batch_id,
    ss.student_id,
    u.username AS student_no,           -- 学号
    u.real_name AS student_name,        -- 姓名
    u.class_name AS class_name,         -- 班级
    m.major_name AS major_name,         -- 专业
    ss.topic_id,
    t.topic_name,
    ss.topic_type,
    ss.version,
    ss.status AS selection_status,
    ss.reviewer_id,
    ur.real_name AS reviewer_name,
    ss.review_time,
    ss.review_comment,
    ss.created_at AS selection_created_at,
    ss.updated_at AS selection_updated_at
FROM student_selections ss
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN majors m ON u.major_id = m.major_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN users ur ON ss.reviewer_id = ur.user_id;

-- ============================================================
-- 2. 签到记录详情视图（包含学号）
-- ============================================================
CREATE OR REPLACE VIEW v_sign_ins_detail AS
SELECT 
    si.sign_id,
    si.student_id,
    u.username AS student_no,           -- 学号
    u.real_name AS student_name,        -- 姓名
    u.class_name AS class_name,         -- 班级
    si.batch_id,
    si.sign_date,
    si.sign_time,
    si.location,
    si.daily_report,
    si.is_makeup,
    si.makeup_reason,
    si.sign_status,
    si.sign_out_time,
    si.duration_minutes,
    si.ip_address,
    si.created_at
FROM sign_ins si
INNER JOIN users u ON si.student_id = u.user_id;

-- ============================================================
-- 3. 中期检查详情视图（包含学号）
-- ============================================================
CREATE OR REPLACE VIEW v_midterm_checks_detail AS
SELECT 
    mc.check_id,
    mc.selection_id,
    ss.student_id,
    u.username AS student_no,           -- 学号
    u.real_name AS student_name,        -- 姓名
    u.class_name AS class_name,         -- 班级
    t.topic_name,
    mc.file_id,
    f.original_name AS file_name,
    mc.submit_time,
    mc.version,
    mc.is_current,
    mc.progress,
    mc.status AS check_status,
    mc.reviewer_id,
    ur.real_name AS reviewer_name,
    mc.review_time,
    mc.review_comment,
    mc.guide_file_id,
    mc.created_at,
    mc.updated_at
FROM midterm_checks mc
INNER JOIN student_selections ss ON mc.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN files f ON mc.file_id = f.file_id
LEFT JOIN users ur ON mc.reviewer_id = ur.user_id;

-- ============================================================
-- 4. 最终检查详情视图（包含学号）
-- ============================================================
CREATE OR REPLACE VIEW v_final_checks_detail AS
SELECT 
    fc.check_id,
    fc.selection_id,
    ss.student_id,
    u.username AS student_no,           -- 学号
    u.real_name AS student_name,        -- 姓名
    u.class_name AS class_name,         -- 班级
    t.topic_name,
    fc.file_id,
    fo.original_name AS file_name,
    fc.submit_time,
    fc.version,
    fc.is_current,
    fc.is_final,
    fc.finalized_at,
    fc.status AS check_status,
    fc.reviewer_id,
    ur.real_name AS reviewer_name,
    fc.review_time,
    fc.review_comment,
    fc.report_score,
    fc.guide_file_id,
    fc.created_at,
    fc.updated_at
FROM final_checks fc
INNER JOIN student_selections ss ON fc.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN files fo ON fc.file_id = fo.file_id
LEFT JOIN users ur ON fc.reviewer_id = ur.user_id;

-- ============================================================
-- 5. 答辩记录详情视图（包含学号）
-- ============================================================
CREATE OR REPLACE VIEW v_defenses_detail AS
SELECT 
    d.defense_id,
    d.selection_id,
    ss.student_id,
    u.username AS student_no,           -- 学号
    u.real_name AS student_name,        -- 姓名
    u.class_name AS class_name,         -- 班级
    t.topic_name,
    d.defense_score,
    d.defense_score_num,
    d.record_file_id,
    rf.original_name AS record_file_name,
    d.submitter_type,
    d.submitter_id,
    st.real_name AS submitter_name,
    d.submit_time,
    d.defense_datetime,
    d.location,
    d.committee,
    d.status AS defense_status,
    d.reviewer_id,
    rv.real_name AS reviewer_name,
    d.review_time,
    d.review_comment,
    d.created_at,
    d.updated_at
FROM defenses d
INNER JOIN student_selections ss ON d.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN files rf ON d.record_file_id = rf.file_id
LEFT JOIN users st ON d.submitter_id = st.user_id
LEFT JOIN users rv ON d.reviewer_id = rv.user_id;

-- ============================================================
-- 6. 最终成绩详情视图（包含学号）
-- ============================================================
CREATE OR REPLACE VIEW v_final_scores_detail AS
SELECT 
    fs.score_id,
    fs.selection_id,
    ss.student_id,
    u.username AS student_no,           -- 学号
    u.real_name AS student_name,        -- 姓名
    u.class_name AS class_name,         -- 班级
    m.major_name AS major_name,         -- 专业
    t.topic_name,
    fs.report_score,
    fs.defense_score,
    fs.total_score,
    fs.grade_level,
    fs.is_published,
    fs.published_by,
    pb.real_name AS publisher_name,
    fs.published_at,
    fs.calculated_at,
    fs.calculation_mode,
    fs.created_at,
    fs.updated_at
FROM final_scores fs
INNER JOIN student_selections ss ON fs.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN majors m ON u.major_id = m.major_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN users pb ON fs.published_by = pb.user_id;

-- ============================================================
-- 7. 学生毕设完整信息汇总视图（包含学号）
-- ============================================================
CREATE OR REPLACE VIEW v_student_graduation_full AS
SELECT 
    u.user_id,
    u.username AS student_no,           -- 学号
    u.real_name AS student_name,        -- 姓名
    u.phone,
    u.email,
    u.class_name AS class_name,         -- 班级
    m.major_name AS major_name,         -- 专业
    c.college_name AS college_name,     -- 学院
    pb.batch_name,
    pb.batch_code,
    t.topic_id,
    t.topic_name,
    t.difficulty,
    t.category,
    ss.status AS selection_status,
    tb.task_id,
    tb.status AS taskbook_status,
    tb.content AS taskbook_content,
    tb.deadline AS taskbook_deadline,
    mc.check_id AS midterm_check_id,
    mc.status AS midterm_status,
    mc.progress AS midterm_progress,
    mc.submit_time AS midterm_submit_time,
    mc.review_comment AS midterm_review_comment,
    fc.check_id AS final_check_id,
    fc.status AS final_status,
    fc.is_final,
    fc.finalized_at,
    fc.report_score,
    fc.review_comment AS final_review_comment,
    d.defense_id,
    d.defense_score,
    d.defense_datetime,
    d.location AS defense_location,
    fs.score_id,
    fs.total_score,
    fs.grade_level,
    fs.is_published
FROM users u
LEFT JOIN majors m ON u.major_id = m.major_id
LEFT JOIN colleges c ON u.college_id = c.college_id
LEFT JOIN teacher_student_relations tsr ON u.user_id = tsr.student_id
LEFT JOIN project_batches pb ON tsr.batch_id = pb.batch_id
LEFT JOIN student_selections ss ON u.user_id = ss.student_id AND tsr.batch_id = ss.batch_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN task_books tb ON ss.selection_id = tb.selection_id
LEFT JOIN midterm_checks mc ON ss.selection_id = mc.selection_id AND mc.is_current = 1
LEFT JOIN final_checks fc ON ss.selection_id = fc.selection_id AND fc.is_current = 1
LEFT JOIN defenses d ON ss.selection_id = d.selection_id
LEFT JOIN final_scores fs ON ss.selection_id = fs.selection_id
WHERE u.role = 'student';

-- ============================================================
-- 查询示例（使用方法）
-- ============================================================

-- 示例1：查询所有学生的选题情况（含学号）
-- SELECT student_no, student_name, class_name, major_name, topic_name, selection_status 
-- FROM v_student_selections_detail 
-- ORDER BY student_no;

-- 示例2：查询签到记录（含学号）
-- SELECT student_no, student_name, sign_date, sign_time, sign_status, location, daily_report
-- FROM v_sign_ins_detail 
-- WHERE batch_id = 1 
-- ORDER BY sign_date DESC, student_no;

-- 示例3：查询中期检查情况（含学号）
-- SELECT student_no, student_name, topic_name, progress, status, review_comment
-- FROM v_midterm_checks_detail 
-- WHERE status != 'draft'
-- ORDER BY submit_time DESC;

-- 示例4：查询最终检查和成绩（含学号）
-- SELECT student_no, student_name, topic_name, report_score, grade_level, is_final
-- FROM v_final_scores_detail 
-- WHERE is_published = 1
-- ORDER BY total_score DESC;

-- 示例5：查询学生毕设完整进度（含学号）
-- SELECT student_no, student_name, class_name, major_name, topic_name, 
--        selection_status, midterm_status, final_status, defense_score, total_score, grade_level
-- FROM v_student_graduation_full 
-- WHERE batch_id IS NOT NULL
-- ORDER BY student_no;