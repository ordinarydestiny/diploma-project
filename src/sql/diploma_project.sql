/*
 Navicat Premium Dump SQL

 Source Server         : localhost_3306
 Source Server Type    : MySQL
 Source Server Version : 80200 (8.2.0)
 Source Host           : localhost:3306
 Source Schema         : diploma_project

 Target Server Type    : MySQL
 Target Server Version : 80200 (8.2.0)
 File Encoding         : 65001

 Date: 02/06/2026 13:35:12
*/

SET NAMES utf8mb4;
SET FOREIGN_KEY_CHECKS = 0;

-- ----------------------------
-- Table structure for colleges
-- ----------------------------
DROP TABLE IF EXISTS `colleges`;
CREATE TABLE `colleges`  (
  `college_id` int NOT NULL AUTO_INCREMENT COMMENT '学院唯一ID',
  `college_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '学院名称',
  `description` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '学院简介（可选）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '创建时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后更新时间',
  PRIMARY KEY (`college_id`) USING BTREE,
  UNIQUE INDEX `uk_college_name`(`college_name` ASC) USING BTREE COMMENT '学院名称唯一约束'
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学院表' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of colleges
-- ----------------------------
INSERT INTO `colleges` VALUES (1, '信息工程学院', '计算机、软件等相关专业', '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `colleges` VALUES (2, '智能制造学院', '机械、自动化等相关专业', '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for defenses
-- ----------------------------
DROP TABLE IF EXISTS `defenses`;
CREATE TABLE `defenses`  (
  `defense_id` int NOT NULL AUTO_INCREMENT COMMENT '答辩记录唯一ID',
  `selection_id` int NOT NULL COMMENT '关联的学生选题ID（一个选题只有一条最终答辩记录）',
  `defense_score` enum('excellent','good','medium','pass','fail') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '答辩成绩（五级制）：\r\n        - excellent: 优秀（≥90分）\r\n        - good: 良好（80-89分）\r\n        - medium: 中等（70-79分）\r\n        - pass: 及格（60-69分）\r\n        - fail: 不及格（<60分）',
  `defense_score_num` decimal(5, 2) NULL DEFAULT NULL COMMENT '【增强】数值分数（如95/85/75/65/50），\r\n        来源：1) 从defense_score根据system_configs映射转换；2) 教师直接录入数值。\r\n        此字段参与总成绩计算：total_score = report_score * report_ratio + defense_score_num * defense_ratio',
  `record_file_id` int NOT NULL COMMENT '答辩记录单页PDF文件ID（强制要求单页，超过则拒绝提交）',
  `submitter_type` enum('student','teacher') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'student' COMMENT '提交人类型：学生自行提交 OR 教师代录',
  `submitter_id` int NOT NULL COMMENT '提交人用户ID',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '审核状态',
  `reviewer_id` int NULL DEFAULT NULL COMMENT '审核人ID（指导老师）',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_comment` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '审核意见（如修改成绩说明）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`defense_id`) USING BTREE,
  UNIQUE INDEX `uk_defense_selection`(`selection_id` ASC) USING BTREE COMMENT '核心约束：一个选题只有一条最终答辩记录',
  INDEX `fk_def_file`(`record_file_id` ASC) USING BTREE COMMENT '按文件查询',
  INDEX `fk_def_submitter`(`submitter_id` ASC) USING BTREE COMMENT '按提交人查询',
  INDEX `fk_def_reviewer`(`reviewer_id` ASC) USING BTREE COMMENT '按审核人查询',
  INDEX `idx_defense_score`(`defense_score` ASC) USING BTREE COMMENT '按五级制筛选统计',
  INDEX `idx_defense_score_num`(`defense_score_num` ASC) USING BTREE COMMENT '【增强】按数值分数排序',
  CONSTRAINT `fk_def_file` FOREIGN KEY (`record_file_id`) REFERENCES `files` (`file_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_def_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_def_selection` FOREIGN KEY (`selection_id`) REFERENCES `student_selections` (`selection_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_def_submitter` FOREIGN KEY (`submitter_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '答辩记录表【增强版】（含五级制+数值双字段，支持灵活的成绩计算）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of defenses
-- ----------------------------
INSERT INTO `defenses` VALUES (1, 1, 'good', 85.00, 6, 'student', 5, '2026-06-01 10:00:00', 'approved', NULL, NULL, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for files
-- ----------------------------
DROP TABLE IF EXISTS `files`;
CREATE TABLE `files`  (
  `file_id` int NOT NULL AUTO_INCREMENT COMMENT '文件唯一ID',
  `original_name` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '原始文件名（保留用户上传时的文件名）',
  `storage_path` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '服务器存储路径或OSS URL',
  `file_size` bigint NOT NULL COMMENT '文件大小（字节）',
  `mime_type` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'MIME类型（application/pdf, image/png等）',
  `file_hash` varchar(64) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '文件SHA256哈希值（用于去重和完整性校验）',
  `uploader_id` int NOT NULL COMMENT '上传者用户ID',
  `upload_time` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '上传时间',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '上传时客户端IP',
  `relation_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '业务类型枚举：\r\n        - task_book: 任务书附件\r\n        - midterm_report: 中期报告PDF\r\n        - final_report: 最终报告PDF\r\n        - guide_attachment: 教师批阅附件\r\n        - defense_record: 答辩记录单页PDF\r\n        - signature: 手写签名图片\r\n        - other: 其他文件',
  `relation_id` int NULL DEFAULT NULL COMMENT '关联的业务记录ID',
  `is_deleted` tinyint(1) NOT NULL DEFAULT 0 COMMENT '逻辑删除标记（0=正常，1=已删除）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`file_id`) USING BTREE,
  INDEX `fk_file_uploader`(`uploader_id` ASC) USING BTREE COMMENT '按上传者查询',
  INDEX `idx_relation`(`relation_type` ASC, `relation_id` ASC) USING BTREE COMMENT '按业务类型+ID快速查找',
  INDEX `idx_file_upload_time`(`upload_time` ASC) USING BTREE COMMENT '按上传时间排序',
  INDEX `idx_file_mime`(`mime_type` ASC) USING BTREE COMMENT '按文件类型筛选',
  CONSTRAINT `fk_file_uploader` FOREIGN KEY (`uploader_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '统一文件管理表（支持业务关联、去重校验、逻辑删除）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of files
-- ----------------------------
INSERT INTO `files` VALUES (1, '小明中期报告.pdf', '/uploads/2026/midterm_1.pdf', 204800, 'application/pdf', 'abc123...', 5, '2026-06-02 10:28:48', '127.0.0.1', 'midterm_report', 1, 0, '2026-06-02 10:28:48');
INSERT INTO `files` VALUES (2, '小红中期报告.pdf', '/uploads/2026/midterm_2.pdf', 180000, 'application/pdf', 'def456...', 6, '2026-06-02 10:28:48', '127.0.0.1', 'midterm_report', 2, 0, '2026-06-02 10:28:48');
INSERT INTO `files` VALUES (3, '小刚中期报告.pdf', '/uploads/2026/midterm_3.pdf', 220000, 'application/pdf', 'ghi789...', 7, '2026-06-02 10:28:48', '127.0.0.1', 'midterm_report', 3, 0, '2026-06-02 10:28:48');
INSERT INTO `files` VALUES (4, '小明最终报告.pdf', '/uploads/2026/final_1.pdf', 512000, 'application/pdf', 'xyz012...', 5, '2026-06-02 10:28:48', '127.0.0.1', 'final_report', 1, 0, '2026-06-02 10:28:48');
INSERT INTO `files` VALUES (5, '王指导批阅意见.pdf', '/uploads/2026/guide_fb_1.pdf', 102400, 'application/pdf', 'jkl345...', 3, '2026-06-02 10:28:48', '127.0.0.1', 'guide_attachment', 1, 0, '2026-06-02 10:28:48');
INSERT INTO `files` VALUES (6, '小明答辩记录表.pdf', '/uploads/2026/defense_1.pdf', 80000, 'application/pdf', 'mno678...', 5, '2026-06-02 10:28:48', '127.0.0.1', 'defense_record', 1, 0, '2026-06-02 10:28:48');
INSERT INTO `files` VALUES (7, '李专业签名.png', '/uploads/signatures/2.png', 15360, 'image/png', 'pqr901...', 2, '2026-06-02 10:28:48', '127.0.0.1', 'signature', 2, 0, '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for final_checks
-- ----------------------------
DROP TABLE IF EXISTS `final_checks`;
CREATE TABLE `final_checks`  (
  `check_id` int NOT NULL AUTO_INCREMENT COMMENT '检查记录唯一ID',
  `selection_id` int NOT NULL COMMENT '关联的学生选题ID',
  `file_id` int NOT NULL COMMENT '学生提交的最终报告PDF文件ID',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '提交时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '提交版本号（显式追踪）',
  `is_current` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否当前版本（1=最新, 0=历史）',
  `is_final` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否为最终定稿版本（0=否, 1=是且不可变更），\r\n        触发条件：教师首次审批通过(status=approved)且该选题尚无其他定稿记录时自动设置为1。\r\n        一旦为1，后续任何操作都不会改变此标记，确保档案权威性。',
  `finalized_at` datetime NULL DEFAULT NULL COMMENT '【新增】定稿时间（is_final变为1时记录，用于审计和争议处理）',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '审核状态',
  `reviewer_id` int NULL DEFAULT NULL COMMENT '审核老师ID',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '教师评语（必填，作为档案留存）',
  `report_score` int NULL DEFAULT NULL COMMENT '报告分数（0-100，必填，用于计算总成绩）',
  `guide_file_id` int NULL DEFAULT NULL COMMENT '教师批阅附件ID',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`check_id`) USING BTREE,
  UNIQUE INDEX `uk_selection_version`(`selection_id` ASC, `version` ASC) USING BTREE COMMENT '【增强】版本唯一约束',
  INDEX `fk_fc_selection`(`selection_id` ASC) USING BTREE COMMENT '按选题查询',
  INDEX `idx_fc_current`(`selection_id` ASC, `is_current` ASC) USING BTREE COMMENT '快速找最新版本',
  INDEX `idx_fc_final`(`selection_id` ASC, `is_final` ASC) USING BTREE COMMENT '【重要】快速判断是否已定稿',
  INDEX `fk_fc_file`(`file_id` ASC) USING BTREE,
  INDEX `fk_fc_guide_file`(`guide_file_id` ASC) USING BTREE,
  INDEX `idx_fc_report_score`(`report_score` ASC) USING BTREE COMMENT '按分数排序（用于统计分析）',
  INDEX `fc_fk_reviewer`(`reviewer_id` ASC) USING BTREE,
  CONSTRAINT `fc_fk_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_fc_file` FOREIGN KEY (`file_id`) REFERENCES `files` (`file_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_fc_guide_file` FOREIGN KEY (`guide_file_id`) REFERENCES `files` (`file_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_fc_selection` FOREIGN KEY (`selection_id`) REFERENCES `student_selections` (`selection_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '最终检查表【增强版】（含version + 定稿锁定 + 定稿时间戳）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of final_checks
-- ----------------------------
INSERT INTO `final_checks` VALUES (1, 1, 4, '2026-05-20 16:00:00', 1, 1, 1, '2026-05-22 09:00:00', 'approved', 3, '2026-05-22 09:00:00', '功能完整，代码规范，论文结构清晰，达到了毕业设计要求。优秀！', 88, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for final_scores
-- ----------------------------
DROP TABLE IF EXISTS `final_scores`;
CREATE TABLE `final_scores`  (
  `score_id` int NOT NULL AUTO_INCREMENT COMMENT '成绩记录唯一ID',
  `selection_id` int NOT NULL COMMENT '关联的学生选题ID（外键）',
  `report_score` int NULL DEFAULT NULL COMMENT '报告分数（来自最终检查定稿版本的report_score，0-100）',
  `defense_score` int NULL DEFAULT NULL COMMENT '答辩数值分数（来自defenses.defense_score_num，0-100）',
  `total_score` decimal(5, 2) NULL DEFAULT NULL COMMENT '总成绩（加权计算结果，保留2位小数）',
  `grade_level` enum('A','B','C','D','F') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '等级评定：\r\n        - A: 优秀（≥90）\r\n        - B: 良好（80-89）\r\n        - C: 中等（70-79）\r\n        - D: 及格（60-69）\r\n        - F: 不及格（<60）',
  `is_published` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否向学生公布（0=隐藏/仅管理员可见, 1=已公布/学生可查看）',
  `published_by` int NULL DEFAULT NULL COMMENT '公布操作人ID',
  `published_at` datetime NULL DEFAULT NULL COMMENT '公布时间',
  `calculated_at` datetime NULL DEFAULT NULL COMMENT '最近一次计算时间',
  `calculation_mode` enum('auto','manual') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'auto' COMMENT '计算方式：auto=自动触发, manual=管理员手动',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`score_id`) USING BTREE,
  UNIQUE INDEX `uk_score_selection`(`selection_id` ASC) USING BTREE COMMENT '核心约束：一个选题只有一条成绩记录',
  INDEX `idx_total_score`(`total_score` ASC) USING BTREE COMMENT '按总分排序（用于排名）',
  INDEX `idx_grade_level`(`grade_level` ASC) USING BTREE COMMENT '按等级统计分布',
  INDEX `idx_published`(`is_published` ASC) USING BTREE COMMENT '筛选已公布/未公布',
  INDEX `fk_score_published_by`(`published_by` ASC) USING BTREE,
  CONSTRAINT `fk_score_published_by` FOREIGN KEY (`published_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_score_selection` FOREIGN KEY (`selection_id`) REFERENCES `student_selections` (`selection_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 2 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '成绩汇总表（自动计算、等级评定、公布控制）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of final_scores
-- ----------------------------
INSERT INTO `final_scores` VALUES (1, 1, 88, 85, 86.80, 'B', 1, 1, '2026-06-02 10:28:48', '2026-06-02 10:28:48', 'auto', '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for majors
-- ----------------------------
DROP TABLE IF EXISTS `majors`;
CREATE TABLE `majors`  (
  `major_id` int NOT NULL AUTO_INCREMENT COMMENT '专业唯一ID',
  `college_id` int NOT NULL COMMENT '所属学院ID（外键→colleges）',
  `major_name` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '专业名称',
  `major_code` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '专业编码（如CS、SE等）',
  `duration` tinyint NULL DEFAULT 4 COMMENT '学制年限（默认4年）',
  `degree_type` enum('bachelor','master','doctor') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'bachelor' COMMENT '学位类型：本科/硕士/博士',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`major_id`) USING BTREE,
  INDEX `fk_major_college`(`college_id` ASC) USING BTREE COMMENT '学院外键索引（加速按学院查询专业）',
  CONSTRAINT `fk_major_college` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`college_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '专业表（含学制、学位类型等扩展信息）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of majors
-- ----------------------------
INSERT INTO `majors` VALUES (1, 1, '计算机科学与技术', 'CS', 4, 'bachelor', '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `majors` VALUES (2, 1, '软件工程', 'SE', 4, 'bachelor', '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `majors` VALUES (3, 2, '机械设计制造及其自动化', 'ME', 4, 'bachelor', '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `majors` VALUES (4, 2, '人工智能', 'AI', 4, 'bachelor', '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for midterm_checks
-- ----------------------------
DROP TABLE IF EXISTS `midterm_checks`;
CREATE TABLE `midterm_checks`  (
  `check_id` int NOT NULL AUTO_INCREMENT COMMENT '检查记录唯一ID',
  `selection_id` int NOT NULL COMMENT '关联的学生选题ID',
  `file_id` int NOT NULL COMMENT '学生提交的中期报告PDF文件ID（外键→files）',
  `submit_time` datetime NOT NULL DEFAULT CURRENT_TIMESTAMP COMMENT '学生提交时间',
  `version` int NOT NULL DEFAULT 1 COMMENT '提交版本号（首次=1，后续递增，显式追踪修改历史）',
  `is_current` tinyint(1) NOT NULL DEFAULT 1 COMMENT '是否为当前最新版本（1=是, 0=历史版本）',
  `status` enum('pending','approved','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '审核状态',
  `reviewer_id` int NULL DEFAULT NULL COMMENT '审核老师ID',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '教师审核意见（会反馈给学生）',
  `guide_file_id` int NULL DEFAULT NULL COMMENT '教师批阅后上传的附件（如带批注的PDF）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`check_id`) USING BTREE,
  UNIQUE INDEX `uk_selection_version`(`selection_id` ASC, `version` ASC) USING BTREE COMMENT '【增强】同一选题每版本唯一',
  INDEX `fk_mc_selection`(`selection_id` ASC) USING BTREE COMMENT '按选题查询所有中期报告',
  INDEX `idx_mc_current`(`selection_id` ASC, `is_current` ASC) USING BTREE COMMENT '快速定位最新版本',
  INDEX `fk_mc_file`(`file_id` ASC) USING BTREE COMMENT '按文件反查',
  INDEX `fk_mc_guide_file`(`guide_file_id` ASC) USING BTREE COMMENT '按批阅附件查询',
  INDEX `idx_mc_status`(`status` ASC) USING BTREE COMMENT '按状态筛选待办',
  INDEX `fk_mc_reviewer`(`reviewer_id` ASC) USING BTREE,
  CONSTRAINT `fk_mc_file` FOREIGN KEY (`file_id`) REFERENCES `files` (`file_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_mc_guide_file` FOREIGN KEY (`guide_file_id`) REFERENCES `files` (`file_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_mc_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_mc_selection` FOREIGN KEY (`selection_id`) REFERENCES `student_selections` (`selection_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '中期检查表【增强版】（含显式version字段，支持清晰的版本追踪）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of midterm_checks
-- ----------------------------
INSERT INTO `midterm_checks` VALUES (1, 1, 1, '2026-04-10 14:00:00', 1, 1, 'approved', 3, '2026-04-12 09:00:00', '进度正常，已完成需求分析和原型设计，请继续按计划推进。', NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `midterm_checks` VALUES (2, 2, 2, '2026-04-10 15:00:00', 1, 1, 'approved', 3, '2026-04-12 10:00:00', '通过，但请注意文档格式规范，图表编号要连续。', 5, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `midterm_checks` VALUES (3, 3, 3, '2026-04-11 10:00:00', 1, 1, 'pending', 4, NULL, NULL, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for notifications
-- ----------------------------
DROP TABLE IF EXISTS `notifications`;
CREATE TABLE `notifications`  (
  `notif_id` int NOT NULL AUTO_INCREMENT COMMENT '通知唯一ID',
  `receiver_id` int NOT NULL COMMENT '接收者用户ID（通知的目标用户）',
  `title` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '通知标题（简短明了，如\"选题审核通过\"）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '通知详细内容（可包含链接、操作按钮等）',
  `is_read` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否已读（0=未读, 1=已读）',
  `read_at` datetime NULL DEFAULT NULL COMMENT '阅读时间',
  `related_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '关联业务类型（selection/taskbook/midterm_check/final_check/defense/score）',
  `related_id` int NULL DEFAULT NULL COMMENT '关联业务记录ID',
  `priority` enum('low','normal','high','urgent') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '优先级：低/普通/高/紧急',
  `category` enum('system','business','reminder') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'business' COMMENT '分类：系统通知/业务通知/提醒',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '发送时间',
  PRIMARY KEY (`notif_id`) USING BTREE,
  INDEX `fk_notif_receiver`(`receiver_id` ASC) USING BTREE COMMENT '按接收者查询通知列表',
  INDEX `idx_notif_unread`(`receiver_id` ASC, `is_read` ASC) USING BTREE COMMENT '快速统计未读数',
  INDEX `idx_notif_related`(`related_type` ASC, `related_id` ASC) USING BTREE COMMENT '按业务对象查询通知',
  INDEX `idx_notif_created`(`created_at` ASC) USING BTREE COMMENT '按时间排序（最新的在前）',
  CONSTRAINT `fk_notif_receiver` FOREIGN KEY (`receiver_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '消息通知表（支持优先级、分类、业务关联跳转）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of notifications
-- ----------------------------
INSERT INTO `notifications` VALUES (1, 5, '选题审核通过', '你的选题\"基于Spring Boot的顶岗实习管理系统\"已审核通过，请等待导师下达任务书。\n\n[查看详情]', 0, NULL, 'selection', 1, 'normal', 'business', '2026-06-02 10:28:48');
INSERT INTO `notifications` VALUES (2, 3, '新选题待审核', '学生小红提交了自主命题选题\"基于微服务的校园一卡通系统\"，请及时登录系统进行审核。\n\n[立即审核]', 0, NULL, 'selection', 2, 'high', 'business', '2026-06-02 10:28:48');
INSERT INTO `notifications` VALUES (3, 6, '任务书已下达', '你的任务书已由王指导老师下达，请登录系统查看详细要求，并按要求推进毕设进度。\n\n[查看任务书]', 0, NULL, 'task_book', 2, 'normal', 'business', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for operation_logs
-- ----------------------------
DROP TABLE IF EXISTS `operation_logs`;
CREATE TABLE `operation_logs`  (
  `log_id` int NOT NULL AUTO_INCREMENT COMMENT '日志唯一ID',
  `user_id` int NOT NULL COMMENT '操作人用户ID',
  `user_role` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作时的角色身份（因为支持多角色切换）',
  `operation` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '操作类型（如 approve_selection, reject_taskbook, reset_topic, publish_score 等）',
  `target_type` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作对象类型（selection/taskbook/midterm_check/final_check/defense/batch/user）',
  `target_id` int NULL DEFAULT NULL COMMENT '操作对象的记录ID',
  `detail` json NULL COMMENT '操作详细信息（JSON对象，可包含：\r\n        {\r\n            \"before\": { \"status\": \"pending\", ... },  // 操作前的值\r\n            \"after\": { \"status\": \"approved\", ... },   // 操作后的值\r\n            \"changes\": [ \"status: pending→approved\" ], // 变更摘要\r\n            \"reason\": \"符合要求\"                       // 操作原因\r\n        }',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '操作时的客户端IP地址（支持IPv6）',
  `request_url` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '请求的API路径（如 POST /api/selections/1/review）',
  `request_method` varchar(10) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT 'HTTP方法（GET/POST/PUT/DELETE）',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器/客户端标识',
  `result` enum('success','failure','error') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'success' COMMENT '操作结果',
  `error_message` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '错误信息（失败时记录异常堆栈摘要）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '操作时间（精确到秒）',
  PRIMARY KEY (`log_id`) USING BTREE,
  INDEX `fk_log_user`(`user_id` ASC) USING BTREE COMMENT '按操作人查询',
  INDEX `idx_log_operation`(`operation` ASC) USING BTREE COMMENT '按操作类型筛选（如查所有reset操作）',
  INDEX `idx_log_target`(`target_type` ASC, `target_id` ASC) USING BTREE COMMENT '按操作对象查询（如某选题的所有操作历史）',
  INDEX `idx_log_ip`(`ip_address` ASC) USING BTREE COMMENT '按IP查询（安全审计）',
  INDEX `idx_log_time`(`created_at` ASC) USING BTREE COMMENT '按时间排序（最新的在前）',
  INDEX `idx_log_result`(`result` ASC) USING BTREE COMMENT '按结果筛选（查所有失败操作）',
  CONSTRAINT `fk_log_user` FOREIGN KEY (`user_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '操作日志表（AOP自动记录，JSON格式存储变更详情，支持安全审计）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of operation_logs
-- ----------------------------
INSERT INTO `operation_logs` VALUES (1, 3, 'teacher', 'approve_selection', 'selection', 1, '{\"after\": {\"status\": \"approved\"}, \"before\": {\"status\": \"pending\"}, \"comment\": \"选题符合专业方向\"}', '10.0.1.25', '/api/selections/1/review', 'PUT', 'Mozilla/5.0...', 'success', NULL, '2026-06-02 10:28:48');
INSERT INTO `operation_logs` VALUES (2, 3, 'teacher', 'issue_taskbook', 'task_book', 1, '{\"version\": 1, \"selection_id\": 1, \"content_length\": 156}', '10.0.1.25', '/api/taskbooks', 'POST', 'Mozilla/5.0...', 'success', NULL, '2026-06-02 10:28:48');
INSERT INTO `operation_logs` VALUES (3, 2, 'major_admin', 'create_batch', 'batch', 1, '{\"major_id\": 1, \"batch_name\": \"2026届计算机科学与技术毕业设计\", \"defense_ratio\": 0.4}', '10.0.1.10', '/api/batches', 'POST', 'Mozilla/5.0...', 'success', NULL, '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for project_batches
-- ----------------------------
DROP TABLE IF EXISTS `project_batches`;
CREATE TABLE `project_batches`  (
  `batch_id` int NOT NULL AUTO_INCREMENT COMMENT '批次唯一ID',
  `major_id` int NOT NULL COMMENT '所属专业ID（外键→majors，一个批次对应一个专业）',
  `batch_name` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '批次名称（如\"2026届计算机科学与技术毕业设计\"）',
  `batch_code` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '批次编码（如\"2026-CS-BATCH\"，可选，用于外部系统对接）',
  `start_date` date NOT NULL COMMENT '毕设开始日期（学生可开始选题）',
  `end_date` date NOT NULL COMMENT '毕设结束日期（答辩截止）',
  `defense_ratio` decimal(3, 2) NOT NULL DEFAULT 0.40 COMMENT '答辩成绩占总成绩比例（如0.40表示40%）',
  `report_ratio` decimal(3, 2) NOT NULL DEFAULT 0.60 COMMENT '报告成绩占总成绩比例（如0.60表示60%）',
  `status` enum('draft','active','finished') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'draft' COMMENT '批次状态：\r\n        - draft: 草稿（管理员编辑中，学生不可见）\r\n        - active: 进行中（学生可操作，核心业务阶段）\r\n        - finished: 已结束（数据归档，只读）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '批次说明（富文本HTML或纯文本）',
  `creator_id` int NOT NULL COMMENT '创建人用户ID（院级/专业管理员）',
  `activated_at` datetime NULL DEFAULT NULL COMMENT '激活时间（从draft变为active时记录）',
  `finished_at` datetime NULL DEFAULT NULL COMMENT '结束时间（从active变为finished时记录）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`batch_id`) USING BTREE,
  UNIQUE INDEX `uk_batch_code`(`batch_code` ASC) USING BTREE COMMENT '批次编码唯一（如果使用）',
  INDEX `fk_batch_major`(`major_id` ASC) USING BTREE COMMENT '按专业查询批次',
  INDEX `fk_batch_creator`(`creator_id` ASC) USING BTREE COMMENT '按创建人查询',
  INDEX `idx_batch_status`(`status` ASC) USING BTREE COMMENT '按状态筛选批次',
  INDEX `idx_batch_date_range`(`start_date` ASC, `end_date` ASC) USING BTREE COMMENT '按时间范围查询',
  CONSTRAINT `fk_batch_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_batch_major` FOREIGN KEY (`major_id`) REFERENCES `majors` (`major_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 3 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '毕设批次表（含状态机、权重配置、时间审计）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of project_batches
-- ----------------------------
INSERT INTO `project_batches` VALUES (1, 1, '2026届计算机科学与技术毕业设计', '2026-CS-BT', '2026-03-01', '2026-06-30', 0.40, 0.60, 'active', '计算机科学与技术专业2026年度本科毕业设计', 2, '2026-03-01 00:00:00', NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `project_batches` VALUES (2, 2, '2026届软件工程毕业设计', '2026-SE-BT', '2026-03-01', '2026-06-30', 0.40, 0.60, 'active', '软件工程专业2026年度本科毕业设计', 2, '2026-03-01 00:00:00', NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for roles
-- ----------------------------
DROP TABLE IF EXISTS `roles`;
CREATE TABLE `roles`  (
  `role_id` int NOT NULL AUTO_INCREMENT COMMENT '角色唯一ID',
  `role_code` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色编码（如 student, teacher, college_admin）',
  `role_name` varchar(30) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '角色显示名称',
  `permissions` json NULL COMMENT '权限列表（JSON数组格式）',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '角色描述',
  `sort_order` int NULL DEFAULT 0 COMMENT '排序号（用于前端展示顺序）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`role_id`) USING BTREE,
  UNIQUE INDEX `uk_role_code`(`role_code` ASC) USING BTREE COMMENT '角色编码唯一'
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '角色权限表（支持细粒度权限控制）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of roles
-- ----------------------------
INSERT INTO `roles` VALUES (1, 'college_admin', '院级管理员', '[\"batch:manage\", \"progress:view\", \"sign:export\", \"reset:topic\", \"reset:taskbook\", \"user:manage\"]', '拥有全院最高权限，可创建批次、重置状态、导出数据', 1, '2026-06-02 10:28:48');
INSERT INTO `roles` VALUES (2, 'major_admin', '专业负责人', '[\"topic:manage\", \"batch:manage\", \"relation:adjust\", \"progress:view\", \"topic:include\"]', '负责本专业的题库维护、师生分配、进度监控', 2, '2026-06-02 10:28:48');
INSERT INTO `roles` VALUES (3, 'teacher', '指导教师', '[\"topic:create_own\", \"selection:review\", \"taskbook:issue\", \"report:review\", \"defense:score\", \"sign:view_own\"]', '指导学生完成毕设全过程，审核报告、录入成绩', 3, '2026-06-02 10:28:48');
INSERT INTO `roles` VALUES (4, 'student', 'student', '[\"selection:submit\", \"report:upload\", \"sign:checkin\", \"defense:view\"]', '提交选题、报告、签到，查看自己的成绩和通知', 4, '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for sign_ins
-- ----------------------------
DROP TABLE IF EXISTS `sign_ins`;
CREATE TABLE `sign_ins`  (
  `sign_id` int NOT NULL AUTO_INCREMENT COMMENT '签到记录唯一ID',
  `student_id` int NOT NULL COMMENT '签到学生用户ID',
  `batch_id` int NOT NULL COMMENT '所属批次ID',
  `sign_date` date NOT NULL COMMENT '签到日期（YYYY-MM-DD）',
  `sign_time` time NOT NULL COMMENT '签到具体时间（HH:MM:SS）',
  `daily_report` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '日报/工作日志内容（今天完成了什么、明天计划、遇到的问题）',
  `is_makeup` tinyint(1) NOT NULL DEFAULT 0 COMMENT '是否补签（0=当天正常签到, 1=事后补签）',
  `makeup_reason` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '补签原因（如\"网络故障\"、\"忘记签到\"）',
  `sign_status` enum('normal','late','absent') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'normal' COMMENT '签到状态：\r\n        - normal: 正常（按时签到）\r\n        - late: 迟到（超出规定时间范围）\r\n        - absent: 缺勤（系统自动标记或管理员手动设置）',
  `ip_address` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '签到时的客户端IP地址',
  `user_agent` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '浏览器/设备信息',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  PRIMARY KEY (`sign_id`) USING BTREE,
  UNIQUE INDEX `uk_sign_student_date`(`student_id` ASC, `batch_id` ASC, `sign_date` ASC) USING BTREE COMMENT '核心约束：同学生同批次同天只能一条',
  INDEX `fk_sign_batch`(`batch_id` ASC) USING BTREE COMMENT '按批次查询所有签到',
  INDEX `fk_sign_student`(`student_id` ASC) USING BTREE COMMENT '按学生查询签到历史',
  INDEX `idx_sign_date`(`sign_date` ASC) USING BTREE COMMENT '按日期范围查询',
  INDEX `idx_sign_makeup`(`is_makeup` ASC) USING BTREE COMMENT '筛选补签记录（用于统计）',
  INDEX `idx_sign_status`(`sign_status` ASC) USING BTREE COMMENT '按状态统计出勤率',
  CONSTRAINT `fk_sign_batch` FOREIGN KEY (`batch_id`) REFERENCES `project_batches` (`batch_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sign_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 6 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '每日签到表（含日报、补签、考勤状态）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of sign_ins
-- ----------------------------
INSERT INTO `sign_ins` VALUES (1, 5, 1, '2026-03-01', '08:30:00', '搭建开发环境，导入项目依赖，配置数据库连接。', 0, NULL, 'normal', '192.168.1.100', 'Mozilla/5.0...', '2026-06-02 10:28:48');
INSERT INTO `sign_ins` VALUES (2, 5, 1, '2026-03-02', '08:35:00', '完成需求分析初稿，绘制用例图和ER图，等待导师确认方向。', 0, NULL, 'normal', '192.168.1.100', 'Mozilla/5.0...', '2026-06-02 10:28:48');
INSERT INTO `sign_ins` VALUES (3, 6, 1, '2026-03-01', '09:00:00', '阅读微服务相关论文3篇，整理技术选型文档初稿。', 0, NULL, 'normal', '192.168.1.101', 'Mozilla/5.0...', '2026-06-02 10:28:48');
INSERT INTO `sign_ins` VALUES (4, 7, 2, '2026-03-01', '08:40:00', '安装Docker Desktop，运行第一个Hello World容器实例。', 0, NULL, 'normal', '192.168.1.102', 'Mozilla/5.0...', '2026-06-02 10:28:48');
INSERT INTO `sign_ins` VALUES (5, 5, 1, '2026-03-03', '09:10:00', '补签3月3日：因网络故障无法访问系统，现补充提交。今日完成接口文档编写。', 1, '网络故障', 'normal', '192.168.1.100', 'Mozilla/5.0...', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for student_selections
-- ----------------------------
DROP TABLE IF EXISTS `student_selections`;
CREATE TABLE `student_selections`  (
  `selection_id` int NOT NULL AUTO_INCREMENT COMMENT '选题记录唯一ID',
  `batch_id` int NOT NULL COMMENT '所属批次ID（选题必须在某一批次内进行）',
  `student_id` int NOT NULL COMMENT '学生用户ID',
  `topic_id` int NULL DEFAULT NULL COMMENT '题库题目ID（题库选题时填写，自主命题时为NULL）',
  `topic_type` enum('library','self') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '选题类型：library=从题库选择, self=自主命题',
  `self_topic_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '自主命题题目名称（topic_type=self时必填）',
  `self_topic_desc` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '自主命题详细描述',
  `version` int NOT NULL DEFAULT 1 COMMENT '选题版本号（首次=1，驳回后重新提交递增）',
  `previous_selection_id` int NULL DEFAULT NULL COMMENT '上一版本的选题ID（形成版本链，NULL表示初始版本）',
  `status` enum('pending','approved','rejected','cancelled') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'pending' COMMENT '审核状态：\r\n        - pending: 待审核（刚提交）\r\n        - approved: 已通过（进入下一环节）\r\n        - rejected: 已驳回（学生可重新提交新版本）\r\n        - cancelled: 已取消（管理员重置或学生撤销）',
  `reviewer_id` int NULL DEFAULT NULL COMMENT '审核人用户ID（指导老师）',
  `review_time` datetime NULL DEFAULT NULL COMMENT '审核时间',
  `review_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '审核意见/驳回理由（会通知给学生）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '选题提交时间',
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`selection_id`) USING BTREE,
  UNIQUE INDEX `uk_batch_student_version`(`batch_id` ASC, `student_id` ASC, `version` ASC) USING BTREE COMMENT '同批次同学生同版本唯一',
  INDEX `fk_sel_topic`(`topic_id` ASC) USING BTREE COMMENT '按题目查询谁选了它',
  INDEX `fk_sel_reviewer`(`reviewer_id` ASC) USING BTREE COMMENT '按审核人查询待办',
  INDEX `fk_sel_previous`(`previous_selection_id` ASC) USING BTREE COMMENT '版本链索引',
  INDEX `fk_sel_student`(`student_id` ASC) USING BTREE COMMENT '按学生查询选题历史',
  INDEX `idx_sel_status`(`status` ASC) USING BTREE COMMENT '按状态筛选（如查所有pending）',
  CONSTRAINT `fk_sel_batch` FOREIGN KEY (`batch_id`) REFERENCES `project_batches` (`batch_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_sel_previous` FOREIGN KEY (`previous_selection_id`) REFERENCES `student_selections` (`selection_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_sel_reviewer` FOREIGN KEY (`reviewer_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_sel_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_sel_topic` FOREIGN KEY (`topic_id`) REFERENCES `topics` (`topic_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '学生选题表（版本链式管理，支持驳回重提、历史追溯）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of student_selections
-- ----------------------------
INSERT INTO `student_selections` VALUES (1, 1, 5, 1, 'library', NULL, NULL, 1, NULL, 'approved', 3, '2026-03-10 10:00:00', '选题符合专业方向，同意。', '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `student_selections` VALUES (2, 1, 6, NULL, 'self', '基于微服务的校园一卡通系统', '设计并实现一个微服务架构的一卡通平台，集成支付、门禁、消费等功能', 1, NULL, 'approved', 3, '2026-03-11 09:30:00', NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `student_selections` VALUES (3, 2, 7, 3, 'library', NULL, NULL, 1, NULL, 'approved', 4, '2026-03-10 11:00:00', '题目难度适中，同意开题。', '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for system_configs
-- ----------------------------
DROP TABLE IF EXISTS `system_configs`;
CREATE TABLE `system_configs`  (
  `config_id` int NOT NULL AUTO_INCREMENT COMMENT '配置项唯一ID',
  `config_key` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置键（英文，如 max_makeup_days, upload_file_size_limit）',
  `config_value` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '配置值（根据config_type决定格式）',
  `config_type` enum('string','int','boolean','json','decimal') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'string' COMMENT '值类型：\r\n        - string: 字符串（如系统名称）\r\n        - int: 整数（如最大补签天数）\r\n        - boolean: 布尔值（true/false，如是否开启某功能）\r\n        - json: JSON对象/数组（如复杂的映射表、白名单）\r\n        - decimal: 小数（如默认权重比例）',
  `description` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '配置项说明（解释用途和取值范围）',
  `updated_by` int NULL DEFAULT NULL COMMENT '最后修改人ID',
  `update_time` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP COMMENT '最后修改时间',
  PRIMARY KEY (`config_id`) USING BTREE,
  UNIQUE INDEX `uk_config_key`(`config_key` ASC) USING BTREE COMMENT '配置键唯一（全局唯一标识）',
  INDEX `idx_config_type`(`config_type` ASC) USING BTREE COMMENT '按类型筛选',
  INDEX `fk_config_updated_by`(`updated_by` ASC) USING BTREE,
  CONSTRAINT `fk_config_updated_by` FOREIGN KEY (`updated_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 9 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '系统配置表【增强版】（支持多类型配置、JSON复杂结构、热更新）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of system_configs
-- ----------------------------
INSERT INTO `system_configs` VALUES (1, 'max_makeup_days', '3', 'int', '允许补签的最大天数（超出的补签请求将被拒绝）', NULL, '2026-06-02 10:28:48');
INSERT INTO `system_configs` VALUES (2, 'upload_file_size_limit', '10485760', 'int', '文件上传大小限制（字节），默认10MB = 10*1024*1024', NULL, '2026-06-02 10:28:48');
INSERT INTO `system_configs` VALUES (3, 'default_defense_ratio', '0.40', 'decimal', '答辩成绩默认占比（新建批次时的默认值，可被批次配置覆盖）', NULL, '2026-06-02 10:28:48');
INSERT INTO `system_configs` VALUES (4, 'default_report_ratio', '0.60', 'decimal', '报告成绩默认占比（新建批次时的默认值，应与defense_ratio之和接近1.0）', NULL, '2026-06-02 10:28:48');
INSERT INTO `system_configs` VALUES (5, 'defense_score_map', '{\"excellent\": 95, \"good\": 85, \"medium\": 75, \"pass\": 65, \"fail\": 50}', 'json', '答辩五级制到数值分数的映射表（用于自动转换计算总成绩）', NULL, '2026-06-02 10:28:48');
INSERT INTO `system_configs` VALUES (6, 'grade_level_map', '{\"A\": [90, 100], \"B\": [80, 89], \"C\": [70, 79], \"D\": [60, 69], \"F\": [0, 59]}', 'json', '总成绩到等级的映射规则（闭区间，用于自动评定等级）', NULL, '2026-06-02 10:28:48');
INSERT INTO `system_configs` VALUES (7, 'system_name', '毕业设计管理系统', 'string', '系统显示名称（出现在页面标题、邮件抬头等位置）', NULL, '2026-06-02 10:28:48');
INSERT INTO `system_configs` VALUES (8, 'enable_email_notification', 'true', 'boolean', '是否启用邮件通知（需要配置SMTP服务器）', NULL, '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for task_books
-- ----------------------------
DROP TABLE IF EXISTS `task_books`;
CREATE TABLE `task_books`  (
  `task_id` int NOT NULL AUTO_INCREMENT COMMENT '任务书唯一ID',
  `selection_id` int NOT NULL COMMENT '关联的学生选题ID（外键→student_selections）',
  `content` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '任务书具体内容和要求（支持富文本HTML，包括设计目标、参考文献、进度安排等）',
  `version` int NOT NULL DEFAULT 1 COMMENT '任务书版本号（首次下达=1，驳回后重下递增）',
  `status` enum('unissued','issued','rejected') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL DEFAULT 'unissued' COMMENT '状态：\r\n        - unissued: 未下达（选题刚通过，等待教师操作）\r\n        - issued: 已下达（学生可查看，正式生效）\r\n        - rejected: 已驳回（院级管理员认为不合格，需修改后重下）',
  `issuer_id` int NOT NULL COMMENT '下达任务的指导老师ID',
  `issued_at` datetime NULL DEFAULT NULL COMMENT '正式下达时间（status变为issued时记录）',
  `rejector_id` int NULL DEFAULT NULL COMMENT '驳回人ID（通常是院级管理员）',
  `reject_time` datetime NULL DEFAULT NULL COMMENT '驳回时间',
  `reject_comment` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '驳回原因和修改建议（会通知给指导老师）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`task_id`) USING BTREE,
  UNIQUE INDEX `uk_selection_version`(`selection_id` ASC, `version` ASC) USING BTREE COMMENT '同一选题每版本唯一',
  INDEX `fk_task_issuer`(`issuer_id` ASC) USING BTREE COMMENT '按教师查询其下达的任务书',
  INDEX `idx_task_status`(`status` ASC) USING BTREE COMMENT '按状态筛选（如查所有rejected需处理）',
  INDEX `fk_task_rejector`(`rejector_id` ASC) USING BTREE,
  CONSTRAINT `fk_task_issuer` FOREIGN KEY (`issuer_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_task_rejector` FOREIGN KEY (`rejector_id`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_task_selection` FOREIGN KEY (`selection_id`) REFERENCES `student_selections` (`selection_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '任务书表（版本化下达，支持驳回重下流程）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of task_books
-- ----------------------------
INSERT INTO `task_books` VALUES (1, 1, '1. 需求分析与系统设计\n2. 后端API开发（Spring Boot + MyBatis-Plus）\n3. 前端界面实现（Vue3 + Element Plus）\n4. 数据库设计与优化\n5. 测试与部署上线\n\n参考文献：《Java企业级开发实战》、《Vue.js设计与实现》', 1, 'issued', 3, '2026-03-15 08:00:00', NULL, NULL, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `task_books` VALUES (2, 2, '1. 微服务架构设计\n2. 核心服务模块开发（用户服务、支付服务、门禁服务）\n3. 服务间通信（gRPC / HTTP）\n4. 容器化部署（Docker Compose）\n5. 性能测试与优化\n\n注意：重点关注高并发场景下的数据一致性', 1, 'issued', 3, '2026-03-15 08:30:00', NULL, NULL, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `task_books` VALUES (3, 3, '1. Docker基础学习（镜像、容器、仓库）\n2. 编写Dockerfile（多阶段构建优化镜像大小）\n3. Docker Compose编排（开发环境一键启动）\n4. Kubernetes入门（Pod、Service、Deployment）\n5. CI/CD流水线搭建（GitLab CI / GitHub Actions）\n\n实验环境：本地Minikube或云服务商K8s集群', 1, 'issued', 4, '2026-03-16 10:00:00', NULL, NULL, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for teacher_student_relations
-- ----------------------------
DROP TABLE IF EXISTS `teacher_student_relations`;
CREATE TABLE `teacher_student_relations`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关系记录唯一ID',
  `batch_id` int NOT NULL COMMENT '所属批次ID',
  `teacher_id` int NOT NULL COMMENT '指导老师用户ID',
  `student_id` int NOT NULL COMMENT '学生用户ID',
  `assigned_by` int NULL DEFAULT NULL COMMENT '分配操作人ID（手动添加时记录）',
  `import_source` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '导入来源（manual=手动, excel=Excel批量导入）',
  `remark` varchar(500) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注（如\"换导师申请批准\"）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_batch_student`(`batch_id` ASC, `student_id` ASC) USING BTREE COMMENT '核心约束：同批次一学生只能有一导师',
  INDEX `fk_tsr_teacher`(`teacher_id` ASC) USING BTREE COMMENT '按老师查询其学生',
  INDEX `fk_tsr_student`(`student_id` ASC) USING BTREE COMMENT '按学生查询其导师',
  INDEX `fk_tsr_batch`(`batch_id` ASC) USING BTREE COMMENT '按批次查询所有关系',
  CONSTRAINT `fk_tsr_batch` FOREIGN KEY (`batch_id`) REFERENCES `project_batches` (`batch_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tsr_student` FOREIGN KEY (`student_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE,
  CONSTRAINT `fk_tsr_teacher` FOREIGN KEY (`teacher_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '师生分配关系表（支持手动添加和Excel批量导入）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of teacher_student_relations
-- ----------------------------
INSERT INTO `teacher_student_relations` VALUES (1, 1, 3, 5, 2, 'manual', NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `teacher_student_relations` VALUES (2, 1, 3, 6, 2, 'manual', NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `teacher_student_relations` VALUES (3, 2, 4, 7, 2, 'excel', '从教务系统导入', '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for topic_major_relations
-- ----------------------------
DROP TABLE IF EXISTS `topic_major_relations`;
CREATE TABLE `topic_major_relations`  (
  `id` int NOT NULL AUTO_INCREMENT COMMENT '关联记录唯一ID',
  `topic_id` int NOT NULL COMMENT '题目ID',
  `major_id` int NOT NULL COMMENT '纳入的专业ID',
  `included_by` int NULL DEFAULT NULL COMMENT '纳入操作人ID（通常是专业负责人）',
  `included_at` datetime NULL DEFAULT CURRENT_TIMESTAMP COMMENT '纳入时间',
  `remark` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '备注（如\"经审核适合本专业\"）',
  PRIMARY KEY (`id`) USING BTREE,
  UNIQUE INDEX `uk_topic_major`(`topic_id` ASC, `major_id` ASC) USING BTREE COMMENT '防止重复纳入同一专业',
  INDEX `fk_tmr_major`(`major_id` ASC) USING BTREE COMMENT '按专业查询已纳入题目',
  INDEX `fk_tmr_included_by`(`included_by` ASC) USING BTREE COMMENT '按操作人查询',
  CONSTRAINT `fk_tmr_included_by` FOREIGN KEY (`included_by`) REFERENCES `users` (`user_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_tmr_major` FOREIGN KEY (`major_id`) REFERENCES `majors` (`major_id`) ON DELETE CASCADE ON UPDATE CASCADE,
  CONSTRAINT `fk_tmr_topic` FOREIGN KEY (`topic_id`) REFERENCES `topics` (`topic_id`) ON DELETE CASCADE ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 5 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题目-专业关联表（支持跨专业题目共享）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of topic_major_relations
-- ----------------------------
INSERT INTO `topic_major_relations` VALUES (1, 1, 1, NULL, '2026-06-02 10:28:48', NULL);
INSERT INTO `topic_major_relations` VALUES (2, 2, 1, 2, '2026-06-02 10:28:48', '该算法题目也适合计科专业');
INSERT INTO `topic_major_relations` VALUES (3, 2, 2, NULL, NULL, NULL);
INSERT INTO `topic_major_relations` VALUES (4, 3, 2, NULL, NULL, NULL);

-- ----------------------------
-- Table structure for topics
-- ----------------------------
DROP TABLE IF EXISTS `topics`;
CREATE TABLE `topics`  (
  `topic_id` int NOT NULL AUTO_INCREMENT COMMENT '题目唯一ID',
  `topic_name` varchar(200) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '题目名称（简洁明确，≤200字）',
  `description` text CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL COMMENT '题目详细描述/要求（支持富文本HTML）',
  `difficulty` enum('easy','medium','hard') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'medium' COMMENT '难度等级：简单/中等/困难',
  `category` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '分类标签（如\"Web开发\"、\"算法研究\"、\"嵌入式\"）',
  `creator_id` int NOT NULL COMMENT '创建人用户ID（指导老师或管理员）',
  `selection_count` int NOT NULL DEFAULT 0 COMMENT '已被选择次数（每次选题成功后+1）',
  `max_students` int NULL DEFAULT 1 COMMENT '最大允许选择人数（0=不限制，默认1人一题）',
  `status` enum('available','unavailable','archived') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT 'available' COMMENT '题目状态：\r\n        - available: 可选用\r\n        - unavailable: 暂不可用（达到人数上限或季节性关闭）\r\n        - archived: 已归档（历史题目，不再显示在题库但保留记录）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`topic_id`) USING BTREE,
  INDEX `fk_topic_creator`(`creator_id` ASC) USING BTREE COMMENT '按创建人查询题目',
  INDEX `idx_topic_difficulty`(`difficulty` ASC) USING BTREE COMMENT '按难度筛选',
  INDEX `idx_topic_status`(`status` ASC) USING BTREE COMMENT '按状态筛选',
  FULLTEXT INDEX `ft_topic_search`(`topic_name`, `description`) COMMENT '全文搜索索引（按关键词搜题目）',
  CONSTRAINT `fk_topic_creator` FOREIGN KEY (`creator_id`) REFERENCES `users` (`user_id`) ON DELETE RESTRICT ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 4 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '题库表（支持难度分级、全文搜索、使用统计）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of topics
-- ----------------------------
INSERT INTO `topics` VALUES (1, '基于Spring Boot的顶岗实习管理系统', '前后端分离架构，实现学生、教师、管理员三大模块，包含签到、报告、评分等功能', 'medium', 'Web开发', 3, 1, 1, 'available', '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `topics` VALUES (2, '智能推荐算法在课程设计中的应用', '研究协同过滤、内容推荐等算法，并实现一个原型系统', 'hard', '算法研究', 3, 0, 1, 'available', '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `topics` VALUES (3, '微服务架构下的容器化部署实践', '使用Docker + Kubernetes完成传统单体应用的容器化改造和编排部署', 'medium', 'DevOps', 4, 1, 1, 'available', '2026-06-02 10:28:48', '2026-06-02 10:28:48');

-- ----------------------------
-- Table structure for users
-- ----------------------------
DROP TABLE IF EXISTS `users`;
CREATE TABLE `users`  (
  `user_id` int NOT NULL AUTO_INCREMENT COMMENT '用户唯一ID',
  `username` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '登录账号（工号/学号，全局唯一）',
  `real_name` varchar(20) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '真实姓名',
  `phone` varchar(11) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '绑定手机号（激活时必填）',
  `email` varchar(100) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '邮箱地址（可选）',
  `password_hash` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '加密后的密码（BCrypt算法）',
  `role` enum('student','teacher','major_admin','college_admin') CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NOT NULL COMMENT '用户主角色',
  `current_role_id` int NULL DEFAULT NULL COMMENT '当前使用的角色ID（多角色切换时使用，NULL表示使用主角色）',
  `college_id` int NULL DEFAULT NULL COMMENT '所属学院ID',
  `major_id` int NULL DEFAULT NULL COMMENT '所属专业ID（学生和教师必填）',
  `is_activated` tinyint(1) NOT NULL DEFAULT 0 COMMENT '账号是否已激活（0=未激活需绑定手机，1=已激活）',
  `status` tinyint(1) NOT NULL DEFAULT 1 COMMENT '账号状态（1=正常启用，0=已禁用）',
  `signature_path` varchar(255) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '教师手写签名图片路径（学生为NULL）',
  `title` varchar(50) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '职称（教授/副教授/讲师等，仅教师）',
  `last_login` datetime NULL DEFAULT NULL COMMENT '最后登录时间',
  `login_ip` varchar(45) CHARACTER SET utf8mb4 COLLATE utf8mb4_0900_ai_ci NULL DEFAULT NULL COMMENT '最后登录IP（支持IPv6）',
  `failed_login_count` int NULL DEFAULT 0 COMMENT '连续失败登录次数（防暴力破解）',
  `locked_until` datetime NULL DEFAULT NULL COMMENT '账户锁定截止时间（多次失败后临时锁定）',
  `created_at` datetime NULL DEFAULT CURRENT_TIMESTAMP,
  `updated_at` datetime NULL DEFAULT CURRENT_TIMESTAMP ON UPDATE CURRENT_TIMESTAMP,
  PRIMARY KEY (`user_id`) USING BTREE,
  UNIQUE INDEX `uk_username`(`username` ASC) USING BTREE COMMENT '登录账号唯一',
  UNIQUE INDEX `uk_phone`(`phone` ASC) USING BTREE COMMENT '手机号唯一（允许NULL）',
  INDEX `fk_user_college`(`college_id` ASC) USING BTREE,
  INDEX `fk_user_major`(`major_id` ASC) USING BTREE,
  INDEX `idx_user_role`(`role` ASC) USING BTREE COMMENT '按角色筛选用户',
  INDEX `idx_user_status`(`status` ASC) USING BTREE COMMENT '按状态筛选用户',
  INDEX `fk_user_current_role`(`current_role_id` ASC) USING BTREE,
  CONSTRAINT `fk_user_college` FOREIGN KEY (`college_id`) REFERENCES `colleges` (`college_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_user_current_role` FOREIGN KEY (`current_role_id`) REFERENCES `roles` (`role_id`) ON DELETE SET NULL ON UPDATE CASCADE,
  CONSTRAINT `fk_user_major` FOREIGN KEY (`major_id`) REFERENCES `majors` (`major_id`) ON DELETE SET NULL ON UPDATE CASCADE
) ENGINE = InnoDB AUTO_INCREMENT = 8 CHARACTER SET = utf8mb4 COLLATE = utf8mb4_0900_ai_ci COMMENT = '用户统一表（支持多角色切换、激活机制、安全审计）' ROW_FORMAT = Dynamic;

-- ----------------------------
-- Records of users
-- ----------------------------
INSERT INTO `users` VALUES (1, 'admin_c', '张院管', '13800000001', 'zhang@college.edu.cn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'college_admin', NULL, 1, NULL, 1, 1, NULL, '教授', NULL, NULL, 0, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `users` VALUES (2, 'major_1', '李专业', '13800000002', 'li@college.edu.cn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'major_admin', NULL, 1, 1, 1, 1, '/signatures/2.png', '副教授', NULL, NULL, 0, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `users` VALUES (3, 'teacher1', '王指导', '13800000003', 'wang@college.edu.cn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'teacher', NULL, 1, 1, 1, 1, '/signatures/3.png', '讲师', NULL, NULL, 0, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `users` VALUES (4, 'teacher2', '赵指导', '13800000004', 'zhao@college.edu.cn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'teacher', NULL, 1, 2, 1, 1, '/signatures/4.png', '教授', NULL, NULL, 0, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `users` VALUES (5, 'stu_01', '小明', '13800000005', 'xiaoming@student.edu.cn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'student', NULL, 1, 1, 1, 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `users` VALUES (6, 'stu_02', '小红', '13800000006', 'xiaohong@student.edu.cn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'student', NULL, 1, 1, 1, 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');
INSERT INTO `users` VALUES (7, 'stu_03', '小刚', '13800000007', 'xiaogang@student.edu.cn', '$2a$10$N9qo8uLOickgx2ZMRZoMyeIjZAgcfl7p92ldGxad68LJZdL17lhWy', 'student', NULL, 1, 2, 1, 1, NULL, NULL, NULL, NULL, 0, NULL, '2026-06-02 10:28:48', '2026-06-02 10:28:48');

SET FOREIGN_KEY_CHECKS = 1;
