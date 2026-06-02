package com.internship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.internship.entity.Defense;
import com.internship.mapper.DefenseMapper;
import com.internship.service.DefenseService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.math.BigDecimal;
import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class DefenseServiceImpl extends ServiceImpl<DefenseMapper, Defense> implements DefenseService {

    private final DefenseMapper defenseMapper;

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
    public void reviewDefense(Integer defenseId, String status, String comment) {
        Defense defense = getById(defenseId);
        if (defense == null) {
            throw new RuntimeException("答辩记录不存在");
        }
        
        defense.setStatus(status);
        defense.setReviewTime(LocalDateTime.now());
        defense.setReviewComment(comment);
        defense.setUpdatedAt(LocalDateTime.now());
        
        updateById(defense);
        log.info("审核答辩记录{}：状态={}", defenseId, status);
    }
}