-- =============================================
-- 补充学生选题数据 - 修复"未选题目"问题
-- 执行时间: 2026-06-08
-- 说明: 为张三、李四等学生补充使用正确batch_id的选题记录
-- =============================================

-- 先查看哪些学生缺少当前批次的选题记录
-- SELECT u.user_id, u.real_name, u.username 
-- FROM users u 
-- WHERE u.role = 'student' 
-- AND u.user_id NOT IN (
--     SELECT ss.student_id 
--     FROM student_selections ss 
--     WHERE ss.batch_id = 31  -- 替换为实际的当前批次ID
-- );

-- 为张三 (user_id=8) 补充选题记录（使用batch_id=31）
INSERT INTO `student_selections` (`batch_id`, `student_id`, `topic_id`, `topic_type`, `self_topic_name`, `self_topic_desc`, `version`, `previous_selection_id`, `status`, `reviewer_id`, `review_time`, `review_comment`, `created_at`, `updated_at`) VALUES
(31, 8, 1, 'library', '', '', 1, 0, 'approved', 4, '2026-06-08 10:00:00', '选题符合要求，已通过审核', NOW(), NOW());

-- 为李四 (user_id=9) 补充选题记录（使用batch_id=32）
INSERT INTO `student_selections` (`batch_id`, `student_id`, `topic_id`, `topic_type`, `self_topic_name`, `self_topic_desc`, `version`, `previous_selection_id`, `status`, `reviewer_id`, `review_time`, `review_comment`, `created_at`, `updated_at`) VALUES
(32, 9, 2, 'library', '', '', 1, 0, 'approved', 4, '2026-06-08 10:30:00', '选题难度适中，适合该生水平，已通过', NOW(), NOW());

-- 如果还有其他学生也需要补充，请参考以下模板继续添加：
-- INSERT INTO `student_selections` (`batch_id`, `student_id`, `topic_id`, ...) VALUES
-- (batch_id, student_id, topic_id, ...);

-- 验证数据是否插入成功
-- SELECT 
--     u.real_name as 学生,
--     u.username as 学号,
--     t.topic_name as 题目,
--     ss.status as 状态,
--     ss.batch_id as 批次
-- FROM student_selections ss
-- INNER JOIN users u ON ss.student_id = u.user_id
-- INNER JOIN topics t ON ss.topic_id = t.topic_id
-- WHERE u.user_id IN (8, 9)
-- ORDER BY u.username ASC;
