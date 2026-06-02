package com.internship.service.impl;

import com.alibaba.fastjson2.JSON;
import com.alibaba.fastjson2.JSONObject;
import com.internship.entity.*;
import com.internship.mapper.FinalCheckMapper;
import com.internship.mapper.DefenseMapper;
import com.internship.mapper.FinalScoreMapper;
import com.internship.mapper.ProjectBatchMapper;
import com.internship.mapper.SystemConfigMapper;
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
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void recompute(Integer studentId, Integer batchId) {
        ProjectBatch batch = batchMapper.selectById(batchId);
        if (batch == null) {
            throw new RuntimeException("批次不存在");
        }
        
        double defenseRatio = batch.getDefenseRatio().doubleValue();
        double reportRatio = batch.getReportRatio().doubleValue();
        
        // 获取最终报告定稿版本的分数
        FinalCheck finalCheck = getFinalVersionByStudent(studentId, batchId);
        Double reportScore = (finalCheck != null) ? finalCheck.getReportScore().doubleValue() : null;
        
        // 获取答辩分数（五级制转数值）
        Defense defense = getDefenseByStudent(studentId, batchId);
        Double defenseScore = (defense != null) ? 
            (defense.getDefenseScoreNum() != null ? defense.getDefenseScoreNum().doubleValue() : convertLevelToNum(defense.getDefenseScore())) : null;
        
        // 计算总分
        Double totalScore = null;
        if (reportScore != null && defenseScore != null) {
            totalScore = reportScore * reportRatio + defenseScore * defenseRatio;
            totalScore = Math.round(totalScore * 100.0) / 100.0;  // 保留2位小数
        }
        
        String gradeLevel = determineGradeLevel(totalScore);
        
        // 查询学生当前有效选题
        StudentSelection selection = null; // TODO: 实现根据studentId和batchId查询选题的逻辑
        
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
        
        String jsonConfig = configMapper.getConfigValue("defense_score_map");
        JSONObject map = JSON.parseObject(jsonConfig);
        
        return map.getDouble(level);  // 返回如 95/85/75/65/50
    }

    @Override
    public String determineGradeLevel(Double score) {
        if (score == null) return null;
        
        String jsonConfig = configMapper.getConfigValue("grade_level_map");
        JSONObject map = JSON.parseObject(jsonConfig);
        
        if (score >= map.getJSONArray("A").getDouble(0)) return "A";
        else if (score >= map.getJSONArray("B").getDouble(0)) return "B";
        else if (score >= map.getJSONArray("C").getDouble(0)) return "C";
        else if (score >= map.getJSONArray("D").getDouble(0)) return "D";
        else return "F";
    }

    private FinalCheck getFinalVersionByStudent(Integer studentId, Integer batchId) {
        // 实现查询逻辑
        return null;
    }

    private Defense getDefenseByStudent(Integer studentId, Integer batchId) {
        // 实现查询逻辑
        return null;
    }
}
