package com.internship.service.impl;

import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.dto.SelectionSubmitDTO;
import com.internship.dto.SelectionReviewDTO;
import com.internship.entity.StudentSelection;
import com.internship.entity.Notification;
import com.internship.mapper.NotificationMapper;
import com.internship.mapper.StudentSelectionMapper;
import com.internship.service.SelectionService;
import com.internship.util.JwtUtil;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

@Slf4j
@Service
@RequiredArgsConstructor
public class SelectionServiceImpl extends ServiceImpl<StudentSelectionMapper, StudentSelection> implements SelectionService {

    private final NotificationMapper notificationMapper;
    private final StudentSelectionMapper selectionMapper;
    private final JwtUtil jwtUtil;

    @Override
    @Transactional
    public StudentSelection submitSelection(SelectionSubmitDTO dto) {
        Long studentId = jwtUtil.getCurrentUserId();
        
        Integer maxVersion = selectionMapper.getMaxVersion(dto.getBatchId(), studentId != null ? studentId.intValue() : null);
        int newVersion = (maxVersion == null) ? 1 : maxVersion + 1;
        
        StudentSelection selection = new StudentSelection();
        selection.setBatchId(dto.getBatchId());
        selection.setStudentId(studentId != null ? studentId.intValue() : null);
        selection.setTopicId(dto.getTopicId());
        selection.setTopicType(dto.getTopicType());
        selection.setSelfTopicName(dto.getSelfTopicName());
        selection.setSelfTopicDesc(dto.getSelfTopicDesc());
        selection.setVersion(newVersion);
        selection.setStatus("pending");
        
        save(selection);
        
        sendNotificationToTeacher(studentId.intValue(), "新选题待审核", "学生提交了新的选题，请及时审核。");
        
        log.info("学生{}提交选题，批次={}，版本={}", studentId, dto.getBatchId(), newVersion);
        return selection;
    }

    @Override
    public IPage<StudentSelection> listSelections(Integer page, Integer size, Integer batchId, String status) {
        Page<StudentSelection> pageParam = new Page<>(page, size);
        LambdaQueryWrapper<StudentSelection> wrapper = new LambdaQueryWrapper<>();
        
        if (batchId != null) {
            wrapper.eq(StudentSelection::getBatchId, batchId);
        }
        if (status != null && !status.isEmpty()) {
            wrapper.eq(StudentSelection::getStatus, status);
        }
        
        wrapper.orderByDesc(StudentSelection::getVersion);
        return page(pageParam, wrapper);
    }

    @Override
    public StudentSelection getCurrentSelection(Integer studentId, Integer batchId) {
        LambdaQueryWrapper<StudentSelection> wrapper = new LambdaQueryWrapper<>();
        wrapper.eq(StudentSelection::getBatchId, batchId)
               .eq(StudentSelection::getStudentId, studentId)
               .eq(StudentSelection::getStatus, "approved")
               .orderByDesc(StudentSelection::getVersion)
               .last("LIMIT 1");
        
        return getOne(wrapper);
    }

    @Override
    @Transactional
    public void reviewSelection(Integer selectionId, SelectionReviewDTO dto) {
        StudentSelection selection = getById(selectionId);
        if (selection == null) {
            throw new RuntimeException("选题不存在");
        }
        
        Long reviewerId = jwtUtil.getCurrentUserId();
        selection.setStatus(dto.getStatus());
        selection.setReviewerId(reviewerId != null ? reviewerId.intValue() : null);
        selection.setReviewTime(java.time.LocalDateTime.now());
        selection.setReviewComment(dto.getComment());
        
        updateById(selection);
        
        if ("approved".equals(dto.getStatus())) {
            sendNotificationToStudent(selection.getStudentId(), "选题审核通过", "你的选题已通过审核，请等待导师下达任务书。");
        } else if ("rejected".equals(dto.getStatus())) {
            sendNotificationToStudent(selection.getStudentId(), "选题被驳回", "你的选题已被驳回，原因：" + dto.getComment() + "，请修改后重新提交。");
        }
        
        log.info("教师{}审核选题{}：{}", reviewerId, selectionId, dto.getStatus());
    }

    @Override
    @Transactional
    public void resetSelection(Integer selectionId, String reason) {
        StudentSelection selection = getById(selectionId);
        
        // 重置为待审核状态，让学生可以重新提交或等待教师重新审核
        selection.setStatus("pending");
        selection.setReviewComment(null); // 清空之前的审核意见
        
        updateById(selection);
        
        sendNotificationToStudent(selection.getStudentId(), "选题已重置", 
            "教师/管理员已重置你的选题状态为'待审核'。原因：" + reason);
        
        log.info("重置选题{}状态为待审核，原因：{}", selectionId, reason);
    }

    private void sendNotificationToTeacher(Integer studentId, String title, String content) {
        // 实现通知逻辑
    }

    private void sendNotificationToStudent(Integer studentId, String title, String content) {
        // 实现通知逻辑
    }
}
