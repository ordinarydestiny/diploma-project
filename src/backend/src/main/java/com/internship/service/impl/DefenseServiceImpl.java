package com.internship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.internship.entity.Defense;
import com.internship.entity.File;
import com.internship.entity.StudentSelection;
import com.internship.mapper.DefenseMapper;
import com.internship.mapper.FileMapper;
import com.internship.mapper.StudentSelectionMapper;
import com.internship.service.DefenseService;
import com.internship.service.ScoreComputationService;
import com.internship.util.JwtUtil;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;

@Slf4j
@Service
public class DefenseServiceImpl extends ServiceImpl<DefenseMapper, Defense> implements DefenseService {

    private final DefenseMapper defenseMapper;
    private final FileMapper fileMapper;
    private final StudentSelectionMapper studentSelectionMapper;
    private final ScoreComputationService scoreComputationService;
    private final JwtUtil jwtUtil;

    public DefenseServiceImpl(DefenseMapper defenseMapper, FileMapper fileMapper, 
                              StudentSelectionMapper studentSelectionMapper,
                              ScoreComputationService scoreComputationService, JwtUtil jwtUtil) {
        this.defenseMapper = defenseMapper;
        this.fileMapper = fileMapper;
        this.studentSelectionMapper = studentSelectionMapper;
        this.scoreComputationService = scoreComputationService;
        this.jwtUtil = jwtUtil;
    }

    @Override
    @Transactional
    public Defense submitByStudent(Integer selectionId, String defenseScore, Integer recordFileId) {
        Defense defense = new Defense();
        defense.setSelectionId(selectionId);
        defense.setDefenseScore(defenseScore);
        defense.setRecordFileId(recordFileId);
        defense.setSubmitterType("student");
        defense.setSubmitTime(LocalDateTime.now());
        defense.setStatus("pending");
        defense.setCreatedAt(LocalDateTime.now());
        
        save(defense);
        log.info("学生提交答辩记录，选题={}，分数等级={}", selectionId, defenseScore);
        return defense;
    }

    @Override
    @Transactional
    public Defense submitByStudentFull(Integer selectionId, String defenseScore, Integer recordFileId, 
                                       String selfEvaluation, String pptFileId, String defenseDatetime) {
        
        log.info("【开始】学生提交答辩信息：selectionId={}, defenseScore={}, recordFileId={}, selfEvaluation长度={}, pptFileId={}, defenseDatetime={}", 
            selectionId, defenseScore, recordFileId, 
            selfEvaluation != null ? selfEvaluation.length() : 0,
            pptFileId, defenseDatetime);
        
        // 参数校验
        if (selectionId == null) {
            throw new RuntimeException("选题ID不能为空");
        }
        
        // 获取当前登录的学生ID（提前获取，以便在重复检测时使用）
        Long userIdLong = jwtUtil.getCurrentUserId();
        Integer studentId = userIdLong != null ? userIdLong.intValue() : null;
        
        if (studentId == null) {
            log.warn("无法获取当前登录用户ID");
        }
        
        // 【重要】检查该选题是否已存在答辩记录
        Defense existingDefense = defenseMapper.selectBySelectionId(selectionId);
        if (existingDefense != null) {
            String currentStatus = existingDefense.getStatus();
            log.warn("【警告】选题{}已存在答辩记录，当前状态：{}", selectionId, currentStatus);
            
            if ("pending".equals(currentStatus) || "approved".equals(currentStatus)) {
                throw new RuntimeException("您已经提交过答辩信息，当前状态为：" + 
                    ("pending".equals(currentStatus) ? "待审核" : "已通过") + 
                    "。如需修改请联系指导教师。");
            } else if ("submitted".equals(currentStatus)) {
                throw new RuntimeException("您的答辩申请正在审核中，请等待教师处理。");
            } else {
                // not_started 或 rejected 状态，允许更新现有记录
                log.info("【更新】允许重新提交答辩信息，原状态：{}", currentStatus);
                return updateExistingStudentDefense(existingDefense, selectionId, defenseScore, 
                    recordFileId, selfEvaluation, pptFileId, defenseDatetime, studentId);
            }
        }
        
        try {
            Defense defense = new Defense();
            defense.setSelectionId(selectionId);
            
            // 【重要】defenseScore字段是ENUM类型，只能接受特定值
            // 允许的值: excellent, good, medium, pass, fail
            // 学生提交时使用默认值 'pending' 不在枚举中，所以用 'pass' 作为占位符
            if (defenseScore == null || defenseScore.trim().isEmpty()) {
                defense.setDefenseScore("pass");  // 默认值（及格），后续教师会修改
                log.warn("defenseScore为空或无效，使用默认值'pass'");
            } else if (
                "excellent".equalsIgnoreCase(defenseScore) ||
                "good".equalsIgnoreCase(defenseScore) ||
                "medium".equalsIgnoreCase(defenseScore) ||
                "pass".equalsIgnoreCase(defenseScore) ||
                "fail".equalsIgnoreCase(defenseScore)
            ) {
                defense.setDefenseScore(defenseScore.trim());
            } else {
                // 如果传入的不是有效枚举值（如 'pending'），使用默认值
                defense.setDefenseScore("pass");
                log.warn("defenseScore '{}' 不是有效的枚举值(excellent/good/medium/pass/fail)，使用默认值'pass'", 
                    defenseScore);
            }
            
            // 【重要】处理文件ID - 必须使用有效的file_id（外键约束）
            if (recordFileId != null && recordFileId > 0) {
                defense.setRecordFileId(recordFileId);
                log.info("学生上传了答辩文件，文件ID: {}", recordFileId);
            } else {
                // 学生未上传文件，使用默认文件ID
                Integer defaultFileId = findDefaultFileId();
                defense.setRecordFileId(defaultFileId);
                log.warn("学生未上传答辩文件（recordFileId={}），使用默认文件ID: {}", 
                    recordFileId, defaultFileId);
            }
            
            // 设置提交者信息
            defense.setSubmitterType("student");
            
            // 【重要】如果submitter_id获取失败，使用默认值0
            if (studentId == null) {
                defense.setSubmitterId(0);  // 使用0作为占位符，表示"未知用户"
                log.warn("无法获取当前登录用户ID(submitter_id)，使用默认值0");
            } else {
                defense.setSubmitterId(studentId);
            }
            defense.setSubmitTime(LocalDateTime.now());
            
            // 初始状态为"待审核"（等待教师确认）
            defense.setStatus("pending");
            defense.setCreatedAt(LocalDateTime.now());
            
            // 【新增】设置学生自评信息 - 答辩日期时间
            if (defenseDatetime != null && !defenseDatetime.trim().isEmpty()) {
                try {
                    defense.setDefenseDatetime(LocalDateTime.parse(defenseDatetime.trim(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    log.info("解析答辩日期成功: {}", defense.getDefenseDatetime());
                } catch (Exception e) {
                    log.warn("解析答辩日期失败: {}, 使用当前时间, 错误: {}", defenseDatetime, e.getMessage());
                    defense.setDefenseDatetime(LocalDateTime.now());  // 默认当前时间
                }
            } else {
                defense.setDefenseDatetime(LocalDateTime.now());  // 默认当前时间
                log.info("未提供答辩日期，使用当前时间");
            }
            
            // 学生自我评价（存入review_comment字段作为初始值）
            if (selfEvaluation != null && !selfEvaluation.trim().isEmpty()) {
                defense.setReviewComment(selfEvaluation.trim());
                log.info("自我评价已设置，长度: {}", selfEvaluation.trim().length());
            } else {
                defense.setReviewComment("学生已提交答辩申请，等待教师审核确认。");
                log.info("未提供自我评价，使用默认值");
            }
            
            // PPT文件ID（暂时不单独存储，可以后续扩展字段）
            if (pptFileId != null && !pptFileId.trim().isEmpty()) {
                log.info("收到PPT文件ID: {}（暂未使用）", pptFileId);
            }
            
            // 保存到数据库
            log.info("准备保存Defense对象到数据库...");
            save(defense);
            
            log.info("【成功】学生提交答辩记录成功！defenseId={}" +
                    ", 选题={}，成绩等级={}，预计答辩时间={}",
                    defense.getDefenseId(), 
                    selectionId, 
                    defense.getDefenseScore(),
                    defense.getDefenseDatetime());
            
            return defense;
            
        } catch (Exception e) {
            log.error("【错误】学生提交答辩记录失败！错误类型: {}, 错误消息: {}", 
                e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("提交答辩信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public Defense submitByTeacher(Integer selectionId, String defenseScore, BigDecimal defenseScoreNum, Integer recordFileId) {
        Defense defense = new Defense();
        defense.setSelectionId(selectionId);
        defense.setDefenseScore(defenseScore);
        defense.setDefenseScoreNum(defenseScoreNum);
        defense.setRecordFileId(recordFileId);
        defense.setSubmitterType("teacher");
        defense.setSubmitTime(LocalDateTime.now());
        defense.setStatus("pending");
        defense.setCreatedAt(LocalDateTime.now());
        
        save(defense);
        log.info("教师提交答辩记录，选题={}，分数={}", selectionId, defenseScoreNum);
        return defense;
    }

    @Override
    @Transactional
    public Defense submitByTeacherFull(Integer selectionId, String defenseScore, BigDecimal defenseScoreNum, 
                                       Integer recordFileId, String defenseDatetime, String location, 
                                       String committee, String comment) {
        
        log.info("【开始】教师录入答辩信息：selectionId={}, defenseScore={}, defenseScoreNum={}, recordFileId={}", 
            selectionId, defenseScore, defenseScoreNum, recordFileId);
        
        // 参数校验
        if (selectionId == null) {
            throw new RuntimeException("选题ID不能为空");
        }
        
        // 获取当前登录的教师ID
        Long userIdLong = jwtUtil.getCurrentUserId();
        Integer teacherId = userIdLong != null ? userIdLong.intValue() : null;
        
        if (teacherId == null) {
            log.warn("无法获取当前登录用户ID");
        }
        
        // 【重要】检查该选题是否已存在答辩记录
        Defense existingDefense = defenseMapper.selectBySelectionId(selectionId);
        if (existingDefense != null) {
            String currentStatus = existingDefense.getStatus();
            log.info("【检测到】选题{}已存在答辩记录，当前状态：{}，允许教师更新", selectionId, currentStatus);
            
            // 教师有权限更新任何状态的记录（除了已通过的）
            return updateExistingTeacherDefense(existingDefense, selectionId, defenseScore, 
                defenseScoreNum, recordFileId, defenseDatetime, location, committee, comment, teacherId);
        }
        
        // 【重要】defenseScore字段是ENUM类型，只能接受特定值
        String normalizedScore = normalizeDefenseScore(defenseScore);
        
        try {
            Defense defense = new Defense();
            defense.setSelectionId(selectionId);
            defense.setDefenseScore(normalizedScore);  // 使用标准化后的成绩等级
            defense.setDefenseScoreNum(defenseScoreNum);  // 数值分数（如：85.0）
            
            // 处理文件ID - 【重要】必须使用有效的file_id（外键约束）
            // 如果前端没有上传新文件，需要查找一个有效的文件ID作为占位符
            if (recordFileId != null && recordFileId > 0) {
                defense.setRecordFileId(recordFileId);
                log.info("使用传入的答辩文件ID: {}", recordFileId);
            } else {
                // 查找files表中最小的有效file_id作为默认值
                Integer defaultFileId = findDefaultFileId();
                defense.setRecordFileId(defaultFileId);
                log.warn("未提供有效文件ID（{}），使用默认文件ID: {}", recordFileId, defaultFileId);
            }
            
            defense.setSubmitterType("teacher");
            
            // 设置提交者信息
            if (teacherId != null) {
                defense.setSubmitterId(teacherId);
            } else {
                defense.setSubmitterId(0);
            }
            defense.setSubmitTime(LocalDateTime.now());
            defense.setStatus("pending");  // 初始状态为"待审核"
            defense.setCreatedAt(LocalDateTime.now());
            
            // 设置完整答辩信息 - 答辩日期时间
            if (defenseDatetime != null && !defenseDatetime.isEmpty()) {
                try {
                    defense.setDefenseDatetime(LocalDateTime.parse(defenseDatetime, 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                    log.info("解析答辩日期成功: {}", defense.getDefenseDatetime());
                } catch (Exception e) {
                    log.warn("解析答辩日期失败: {}, 使用当前时间", defenseDatetime);
                    defense.setDefenseDatetime(LocalDateTime.now());
                }
            } else {
                defense.setDefenseDatetime(LocalDateTime.now());
                log.info("未提供答辩日期，使用当前时间");
            }
            
            // 答辩地点
            defense.setLocation(location != null ? location : "");
            
            // 答辩委员会
            defense.setCommittee(committee != null ? committee : "");
            
            // 答辩评语（存入reviewComment字段作为初始评语）
            defense.setReviewComment(comment != null ? comment : "");
            
            // 保存到数据库
            save(defense);
            
            // 【重要】自动重新计算总成绩
            autoRecomputeTotalScore(selectionId, "教师录入答辩成绩");
            
            log.info("【成功】教师录入答辩记录成功！defenseId={}" +
                    ", 选题={}，成绩等级={}，数值分数={}，预计答辩时间={}，地点={}，委员会={}",
                    defense.getDefenseId(), 
                    selectionId, 
                    normalizedScore,
                    defenseScoreNum,
                    defense.getDefenseDatetime(),
                    location,
                    committee);
            
            return defense;
            
        } catch (Exception e) {
            log.error("【错误】教师录入答辩记录失败！错误类型: {}, 错误消息: {}", 
                e.getClass().getName(), e.getMessage(), e);
            throw new RuntimeException("教师录入答辩信息失败: " + e.getMessage(), e);
        }
    }
    
    /**
     * 标准化defenseScore值（支持中文和英文）
     */
    private String normalizeDefenseScore(String defenseScore) {
        if (defenseScore == null || defenseScore.trim().isEmpty()) {
            log.warn("defenseScore为空或无效，使用默认值'pass'");
            return "pass";  // 默认及格
        }
        
        String trimmed = defenseScore.trim().toLowerCase();
        
        // 英文枚举值（直接返回）
        switch (trimmed) {
            case "excellent":
            case "good":
            case "medium":
            case "pass":
            case "fail":
                log.info("defenseScore '{}' 是有效的英文枚举值", trimmed);
                return trimmed;
                
            // 中文映射（转换为英文）
            case "优秀":
                log.info("将中文'优秀'转换为'excellent'");
                return "excellent";
            case "良好":
                log.info("将中文'良好'转换为'good'");
                return "good";
            case "中等":
                log.info("将中文'中等'转换为'medium'");
                return "medium";
            case "及格":
                log.info("将中文'及格'转换为'pass'");
                return "pass";
            case "不及格":
                log.info("将中文'不及格'转换为'fail'");
                return "fail";
                
            default:
                // 无法识别的值，使用默认值
                log.warn("defenseScore '{}' 不是有效的枚举值，使用默认值'pass'", trimmed);
                return "pass";
        }
    }
    
    /**
     * 更新已存在的教师答辩记录（用于重新录入场景）
     */
    private Defense updateExistingTeacherDefense(Defense existingDefense, Integer selectionId, 
            String defenseScore, BigDecimal defenseScoreNum, Integer recordFileId, 
            String defenseDatetime, String location, String committee, String comment, 
            Integer teacherId) {
        
        log.info("【开始更新】教师重新录入答辩信息：selectionId={}, 原defenseId={}", 
            selectionId, existingDefense.getDefenseId());
        
        try {
            // 标准化并更新成绩等级
            String normalizedScore = normalizeDefenseScore(defenseScore);
            existingDefense.setDefenseScore(normalizedScore);
            
            // 更新数值分数
            if (defenseScoreNum != null) {
                existingDefense.setDefenseScoreNum(defenseScoreNum);
            }
            
            // 【重要】更新文件ID - 只有当传入有效文件ID时才更新
            // 如果前端没有上传新文件（recordFileId为null或0），保持原有文件ID不变
            if (recordFileId != null && recordFileId > 0) {
                existingDefense.setRecordFileId(recordFileId);
                log.info("更新答辩文件ID: {}", recordFileId);
            } else {
                log.info("未提供新文件ID（{}），保持原文件ID不变: {}", 
                    recordFileId, existingDefense.getRecordFileId());
            }
            
            // 更新提交者信息
            existingDefense.setSubmitterType("teacher");
            if (teacherId != null) {
                existingDefense.setSubmitterId(teacherId);
            } else {
                existingDefense.setSubmitterId(0);
            }
            existingDefense.setSubmitTime(LocalDateTime.now());
            
            // 重置状态为"待审核"
            existingDefense.setStatus("pending");
            
            // 更新答辩日期时间
            if (defenseDatetime != null && !defenseDatetime.isEmpty()) {
                try {
                    existingDefense.setDefenseDatetime(LocalDateTime.parse(defenseDatetime, 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    existingDefense.setDefenseDatetime(LocalDateTime.now());
                }
            } else {
                existingDefense.setDefenseDatetime(LocalDateTime.now());
            }
            
            // 更新其他信息
            existingDefense.setLocation(location != null ? location : "");
            existingDefense.setCommittee(committee != null ? committee : "");
            existingDefense.setReviewComment(comment != null ? comment : "");
            
            // 清空审核信息（因为重新录入了）
            existingDefense.setReviewerId(null);
            existingDefense.setReviewTime(null);
            
            // 保存更新
            updateById(existingDefense);
            
            // 【重要】自动重新计算总成绩
            autoRecomputeTotalScore(selectionId, "教师更新答辩成绩");
            
            log.info("【成功】教师答辩记录更新成功！defenseId={}，选题={}，状态=待审核", 
                existingDefense.getDefenseId(), selectionId);
            
            return existingDefense;
            
        } catch (Exception e) {
            log.error("【错误】更新教师答辩记录失败！错误: {}", e.getMessage(), e);
            throw new RuntimeException("更新答辩信息失败: " + e.getMessage(), e);
        }
    }

    @Override
    @Transactional
    public void reviewDefense(Integer defenseId, String status, String comment, 
                              String defenseScore, BigDecimal defenseScoreNum) {
        Defense defense = getById(defenseId);
        if (defense == null) {
            throw new RuntimeException("答辩记录不存在");
        }
        
        defense.setStatus(status);
        defense.setReviewTime(LocalDateTime.now());
        defense.setReviewComment(comment);
        defense.setUpdatedAt(LocalDateTime.now());
        
        // 【重要修复】如果审核通过时传入了分数和等级，一并保存
        if ("approved".equals(status)) {
            if (defenseScore != null && !defenseScore.isEmpty()) {
                String normalizedScore = normalizeDefenseScore(defenseScore);
                defense.setDefenseScore(normalizedScore);
                log.info("审核通过时更新成绩等级: {}", normalizedScore);
            }
            if (defenseScoreNum != null) {
                defense.setDefenseScoreNum(defenseScoreNum);
                log.info("审核通过时更新答辩分数: {}", defenseScoreNum);
            }
        }
        
        updateById(defense);
        log.info("审核答辩记录{}：状态={}", defenseId, status);
    }
    
    /**
     * 查找默认的文件ID（用于外键约束）
     * 当没有实际文件时，返回一个有效的file_id作为占位符
     */
    private Integer findDefaultFileId() {
        try {
            // 查询files表中最小的有效file_id
            File defaultFile = fileMapper.selectMinFileId();
            if (defaultFile != null && defaultFile.getFileId() != null) {
                log.info("找到默认文件ID: {}", defaultFile.getFileId());
                return defaultFile.getFileId();
            } else {
                log.warn("files表中没有任何记录，使用备用方案");
                return createDefaultFileRecord();
            }
        } catch (Exception e) {
            log.error("查找默认文件ID失败: {}", e.getMessage(), e);
            return createDefaultFileRecord();
        }
    }
    
    /**
     * 创建默认文件记录（当files表为空时使用）
     */
    private Integer createDefaultFileRecord() {
        try {
            com.internship.entity.File defaultFile = new com.internship.entity.File();
            defaultFile.setOriginalName("default_placeholder.txt");
            defaultFile.setStoragePath("./uploads/default/default_placeholder.txt");
            defaultFile.setFileSize(0L);
            defaultFile.setMimeType("text/plain");
            defaultFile.setUploaderId(1);  // 系统管理员
            defaultFile.setUploadTime(java.time.LocalDateTime.now());
            defaultFile.setRelationType("defense_placeholder");
            defaultFile.setRelationId(0);
            defaultFile.setIsDeleted(false);
            
            fileMapper.insert(defaultFile);
            
            log.info("创建默认文件记录成功，fileId: {}", defaultFile.getFileId());
            return defaultFile.getFileId();
        } catch (Exception e) {
            log.error("创建默认文件记录失败: {}", e.getMessage(), e);
            throw new RuntimeException("无法创建默认文件记录，请检查数据库连接", e);
        }
    }
    
    /**
     * 更新已存在的学生答辩记录（用于重新提交场景）
     */
    private Defense updateExistingStudentDefense(Defense existingDefense, Integer selectionId, 
            String defenseScore, Integer recordFileId, String selfEvaluation, 
            String pptFileId, String defenseDatetime, Integer studentId) {
        
        log.info("【开始更新】重新提交答辩信息：selectionId={}, 原defenseId={}", 
            selectionId, existingDefense.getDefenseId());
        
        try {
            // 更新成绩等级
            if (defenseScore == null || defenseScore.trim().isEmpty()) {
                existingDefense.setDefenseScore("pass");
            } else if (
                "excellent".equalsIgnoreCase(defenseScore) ||
                "good".equalsIgnoreCase(defenseScore) ||
                "medium".equalsIgnoreCase(defenseScore) ||
                "pass".equalsIgnoreCase(defenseScore) ||
                "fail".equalsIgnoreCase(defenseScore)
            ) {
                existingDefense.setDefenseScore(defenseScore.trim());
            } else {
                existingDefense.setDefenseScore("pass");
            }
            
            // 更新文件ID - 【重要】只有当传入有效文件ID时才更新
            if (recordFileId != null && recordFileId > 0) {
                existingDefense.setRecordFileId(recordFileId);
                log.info("更新学生答辩文件ID: {}", recordFileId);
            } else {
                log.info("未提供新文件ID（{}），保持原文件ID不变: {}", 
                    recordFileId, existingDefense.getRecordFileId());
            }
            
            // 更新提交者信息
            existingDefense.setSubmitterType("student");
            if (studentId != null) {
                existingDefense.setSubmitterId(studentId);
            } else {
                existingDefense.setSubmitterId(0);
            }
            existingDefense.setSubmitTime(LocalDateTime.now());
            
            // 重置状态为"待审核"
            existingDefense.setStatus("pending");
            
            // 更新答辩日期时间
            if (defenseDatetime != null && !defenseDatetime.trim().isEmpty()) {
                try {
                    existingDefense.setDefenseDatetime(LocalDateTime.parse(defenseDatetime.trim(), 
                        DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss")));
                } catch (Exception e) {
                    existingDefense.setDefenseDatetime(LocalDateTime.now());
                }
            } else {
                existingDefense.setDefenseDatetime(LocalDateTime.now());
            }
            
            // 更新自我评价
            if (selfEvaluation != null && !selfEvaluation.trim().isEmpty()) {
                existingDefense.setReviewComment(selfEvaluation.trim());
            } else {
                existingDefense.setReviewComment("学生已重新提交答辩申请，等待教师审核确认。");
            }
            
            // 清空审核信息（因为重新提交了）
            existingDefense.setReviewerId(null);
            existingDefense.setReviewTime(null);
            
            // 保存更新
            updateById(existingDefense);
            
            log.info("【成功】答辩记录更新成功！defenseId={}，选题={}，状态=待审核", 
                existingDefense.getDefenseId(), selectionId);
            
            return existingDefense;
            
        } catch (Exception e) {
            log.error("【错误】更新答辩记录失败！错误: {}", e.getMessage(), e);
            throw newRuntimeException("更新答辩信息失败: " + e.getMessage(), e);
        }
    }
    
    private RuntimeException newRuntimeException(String message, Exception e) {
        return new RuntimeException(message, e);
    }
    
    /**
     * 自动重新计算总成绩（在答辩成绩更新后调用）
     * @param selectionId 选题ID
     * @param trigger 触发原因（用于日志）
     */
    private void autoRecomputeTotalScore(Integer selectionId, String trigger) {
        try {
            // 通过selectionId查询学生选题信息
            StudentSelection selection = studentSelectionMapper.selectById(selectionId);
            if (selection == null) {
                log.warn("【自动重算】未找到选题ID={}的记录，跳过总成绩重算", selectionId);
                return;
            }
            
            Integer studentId = selection.getStudentId();
            Integer batchId = selection.getBatchId();
            
            if (studentId == null || batchId == null) {
                log.warn("【自动重算】选题ID={}缺少studentId或batchId，跳过总成绩重算", selectionId);
                return;
            }
            
            log.info("【自动重算】触发原因：{}，开始重新计算学生{}在批次{}的总成绩...", 
                trigger, studentId, batchId);
            
            // 调用成绩计算服务重新计算
            scoreComputationService.recompute(studentId, batchId);
            
            log.info("【自动重算】✅ 总成绩重新计算完成！studentId={}, batchId={}", studentId, batchId);
            
        } catch (Exception e) {
            // 自动重算失败不应该影响主流程，只记录警告
            log.error("【自动重算】❌ 重新计算总成绩失败（不影响主操作）: {}", e.getMessage(), e);
        }
    }
}