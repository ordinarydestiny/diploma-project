-- ============================================================
-- 快速查询示例：展示学生学号
-- 使用方法：直接复制这些SQL语句到MySQL中执行
-- ============================================================

-- ============================================================
-- 1️⃣ 查询学生基本信息（含学号）
-- ============================================================
SELECT 
    user_id,
    username AS 学号,
    real_name AS 姓名,
    class_name AS 班级,
    phone AS 手机号,
    email AS 邮箱
FROM users 
WHERE role = 'student'
ORDER BY username
LIMIT 20;

-- 结果示例：
-- | user_id | 学号          | 姓名   | 班级     | 手机号        | 邮箱                     |
-- |---------|---------------|--------|----------|---------------|--------------------------|
-- | 8       | 202358210001  | 张三   | 软工2101 | 13801000001   | zhangsan@student.edu.cn   |
-- | 9       | 202358210002  | 李四   | 软工2101 | 13801000002   | lisi@student.edu.cn      |
-- | 10      | 202358210003  | 王五   | 软工2101 | 13801000003   | wangwu@student.edu.cn     |

-- ============================================================
-- 2️⃣ 查询学生选题情况（含学号）
-- ============================================================
SELECT 
    u.username AS 学号,
    u.real_name AS 姓名,
    u.class_name AS 班级,
    m.major_name AS 专业,
    t.topic_name AS 题目名称,
    ss.status AS 选题状态,
    ss.version AS 版本,
    ur.real_name AS 审核人,
    ss.review_time AS 审核时间
FROM student_selections ss
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN majors m ON u.major_id = m.major_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN users ur ON ss.reviewer_id = ur.user_id
WHERE ss.batch_id = 1
ORDER BY u.username
LIMIT 20;

-- ============================================================
-- 3️⃣ 查询签到记录（含学号）
-- ============================================================
SELECT 
    u.username AS 学号,
    u.real_name AS 姓名,
    si.sign_date AS 签到日期,
    si.sign_time AS 签到时间,
    si.location AS 签到地点,
    si.sign_status AS 状态,
    si.daily_report AS 工作内容,
    si.duration_minutes AS 时长(分钟)
FROM sign_ins si
INNER JOIN users u ON si.student_id = u.user_id
WHERE si.batch_id = 1
ORDER BY si.sign_date DESC, u.username
LIMIT 30;

-- ============================================================
-- 4️⃣ 查询中期检查（含学号）
-- ============================================================
SELECT 
    u.username AS 学号,
    u.real_name AS 姓名,
    t.topic_name AS 题目名称,
    mc.progress AS 进度(%),
    mc.status AS 状态,
    mc.submit_time AS 提交时间,
    ur.real_name AS 审核老师,
    mc.review_comment AS 审核意见
FROM midterm_checks mc
INNER JOIN student_selections ss ON mc.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN users ur ON mc.reviewer_id = ur.user_id
WHERE mc.status != 'draft'
ORDER BY mc.submit_time DESC;

-- ============================================================
-- 5️⃣ 查询最终检查（含学号）
-- ============================================================
SELECT 
    u.username AS 学号,
    u.real_name AS 姓名,
    t.topic_name AS 题目名称,
    fc.report_score AS 报告分数,
    fc.status AS 状态,
    fc.is_final AS 是否定稿,
    fc.finalized_at AS 定稿时间,
    ur.real_name AS 审核老师,
    fc.review_comment AS 教师评语
FROM final_checks fc
INNER JOIN student_selections ss ON fc.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN users ur ON fc.reviewer_id = ur.user_id
WHERE fc.is_current = 1
ORDER BY fc.submit_time DESC;

-- ============================================================
-- 6️⃣ 查询答辩记录（含学号）
-- ============================================================
SELECT 
    u.username AS 学号,
    u.real_name AS 姓名,
    t.topic_name AS 题目名称,
    d.defense_score AS 答辩等级,
    d.defense_score_num AS 答辩分数,
    d.defense_datetime AS 答辩时间,
    d.location AS 答辩地点,
    d.committee AS 答辩委员会,
    d.status AS 状态
FROM defenses d
INNER JOIN student_selections ss ON d.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
ORDER BY d.defense_score_num DESC;

-- ============================================================
-- 7️⃣ 查询最终成绩（含学号）
-- ============================================================
SELECT 
    u.username AS 学号,
    u.real_name AS 姓名,
    m.major_name AS 专业,
    t.topic_name AS 题目名称,
    fs.report_score AS 报告分,
    fs.defense_score AS 答辩分,
    fs.total_score AS 总分,
    fs.grade_level AS 等级,
    fs.is_published AS 是否公布
FROM final_scores fs
INNER JOIN student_selections ss ON fs.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN majors m ON u.major_id = m.major_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
WHERE fs.is_published = 1
ORDER BY fs.total_score DESC;

-- ============================================================
-- 8️⃣ 查询学生完整毕设进度（含学号）- 最常用！
-- ============================================================
SELECT 
    u.username AS 学号,
    u.real_name AS 姓名,
    u.class_name AS 班级,
    m.major_name AS 专业,
    pb.batch_name AS 批次,
    CASE 
        WHEN ss.status = 'approved' THEN '✅ 已选题'
        WHEN ss.status = 'pending' THEN '⏳ 待审核'
        WHEN ss.status = 'rejected' THEN '❌ 已驳回'
        ELSE '未选题'
    END AS 选题状态,
    t.topic_name AS 题目名称,
    CASE 
        WHEN tb.status = 'confirmed' THEN '✅ 已确认'
        WHEN tb.status = 'issued' THEN '📋 已下达'
        ELSE '未下发'
    END AS 任务书状态,
    CASE 
        WHEN mc.status = 'approved' THEN '✅ 已通过'
        WHEN mc.status = 'pending' THEN '⏳ 待审核'
        WHEN mc.status = 'submitted' THEN '📝 已提交'
        ELSE '未提交'
    END AS 中期检查状态,
    CASE 
        WHEN fc.is_final = 1 THEN '🎯 已定稿'
        WHEN fc.status = 'approved' THEN '✅ 已通过'
        WHEN fc.status = 'pending' THEN '⏳ 待审核'
        ELSE '未提交'
    END AS 最终检查状态,
    CASE 
        WHEN d.status = 'approved' THEN '✅ 已完成'
        WHEN d.status = 'pending' THEN '⏳ 待答辩'
        ELSE '未开始'
    END AS 答辩状态,
    fs.total_score AS 总成绩,
    fs.grade_level AS 等级
FROM users u
LEFT JOIN majors m ON u.major_id = m.major_id
LEFT JOIN teacher_student_relations tsr ON u.user_id = tsr.student_id
LEFT JOIN project_batches pb ON tsr.batch_id = pb.batch_id
LEFT JOIN student_selections ss ON u.user_id = ss.student_id AND tsr.batch_id = ss.batch_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN task_books tb ON ss.selection_id = tb.selection_id
LEFT JOIN midterm_checks mc ON ss.selection_id = mc.selection_id AND mc.is_current = 1
LEFT JOIN final_checks fc ON ss.selection_id = fc.selection_id AND fc.is_current = 1
LEFT JOIN defenses d ON ss.selection_id = d.selection_id
LEFT JOIN final_scores fs ON ss.selection_id = fs.selection_id
WHERE u.role = 'student'
  AND tsr.batch_id IS NOT NULL
ORDER BY u.username
LIMIT 50;

-- ============================================================
-- 9️⃣ 统计查询：各班级学生完成情况（含学号统计）
-- ============================================================
SELECT 
    u.class_name AS 班级,
    COUNT(DISTINCT u.user_id) AS 总人数,
    COUNT(DISTINCT CASE WHEN ss.status = 'approved' THEN u.user_id END) AS 已选题,
    COUNT(DISTINCT CASE WHEN tb.status = 'confirmed' THEN u.user_id END) AS 已确认任务书,
    COUNT(DISTINCT CASE WHEN mc.status = 'approved' THEN u.user_id END) AS 中期通过,
    COUNT(DISTINCT CASE WHEN fc.is_final = 1 THEN u.user_id END) AS 最终定稿,
    COUNT(DISTINCT CASE WHEN d.status = 'approved' THEN u.user_id END) AS 答辩完成,
    COUNT(DISTINCT CASE WHEN fs.is_published = 1 THEN u.user_id END) AS 成绩已公布
FROM users u
LEFT JOIN teacher_student_relations tsr ON u.user_id = tsr.student_id
LEFT JOIN student_selections ss ON u.user_id = ss.student_id AND tsr.batch_id = ss.batch_id
LEFT JOIN task_books tb ON ss.selection_id = tb.selection_id
LEFT JOIN midterm_checks mc ON ss.selection_id = mc.selection_id
LEFT JOIN final_checks fc ON ss.selection_id = fc.selection_id AND fc.is_current = 1
LEFT JOIN defenses d ON ss.selection_id = d.selection_id
LEFT JOIN final_scores fs ON ss.selection_id = fs.selection_id
WHERE u.role = 'student'
  AND tsr.batch_id = 1
GROUP BY u.class_name
ORDER BY u.class_name;

-- ============================================================
-- 🔟 高级查询：根据学号查询单个学生的所有信息
-- ============================================================
-- 将下面的 '202358210001' 替换为要查询的学号
SET @target_student_no = '202358210001';

SELECT 
    '基本信息' AS 信息类型,
    u.username AS 学号,
    u.real_name AS 姓名,
    u.class_name AS 班级,
    m.major_name AS 专业,
    c.college_name AS 学院,
    u.phone AS 联系电话,
    u.email AS 邮箱
FROM users u
LEFT JOIN majors m ON u.major_id = m.major_id
LEFT JOIN colleges c ON u.college_id = c.college_id
WHERE u.username = @target_student_no

UNION ALL

SELECT 
    '选题信息',
    u.username,
    t.topic_name,
    ss.status,
    CAST(ss.version AS CHAR),
    ur.real_name,
    CAST(ss.review_time AS CHAR),
    NULL, NULL
FROM student_selections ss
INNER JOIN users u ON ss.student_id = u.user_id
LEFT JOIN topics t ON ss.topic_id = t.topic_id
LEFT JOIN users ur ON ss.reviewer_id = ur.user_id
WHERE u.username = @target_student_no

UNION ALL

SELECT 
    '任务书信息',
    u.username,
    tb.status,
    tb.content,
    tb.deadline,
    tb.issuer_id,
    tb.issued_at,
    NULL, NULL
FROM task_books tb
INNER JOIN student_selections ss ON tb.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
WHERE u.username = @target_student_no

UNION ALL

SELECT 
    '中期检查',
    u.username,
    mc.status,
    CAST(mc.progress AS CHAR),
    mc.submit_time,
    mc.review_comment,
    mc.review_time,
    NULL, NULL
FROM midterm_checks mc
INNER JOIN student_selections ss ON mc.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
WHERE u.username = @target_student_no AND mc.is_current = 1

UNION ALL

SELECT 
    '最终检查',
    u.username,
    fc.status,
    CAST(fc.report_score AS CHAR),
    fc.submit_time,
    fc.review_comment,
    fc.finalized_at,
    NULL, NULL
FROM final_checks fc
INNER JOIN student_selections ss ON fc.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
WHERE u.username = @target_student_no AND fc.is_current = 1

UNION ALL

SELECT 
    '答辩信息',
    u.username,
    d.status,
    d.defense_score,
    CAST(d.defense_score_num AS CHAR),
    d.defense_datetime,
    d.location,
    NULL, NULL
FROM defenses d
INNER JOIN student_selections ss ON d.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
WHERE u.username = @target_student_no

UNION ALL

SELECT 
    '成绩信息',
    u.username,
    fs.grade_level,
    CAST(fs.total_score AS CHAR),
    CAST(fs.report_score AS CHAR),
    CAST(fs.defense_score AS CHAR),
    fs.published_at,
    NULL, NULL
FROM final_scores fs
INNER JOIN student_selections ss ON fs.selection_id = ss.selection_id
INNER JOIN users u ON ss.student_id = u.user_id
WHERE u.username = @target_student_no;