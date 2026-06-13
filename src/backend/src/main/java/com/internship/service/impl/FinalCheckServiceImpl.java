package com.internship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.internship.entity.FinalCheck;
import com.internship.entity.ProjectBatch;
import com.internship.entity.StudentSelection;
import com.internship.mapper.FinalCheckMapper;
import com.internship.mapper.ProjectBatchMapper;
import com.internship.mapper.StudentSelectionMapper;
import com.internship.service.FinalCheckService;
import com.internship.service.ScoreComputationService;
import com.internship.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class FinalCheckServiceImpl extends ServiceImpl<FinalCheckMapper, FinalCheck> implements FinalCheckService {

    private final ScoreComputationService scoreComputationService;
    private final ProjectBatchMapper batchMapper;
    private final FinalCheckMapper finalCheckMapper;
    private final StudentSelectionMapper studentSelectionMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public void submitFinalCheck(Integer selectionId, Integer fileId) {
        Long studentId = jwtUtil.getCurrentUserId();
        
        LambdaQueryWrapper<FinalCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalCheck::getSelectionId, selectionId)
               .eq(FinalCheck::getIsCurrent, true);
        
        FinalCheck currentCheck = getOne(wrapper);
        if (currentCheck != null && !"rejected".equals(currentCheck.getStatus())) {
            throw new RuntimeException("已有待审核或已通过的最终报告，请先处理当前版本");
        }
        
        if (currentCheck != null) {
            currentCheck.setIsCurrent(false);
            updateById(currentCheck);
        }
        
        Integer maxVersion = lambdaQuery()
                .eq(FinalCheck::getSelectionId, selectionId)
                .orderByDesc(FinalCheck::getVersion)
                .one() != null ? 
                lambdaQuery().eq(FinalCheck::getSelectionId, selectionId).orderByDesc(FinalCheck::getVersion).one().getVersion() + 1 : 1;
        
        FinalCheck finalCheck = new FinalCheck();
        finalCheck.setSelectionId(selectionId);
        finalCheck.setFileId(fileId);
        finalCheck.setSubmitTime(java.time.LocalDateTime.now());
        finalCheck.setVersion(maxVersion);
        finalCheck.setIsCurrent(true);
        finalCheck.setIsFinal(false);  // 初始不是定稿
        finalCheck.setStatus("pending");
        
        save(finalCheck);
        log.info("学生{}提交最终报告，选题={}，版本={}", studentId, selectionId, maxVersion);
    }

    @Override
    @Transactional
    public void reviewFinalCheck(Integer checkId, String status, String comment, Integer score) {
        FinalCheck finalCheck = getById(checkId);
        if (finalCheck == null) {
            throw new RuntimeException("最终检查记录不存在");
        }
        
        // 验证：通过并定稿时必须填写分数
        if ("approved".equals(status) && (score == null || score < 0 || score > 100)) {
            throw new RuntimeException("通过并定稿时必须填写报告成绩（0-100分）");
        }
        
        Long reviewerId = jwtUtil.getCurrentUserId();
        finalCheck.setStatus(status);
        finalCheck.setReviewerId(reviewerId != null ? reviewerId.intValue() : null);
        finalCheck.setReviewTime(java.time.LocalDateTime.now());
        finalCheck.setReviewComment(comment);
        
        // 只有通过时才设置分数，驳回时不设置
        if ("approved".equals(status)) {
            finalCheck.setReportScore(score);
        } else {
            finalCheck.setReportScore(null);  // 驳回时清空分数
        }
        
        if ("approved".equals(status)) {
            boolean hasExistingFinal = finalCheckMapper.existsBySelectionAndFinal(finalCheck.getSelectionId()) > 0;
            
            if (!hasExistingFinal) {
                finalCheck.setIsFinal(true);           // 首次通过 → 设为定稿
                finalCheck.setFinalizedAt(java.time.LocalDateTime.now());  // 记录定稿时间
                log.info("选题{}的最终报告已定稿！版本={}, 分数={}", finalCheck.getSelectionId(), finalCheck.getVersion(), score);
            } else {
                finalCheck.setIsFinal(false);          // 已有定稿 → 本次仅作为普通通过记录
                log.warn("选题{}已有定稿，本次通过不会覆盖定稿", finalCheck.getSelectionId());
            }
        }
        
        updateById(finalCheck);
        
        if ("approved".equals(status)) {
            StudentSelection selection = studentSelectionMapper.selectById(finalCheck.getSelectionId());
            ProjectBatch batch = batchMapper.selectById(selection.getBatchId());
            scoreComputationService.recompute(finalCheck.getSelectionId(), batch.getBatchId());
        }
    }

    @Override
    public FinalCheck getFinalVersion(Integer selectionId) {
        LambdaQueryWrapper<FinalCheck> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(FinalCheck::getSelectionId, selectionId)
               .eq(FinalCheck::getIsFinal, true);
        return getOne(wrapper);
    }
}
