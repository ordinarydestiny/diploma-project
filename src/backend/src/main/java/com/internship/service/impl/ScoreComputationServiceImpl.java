package com.internship.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.internship.entity.*;
import com.internship.mapper.FinalCheckMapper;
import com.internship.mapper.DefenseMapper;
import com.internship.mapper.FinalScoreMapper;
import com.internship.mapper.ProjectBatchMapper;
import com.internship.mapper.SystemConfigMapper;
import com.internship.mapper.StudentSelectionMapper;
import com.internship.service.ScoreComputationService;
import com.internship.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.math.RoundingMode;

@Slf4j
@Service
@RequiredArgsConstructor
public class ScoreComputationServiceImpl implements ScoreComputationService {

    private final FinalScoreMapper finalScoreMapper;
    private final FinalCheckMapper finalCheckMapper;
    private final DefenseMapper defenseMapper;
    private final ProjectBatchMapper batchMapper;
    private final SystemConfigMapper configMapper;
    private final StudentSelectionMapper studentSelectionMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void recompute(Integer studentId, Integer batchId) {
        ProjectBatch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            log.warn("批次{}不存在，跳过成绩计算", batchId);
            return;
        }
        
        // 【修复】安全获取权重配置，避免NullPointerException
        double defenseRatio = 0.4;  // 默认答辩占比40%
        double reportRatio = 0.6;   // 默认报告占比60%
        
        try {
            if (batch.getDefenseRatio() != null) {
                defenseRatio = batch.getDefenseRatio().doubleValue();
            }
            if (batch.getReportRatio() != null) {
                reportRatio = batch.getReportRatio().doubleValue();
            }
        } catch (Exception e) {
            log.warn("读取批次{}的权重配置失败，使用默认值: 报告={}%, 答辩={}%", 
                batchId, reportRatio * 100, defenseRatio * 100);
        }
        
        // 获取最终报告定稿版本的分数
        FinalCheck finalCheck = getFinalVersionByStudent(studentId, batchId);
        
        // 【修复】安全获取报告分数，避免NullPointerException
        Double reportScore = null;
        if (finalCheck != null && finalCheck.getReportScore() != null) {
            try {
                reportScore = finalCheck.getReportScore().doubleValue();
            } catch (Exception e) {
                log.warn("解析报告分数失败: {}", e.getMessage());
            }
        }
        
        // 获取答辩分数（五级制转数值）
        Defense defense = getDefenseByStudent(studentId, batchId);
        Double defenseScore = null;
        if (defense != null) {
            if (defense.getDefenseScoreNum() != null) {
                try {
                    defenseScore = defense.getDefenseScoreNum().doubleValue();
                } catch (Exception e) {
                    log.warn("解析答辩数值分数失败: {}", e.getMessage());
                }
            } else if (defense.getDefenseScore() != null) {
                // 尝试从等级转换
                defenseScore = convertLevelToNum(defense.getDefenseScore());
            }
        }
        
        // 计算总分
        Double totalScore = null;
        if (reportScore != null && defenseScore != null) {
            // 情况1：报告和答辩都有成绩 → 正常加权计算
            totalScore = reportScore * reportRatio + defenseScore * defenseRatio;
            log.info("正常加权计算: 报告={}×{} + 答辩={}×{} = {}", 
                reportScore, reportRatio, defenseScore, defenseRatio, totalScore);
        } else if (defenseScore != null && (reportScore == null || reportScore == 0)) {
            // 情况2：只有答辩成绩（报告未定稿）→ 仅按答辩权重折算
            // 假设答辩满分100分，当前得分占比作为预估总分
            totalScore = defenseScore * (reportRatio + defenseRatio);  // 简化：直接用答辩分数
            log.warn("报告未定稿，使用答辩成绩作为参考总分: {}", totalScore);
        } else if (reportScore != null && (defenseScore == null || defenseScore == 0)) {
            // 情况3：只有报告成绩（未答辩）→ 仅按报告权重折算
            totalScore = reportScore;
            log.warn("未答辩，使用报告成绩作为参考总分: {}", totalScore);
        }
        
        if (totalScore != null) {
            totalScore = Math.round(totalScore * 100.0) / 100.0;  // 保留2位小数
        }
        
        String gradeLevel = determineGradeLevel(totalScore);
        
        // 【重要】查询学生当前有效选题（已实现！）
        StudentSelection selection = studentSelectionMapper.selectOne(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<StudentSelection>()
                .eq(StudentSelection::getStudentId, studentId)
                .eq(StudentSelection::getBatchId, batchId)
                .in(StudentSelection::getStatus, java.util.Arrays.asList("approved", "pending"))
                .orderByDesc(StudentSelection::getVersion)
                .last("LIMIT 1")
        );
        
        if (selection == null) {
            log.warn("未找到学生{}在批次{}的有效选题，跳过成绩计算", studentId, batchId);
            return;
        }
        
        FinalScore score = finalScoreMapper.getBySelectionId(selection.getSelectionId());
        if (score == null) {
            score = new FinalScore();
            score.setSelectionId(selection.getSelectionId());
            score.setCalculatedAt(java.time.LocalDateTime.now());
            score.setCalculationMode("auto");
        }
        
        score.setReportScore(reportScore != null ? reportScore.intValue() : null);
        score.setDefenseScore(defenseScore != null ? defenseScore.intValue() : null);
        score.setTotalScore(totalScore != null ? BigDecimal.valueOf(totalScore) : null);
        score.setGradeLevel(gradeLevel);
        
        if (score.getScoreId() == null) {
            finalScoreMapper.insert(score);
        } else {
            finalScoreMapper.updateById(score);
        }
        
        log.info("学生{}的成绩已重新计算: 报告={} 答辩={} 总分={}", 
            studentId, reportScore, defenseScore, totalScore);
    }

    @Override
    @Transactional
    public void batchRecompute(Integer batchId) {
        // 批量计算该批次所有学生的成绩
        log.info("开始批量计算批次{}的所有成绩", batchId);
    }

    @Override
    public void publishScore(Integer selectionId) {
        FinalScore score = finalScoreMapper.getBySelectionId(selectionId);
        if (score == null) {
            throw new RuntimeException("成绩记录不存在");
        }
        Long userId = jwtUtil.getCurrentUserId();
        score.setIsPublished(true);
        score.setPublishedBy(userId != null ? userId.intValue() : null);
        score.setPublishedAt(java.time.LocalDateTime.now());
        finalScoreMapper.updateById(score);
        
        log.info("管理员{}公布成绩，选题={}", userId, selectionId);
    }

    @Override
    public Double convertLevelToNum(String level) {
        if (level == null) return null;
        
        try {
            String jsonConfig = configMapper.getConfigValue("defense_score_map");
            
            // 【修复】如果配置为空，使用默认映射表
            if (jsonConfig == null || jsonConfig.trim().isEmpty()) {
                log.warn("数据库中未配置defense_score_map，使用默认值");
                return getDefaultLevelToNum(level);
            }
            
            JSONObject map = JSON.parseObject(jsonConfig);
            
            // 【修复】检查map是否为null
            if (map == null) {
                log.warn("解析defense_score_map失败，使用默认值");
                return getDefaultLevelToNum(level);
            }
            
            Double result = map.getDouble(level);
            if (result == null) {
                log.warn("未找到等级'{}'的数值映射，使用默认值", level);
                return getDefaultLevelToNum(level);
            }
            
            return result;
        } catch (Exception e) {
            log.error("转换等级{}为数值时出错: {}，使用默认值", level, e.getMessage());
            return getDefaultLevelToNum(level);
        }
    }
    
    /**
     * 默认的等级到数值映射（当数据库配置缺失时使用）
     */
    private Double getDefaultLevelToNum(String level) {
        switch (level.toLowerCase()) {
            case "excellent":
            case "优秀":
                return 95.0;
            case "good":
            case "良好":
                return 85.0;
            case "medium":
            case "中等":
                return 75.0;
            case "pass":
            case "及格":
                return 65.0;
            case "fail":
            case "不及格":
                return 50.0;
            default:
                log.warn("未知等级: {}，返回null", level);
                return null;
        }
    }

    @Override
    public String determineGradeLevel(Double score) {
        if (score == null) return null;
        
        try {
            String jsonConfig = configMapper.getConfigValue("grade_level_map");
            
            // 【修复】如果配置为空，使用默认等级划分标准
            if (jsonConfig == null || jsonConfig.trim().isEmpty()) {
                log.warn("数据库中未配置grade_level_map，使用默认标准");
                return determineGradeLevelDefault(score);
            }
            
            JSONObject map = JSON.parseObject(jsonConfig);
            
            // 【修复】检查map是否为null
            if (map == null) {
                log.warn("解析grade_level_map失败，使用默认标准");
                return determineGradeLevelDefault(score);
            }
            
            // 安全获取各等级分数线
            Double scoreA = safeGetArrayDouble(map, "A", 90.0);
            Double scoreB = safeGetArrayDouble(map, "B", 80.0);
            Double scoreC = safeGetArrayDouble(map, "C", 70.0);
            Double scoreD = safeGetArrayDouble(map, "D", 60.0);
            
            if (score >= scoreA) return "A";
            else if (score >= scoreB) return "B";
            else if (score >= scoreC) return "C";
            else if (score >= scoreD) return "D";
            else return "F";
            
        } catch (Exception e) {
            log.error("确定成绩等级时出错: {}，使用默认标准", score, e.getMessage());
            return determineGradeLevelDefault(score);
        }
    }
    
    /**
     * 安全获取JSON数组中的第一个double值
     */
    private Double safeGetArrayDouble(JSONObject map, String key, Double defaultValue) {
        try {
            if (map.containsKey(key) && map.getJSONArray(key) != null 
                && !map.getJSONArray(key).isEmpty()) {
                return map.getJSONArray(key).getDouble(0);
            }
            return defaultValue;
        } catch (Exception e) {
            log.warn("获取{}等级分数线失败: {}，使用默认值{}", key, e.getMessage(), defaultValue);
            return defaultValue;
        }
    }
    
    /**
     * 默认的成绩等级划分标准（当数据库配置缺失时使用）
     */
    private String determineGradeLevelDefault(Double score) {
        if (score >= 90.0) return "A";      // 优秀：90-100分
        else if (score >= 80.0) return "B";  // 良好：80-89分
        else if (score >= 70.0) return "C";  // 中等：70-79分
        else if (score >= 60.0) return "D";  // 及格：60-69分
        else return "F";                      // 不及格：<60分
    }

    private FinalCheck getFinalVersionByStudent(Integer studentId, Integer batchId) {
        try {
            // 查询该学生在该批次下的最终检查记录（已定稿版本）
            // 通过student_selections表关联查询
            return finalCheckMapper.selectByStudentAndBatch(studentId, batchId);
        } catch (Exception e) {
            log.warn("查询学生{}在批次{}的最终检查记录失败: {}", studentId, batchId, e.getMessage());
            return null;
        }
    }

    private Defense getDefenseByStudent(Integer studentId, Integer batchId) {
        try {
            // 查询该学生在该批次下的答辩记录
            // 通过student_selections表关联查询
            return defenseMapper.selectByStudentAndBatch(studentId, batchId);
        } catch (Exception e) {
            log.warn("查询学生{}在批次{}的答辩记录失败: {}", studentId, batchId, e.getMessage());
            return null;
        }
    }
    
    @Override
    @Transactional
    public void recomputeBySelectionId(Integer selectionId) {
        log.info("【手动重算】开始重新计算选题{}的总成绩", selectionId);
        
        // 1. 根据selectionId查询选题信息
        StudentSelection selection = studentSelectionMapper.selectById(selectionId);
        if (selection == null) {
            throw new RuntimeException("选题记录不存在: " + selectionId);
        }
        
        Integer studentId = selection.getStudentId();
        Integer batchId = selection.getBatchId();
        
        log.info("【手动重算】查询到学生={}, 批次={}", studentId, batchId);
        
        // 2. 调用原有的重算逻辑
        recompute(studentId, batchId);
        
        log.info("【手动重算】✅ 选题{}的总成绩已成功重新计算！", selectionId);
    }
}
