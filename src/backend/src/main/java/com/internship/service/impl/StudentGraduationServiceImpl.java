package com.internship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.dto.MyGraduationDTO;
import com.internship.entity.*;
import com.internship.mapper.*;
import com.internship.service.StudentGraduationService;
import com.internship.service.ScoreComputationService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;

import java.time.format.DateTimeFormatter;
import java.util.*;
import java.util.stream.Collectors;

/**
 * 学生个人毕设信息查询服务实现
 */
@Slf4j
@Service
@RequiredArgsConstructor
public class StudentGraduationServiceImpl implements StudentGraduationService {
    
    private final UserMapper userMapper;
    private final StudentSelectionMapper studentSelectionMapper;
    private final ProjectBatchMapper projectBatchMapper;
    private final TopicMapper topicMapper;
    private final TaskBookMapper taskBookMapper;
    private final MidtermCheckMapper midtermCheckMapper;
    private final FinalCheckMapper finalCheckMapper;
    private final DefenseMapper defenseMapper;
    private final FinalScoreMapper finalScoreMapper;
    private final MajorMapper majorMapper;
    private final CollegeMapper collegeMapper;
    private final FileMapper fileMapper;
    private final TeacherStudentRelationMapper teacherStudentRelationMapper;
    private final ScoreComputationService scoreComputationService;
    
    private final ObjectMapper objectMapper;
    
    private static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    private static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    @Override
    public MyGraduationDTO getMyGraduationInfo(Integer studentId) {
        MyGraduationDTO dto = new MyGraduationDTO();
        
        // 1. 获取用户基础信息
        User student = userMapper.selectById(studentId);
        if (student == null) {
            log.warn("学生不存在: {}", studentId);
            return dto;
        }
        
        dto.setUserId(student.getUserId());
        dto.setUsername(student.getUsername());
        dto.setRealName(student.getRealName());
        dto.setRoleName(student.getRole());
        dto.setClassName(student.getClassName());
        
        // 查询学院和专业名称
        if (student.getMajorId() != null) {
            Major major = majorMapper.selectById(student.getMajorId());
            if (major != null) {
                dto.setMajorName(major.getMajorName());
                if (major.getCollegeId() != null) {
                    College college = collegeMapper.selectById(major.getCollegeId());
                    if (college != null) {
                        dto.setCollegeName(college.getCollegeName());
                    }
                }
            }
        }
        
        // 2. 查找学生的选题记录（最新的已通过或待审核的）
        StudentSelection selection = getLatestStudentSelection(studentId);
        if (selection == null) {
            log.info("学生 {} 尚无选题记录", studentId);
            return dto; // 返回只有基础信息的DTO
        }
        
        // 3. 获取批次信息
        dto.setBatchInfo(buildBatchInfo(selection, studentId));
        
        // 4. 获取题目信息
        dto.setTopicInfo(buildTopicInfo(selection));
        
        // 5. 获取任务书信息
        dto.setTaskBookInfo(buildTaskBookInfo(selection.getSelectionId()));
        
        // 6. 获取中期检查信息
        dto.setMidtermInfo(buildMidtermInfo(selection.getSelectionId()));
        
        // 7. 获取最终检查信息
        dto.setFinalInfo(buildFinalInfo(selection.getSelectionId()));
        
        // 8. 获取答辩信息
        dto.setDefenseInfo(buildDefenseInfo(selection.getSelectionId()));
        
        // 9. 获取成绩信息
        dto.setScoreInfo(buildScoreInfo(selection.getSelectionId()));
        
        return dto;
    }
    
    /**
     * 获取学生最新的选题记录（优先返回approved状态）
     */
    private StudentSelection getLatestStudentSelection(Integer studentId) {
        List<StudentSelection> selections = studentSelectionMapper.selectList(
            new LambdaQueryWrapper<StudentSelection>()
                .eq(StudentSelection::getStudentId, studentId)
                .in(StudentSelection::getStatus, Arrays.asList("pending", "approved", "rejected"))
                .orderByDesc(StudentSelection::getVersion)
                .orderByDesc(StudentSelection::getCreatedAt)
                .last("LIMIT 5")
        );
        
        if (selections.isEmpty()) {
            return null;
        }
        
        // 优先返回approved状态的
        return selections.stream()
            .filter(s -> "approved".equals(s.getStatus()))
            .findFirst()
            .orElse(selections.get(0));
    }
    
    /**
     * 构建批次信息
     */
    private MyGraduationDTO.BatchInfo buildBatchInfo(StudentSelection selection, Integer studentId) {
        ProjectBatch batch = projectBatchMapper.selectById(selection.getBatchId());
        if (batch == null) return null;
        
        MyGraduationDTO.BatchInfo batchInfo = new MyGraduationDTO.BatchInfo();
        batchInfo.setBatchId(batch.getBatchId());
        batchInfo.setBatchName(batch.getBatchName());
        batchInfo.setBatchCode(batch.getBatchCode());
        batchInfo.setSemester(batch.getSemester());
        batchInfo.setStartDate(batch.getStartDate() != null ? batch.getStartDate().format(DATE_FORMATTER) : null);
        batchInfo.setEndDate(batch.getEndDate() != null ? batch.getEndDate().format(DATE_FORMATTER) : null);
        batchInfo.setStatus(batch.getStatus());
        batchInfo.setCurrentPhase(batch.getCurrentPhase());
        batchInfo.setDescription(batch.getDescription());
        batchInfo.setDefenseRatio(batch.getDefenseRatio());
        batchInfo.setReportRatio(batch.getReportRatio());
        batchInfo.setActivatedAt(batch.getActivatedAt() != null ? batch.getActivatedAt().format(DATETIME_FORMATTER) : null);
        batchInfo.setFinishedAt(batch.getFinishedAt() != null ? batch.getFinishedAt().format(DATETIME_FORMATTER) : null);
        
        // 查询专业信息
        if (batch.getMajorId() != null) {
            Major major = majorMapper.selectById(batch.getMajorId());
            if (major != null) {
                batchInfo.setMajorId(major.getMajorId());
                batchInfo.setMajorName(major.getMajorName());
                if (major.getCollegeId() != null) {
                    College college = collegeMapper.selectById(major.getCollegeId());
                    if (college != null) {
                        batchInfo.setCollegeName(college.getCollegeName());
                    }
                }
            }
        }
        
        // 查询学生班级信息
        User student = userMapper.selectById(studentId);
        if (student != null) {
            batchInfo.setClassName(student.getClassName());
        }
        
        // 查询指导老师信息
        TeacherStudentRelation relation = teacherStudentRelationMapper.selectOne(
            new LambdaQueryWrapper<TeacherStudentRelation>()
                .eq(TeacherStudentRelation::getBatchId, batch.getBatchId())
                .eq(TeacherStudentRelation::getStudentId, studentId)
                .last("LIMIT 1")
        );
        
        if (relation != null && relation.getTeacherId() != null) {
            User teacher = userMapper.selectById(relation.getTeacherId());
            if (teacher != null) {
                batchInfo.setTeacherId(teacher.getUserId());
                batchInfo.setTeacherName(teacher.getRealName());
            }
        }
        
        // 构建时间线
        batchInfo.setTimeline(buildTimeline(batch));
        
        return batchInfo;
    }
    
    /**
     * 构建批次时间线
     */
    private List<MyGraduationDTO.TimelineItem> buildTimeline(ProjectBatch batch) {
        List<MyGraduationDTO.TimelineItem> timeline = new ArrayList<>();
        
        String startDate = batch.getStartDate() != null ? batch.getStartDate().format(DATE_FORMATTER) : "";
        String endDate = batch.getEndDate() != null ? batch.getEndDate().format(DATE_FORMATTER) : "";
        
        timeline.add(createTimelineItem(startDate, "批次启动", "毕业设计工作正式启动，开始选题", "primary"));
        
        // 根据当前阶段动态生成时间线
        String phase = batch.getCurrentPhase();
        if (phase != null) {
            switch (phase) {
                case "preparation":
                    timeline.add(createTimelineItem("", "准备阶段", "等待选题开始", "info"));
                    break;
                case "selection":
                    timeline.add(createTimelineItem("", "选题阶段", "正在进行题目选择", "warning"));
                    break;
                case "taskbook":
                    timeline.add(createTimelineItem("", "任务书下达", "指导老师正在下达任务书", "success"));
                    break;
                case "midterm":
                    timeline.add(createTimelineItem("", "中期检查", "提交中期检查报告", "warning"));
                    break;
                case "final":
                    timeline.add(createTimelineItem("", "最终检查", "提交最终毕设报告", "danger"));
                    break;
                case "defense":
                    timeline.add(createTimelineItem("", "答辩环节", "毕业设计答辩进行中", "danger"));
                    break;
                case "finished":
                    timeline.add(createTimelineItem(endDate, "批次结束", "所有流程已完成", "success"));
                    break;
            }
        }
        
        return timeline;
    }
    
    private MyGraduationDTO.TimelineItem createTimelineItem(String date, String title, String description, String type) {
        MyGraduationDTO.TimelineItem item = new MyGraduationDTO.TimelineItem();
        item.setDate(date);
        item.setTitle(title);
        item.setDescription(description);
        item.setType(type);
        return item;
    }
    
    /**
     * 构建题目信息
     */
    private MyGraduationDTO.TopicInfo buildTopicInfo(StudentSelection selection) {
        MyGraduationDTO.TopicInfo topicInfo = new MyGraduationDTO.TopicInfo();
        
        topicInfo.setSelectionId(selection.getSelectionId());
        topicInfo.setSelectionStatus(selection.getStatus());
        topicInfo.setTopicTypeSelection(selection.getTopicType()); // library/self
        topicInfo.setSelfTopicName(selection.getSelfTopicName());
        topicInfo.setSelfTopicDesc(selection.getSelfTopicDesc());
        topicInfo.setVersion(selection.getVersion());
        topicInfo.setSelectTime(selection.getCreatedAt() != null ? selection.getCreatedAt().format(DATETIME_FORMATTER) : null);
        topicInfo.setReviewTime(selection.getReviewTime() != null ? selection.getReviewTime().format(DATETIME_FORMATTER) : null);
        topicInfo.setReviewComment(selection.getReviewComment());
        topicInfo.setReviewerId(selection.getReviewerId());
        
        // 查询审核人姓名
        if (selection.getReviewerId() != null) {
            User reviewer = userMapper.selectById(selection.getReviewerId());
            if (reviewer != null) {
                topicInfo.setReviewerName(reviewer.getRealName());
            }
        }
        
        // 如果是从题库选择的，查询题目详情
        if ("library".equals(selection.getTopicType()) && selection.getTopicId() != null) {
            Topic topic = topicMapper.selectById(selection.getTopicId());
            if (topic != null) {
                topicInfo.setTopicId(topic.getTopicId());
                topicInfo.setTopicName(topic.getTopicName());
                topicInfo.setDescription(topic.getDescription());
                topicInfo.setDifficulty(topic.getDifficulty());
                topicInfo.setCategory(topic.getCategory());
                topicInfo.setTopicType(topic.getTopicType()); // research/engineering/thesis
                topicInfo.setSource(topic.getSource()); // teacher/student/enterprise
                topicInfo.setRequirements(topic.getRequirements());
                topicInfo.setReferences(topic.getReferences());
                topicInfo.setSelectionCount(topic.getSelectionCount());
                topicInfo.setMaxStudents(topic.getMaxStudents());
                topicInfo.setStatus(topic.getStatus());
            }
        } else if ("self".equals(selection.getTopicType())) {
            // 自主命题
            topicInfo.setTopicName(selection.getSelfTopicName() != null ? selection.getSelfTopicName() : "自主命题");
            topicInfo.setDescription(selection.getSelfTopicDesc());
            topicInfo.setSource("student");
            topicInfo.setStatus("available");
        }
        
        return topicInfo;
    }
    
    /**
     * 构建任务书信息
     */
    private MyGraduationDTO.TaskBookInfo buildTaskBookInfo(Integer selectionId) {
        List<TaskBook> taskBooks = taskBookMapper.selectList(
            new LambdaQueryWrapper<TaskBook>()
                .eq(TaskBook::getSelectionId, selectionId)
                .orderByDesc(TaskBook::getVersion)
                .last("LIMIT 3")
        );
        
        if (taskBooks.isEmpty()) {
            return null;
        }
        
        TaskBook latestTaskBook = taskBooks.get(0);
        MyGraduationDTO.TaskBookInfo taskBookInfo = new MyGraduationDTO.TaskBookInfo();
        
        taskBookInfo.setTaskId(latestTaskBook.getTaskId());
        taskBookInfo.setSelectionId(latestTaskBook.getSelectionId());
        taskBookInfo.setContent(latestTaskBook.getContent());
        taskBookInfo.setDeadline(latestTaskBook.getDeadline() != null ? latestTaskBook.getDeadline().format(DATE_FORMATTER) : null);
        taskBookInfo.setVersion(latestTaskBook.getVersion());
        taskBookInfo.setRequirementsJson(latestTaskBook.getRequirements());
        taskBookInfo.setTechParamsJson(latestTaskBook.getTechParams());
        taskBookInfo.setReferences(latestTaskBook.getReferences());
        taskBookInfo.setStatus(latestTaskBook.getStatus());
        taskBookInfo.setIssuerId(latestTaskBook.getIssuerId());
        taskBookInfo.setIssuedAt(latestTaskBook.getIssuedAt() != null ? latestTaskBook.getIssuedAt().format(DATETIME_FORMATTER) : null);
        taskBookInfo.setConfirmBy(latestTaskBook.getConfirmBy());
        taskBookInfo.setConfirmAt(latestTaskBook.getConfirmAt() != null ? latestTaskBook.getConfirmAt().format(DATETIME_FORMATTER) : null);
        taskBookInfo.setRejectorId(latestTaskBook.getRejectorId());
        taskBookInfo.setRejectTime(latestTaskBook.getRejectTime() != null ? latestTaskBook.getRejectTime().format(DATETIME_FORMATTER) : null);
        taskBookInfo.setRejectComment(latestTaskBook.getRejectComment());
        
        // 查询下达人姓名
        if (latestTaskBook.getIssuerId() != null) {
            User issuer = userMapper.selectById(latestTaskBook.getIssuerId());
            if (issuer != null) {
                taskBookInfo.setIssuerName(issuer.getRealName());
            }
        }
        
        // 解析JSON字段为列表
        try {
            if (latestTaskBook.getRequirements() != null && !latestTaskBook.getRequirements().isEmpty()) {
                List<MyGraduationDTO.RequirementItem> reqList = objectMapper.readValue(
                    latestTaskBook.getRequirements(),
                    new TypeReference<List<MyGraduationDTO.RequirementItem>>() {}
                );
                taskBookInfo.setRequirementList(reqList);
            }
            
            if (latestTaskBook.getTechParams() != null && !latestTaskBook.getTechParams().isEmpty()) {
                List<MyGraduationDTO.TechParamItem> techList = objectMapper.readValue(
                    latestTaskBook.getTechParams(),
                    new TypeReference<List<MyGraduationDTO.TechParamItem>>() {}
                );
                taskBookInfo.setTechParamList(techList);
            }
            
            // 解析参考文献为列表
            if (latestTaskBook.getReferences() != null && !latestTaskBook.getReferences().isEmpty()) {
                List<String> refList = Arrays.asList(latestTaskBook.getReferences().split("\n"))
                    .stream()
                    .filter(s -> s.trim().length() > 0)
                    .collect(Collectors.toList());
                taskBookInfo.setReferenceList(refList);
            }
        } catch (JsonProcessingException e) {
            log.warn("解析任务书JSON字段失败: {}", e.getMessage());
        }
        
        return taskBookInfo;
    }
    
    /**
     * 构建中期检查信息
     */
    private MyGraduationDTO.MidtermInfo buildMidtermInfo(Integer selectionId) {
        List<MidtermCheck> checks = midtermCheckMapper.selectList(
            new LambdaQueryWrapper<MidtermCheck>()
                .eq(MidtermCheck::getSelectionId, selectionId)
                .orderByDesc(MidtermCheck::getVersion)
                .orderByDesc(MidtermCheck::getSubmitTime)
                .last("LIMIT 10")
        );
        
        if (checks.isEmpty()) {
            return null;
        }
        
        MidtermCheck latest = checks.get(0);
        MyGraduationDTO.MidtermInfo midtermInfo = new MyGraduationDTO.MidtermInfo();
        
        midtermInfo.setCheckId(latest.getCheckId());
        midtermInfo.setSelectionId(latest.getSelectionId());
        midtermInfo.setFileId(latest.getFileId());
        midtermInfo.setSubmitTime(latest.getSubmitTime() != null ? latest.getSubmitTime().format(DATETIME_FORMATTER) : null);
        midtermInfo.setVersion(latest.getVersion());
        midtermInfo.setProgress(latest.getProgress());
        midtermInfo.setStatus(latest.getStatus());
        midtermInfo.setReviewerId(latest.getReviewerId());
        midtermInfo.setReviewTime(latest.getReviewTime() != null ? latest.getReviewTime().format(DATETIME_FORMATTER) : null);
        midtermInfo.setReviewComment(latest.getReviewComment());
        midtermInfo.setGuideFileId(latest.getGuideFileId());
        
        // 查询文件名
        if (latest.getFileId() != null) {
            File file = fileMapper.selectById(latest.getFileId());
            if (file != null) {
                midtermInfo.setFileName(file.getOriginalName());
            }
        }
        
        // 查询审核人姓名
        if (latest.getReviewerId() != null) {
            User reviewer = userMapper.selectById(latest.getReviewerId());
            if (reviewer != null) {
                midtermInfo.setReviewerName(reviewer.getRealName());
            }
        }
        
        // 构建历史版本列表（只显示真正提交过的记录，排除草稿）
        List<MyGraduationDTO.MidtermVersion> versions = checks.stream()
            .filter(check -> !"draft".equals(check.getStatus()))  // ✅ 过滤掉未提交的草稿
            .map(check -> {
                MyGraduationDTO.MidtermVersion v = new MyGraduationDTO.MidtermVersion();
                v.setCheckId(check.getCheckId());
                v.setVersion(check.getVersion());
                v.setSubmitTime(check.getSubmitTime() != null ? check.getSubmitTime().format(DATETIME_FORMATTER) : null);
                v.setProgress(check.getProgress());
                v.setStatus(check.getStatus());
                v.setSummary(check.getReviewComment()); // 使用评语作为摘要
                v.setComment(check.getReviewComment());
                return v;
            })
            .collect(Collectors.toList());
        
        midtermInfo.setVersions(versions);
        
        return midtermInfo;
    }
    
    /**
     * 构建最终检查信息
     */
    private MyGraduationDTO.FinalInfo buildFinalInfo(Integer selectionId) {
        List<FinalCheck> checks = finalCheckMapper.selectList(
            new LambdaQueryWrapper<FinalCheck>()
                .eq(FinalCheck::getSelectionId, selectionId)
                .orderByDesc(FinalCheck::getVersion)
                .orderByDesc(FinalCheck::getSubmitTime)
                .last("LIMIT 10")
        );
        
        if (checks.isEmpty()) {
            return null;
        }
        
        FinalCheck latest = checks.get(0);
        MyGraduationDTO.FinalInfo finalInfo = new MyGraduationDTO.FinalInfo();
        
        finalInfo.setCheckId(latest.getCheckId());
        finalInfo.setSelectionId(latest.getSelectionId());
        finalInfo.setFileId(latest.getFileId());
        finalInfo.setSubmitTime(latest.getSubmitTime() != null ? latest.getSubmitTime().format(DATETIME_FORMATTER) : null);
        finalInfo.setVersion(latest.getVersion());
        finalInfo.setFinalizedAt(latest.getFinalizedAt() != null ? latest.getFinalizedAt().format(DATETIME_FORMATTER) : null);
        finalInfo.setStatus(latest.getStatus());
        finalInfo.setReviewerId(latest.getReviewerId());
        finalInfo.setReviewTime(latest.getReviewTime() != null ? latest.getReviewTime().format(DATETIME_FORMATTER) : null);
        finalInfo.setReviewComment(latest.getReviewComment());
        finalInfo.setReportScore(latest.getReportScore());
        finalInfo.setGuideFileId(latest.getGuideFileId());
        
        // 查询文件名
        if (latest.getFileId() != null) {
            File file = fileMapper.selectById(latest.getFileId());
            if (file != null) {
                finalInfo.setFileName(file.getOriginalName());
            }
        }
        
        // 查询审核人姓名
        if (latest.getReviewerId() != null) {
            User reviewer = userMapper.selectById(latest.getReviewerId());
            if (reviewer != null) {
                finalInfo.setReviewerName(reviewer.getRealName());
            }
        }
        
        // 构建历史版本列表（每条通过的记录显示两个时间点）
        List<MyGraduationDTO.FinalVersion> history = new java.util.ArrayList<>();
        
        log.info("【最终检查】开始构建历史版本列表，共 {} 条记录", checks.size());
        
        for (int i = 0; i < checks.size(); i++) {
            FinalCheck check = checks.get(i);
            log.info("【最终检查】处理第{}条: status={}, version={}, isFinal={}, submitTime={}, reviewTime={}", 
                i + 1, check.getStatus(), check.getVersion(), check.getIsFinal(),
                check.getSubmitTime(), check.getReviewTime());
            
            MyGraduationDTO.FinalVersion submitRecord = new MyGraduationDTO.FinalVersion();
            submitRecord.setCheckId(check.getCheckId());
            submitRecord.setVersion(check.getVersion());
            submitRecord.setSubmitTime(check.getSubmitTime() != null ? check.getSubmitTime().format(DATETIME_FORMATTER) : null);
            
            // 判断操作类型并设置action（放宽条件：只要是approved状态就生成两条）
            if ("approved".equals(check.getStatus())) {
                // 已通过/已定稿的记录：先添加提交记录，再添加通过记录
                log.info("【最终检查】检测到已通过状态，将生成2条历史记录");
                
                submitRecord.setAction("首次提交");
                submitRecord.setComment(null);  // 提交时无评语
                history.add(submitRecord);
                
                // 添加审核通过记录
                MyGraduationDTO.FinalVersion approveRecord = new MyGraduationDTO.FinalVersion();
                approveRecord.setCheckId(check.getCheckId());
                approveRecord.setVersion(check.getVersion());
                
                // 优先使用reviewTime，其次使用finalizedAt，最后使用当前时间
                String approveTime = null;
                if (check.getReviewTime() != null) {
                    approveTime = check.getReviewTime().format(DATETIME_FORMATTER);
                } else if (check.getFinalizedAt() != null) {
                    approveTime = check.getFinalizedAt().format(DATETIME_FORMATTER);
                } else {
                    approveTime = java.time.LocalDateTime.now().format(DATETIME_FORMATTER);
                    log.warn("【最终检查】recordId={} 的reviewTime和finalizedAt都为空，使用当前时间", check.getCheckId());
                }
                
                approveRecord.setSubmitTime(approveTime);
                approveRecord.setAction("提交通过");
                approveRecord.setComment(check.getReviewComment() != null ? check.getReviewComment() : "教师评语待补充");
                approveRecord.setScore(check.getReportScore());
                history.add(approveRecord);
                
                log.info("【最终检查】已添加审核通过记录: time={}, comment={}", approveTime, approveRecord.getComment());
                
            } else if ("rejected".equals(check.getStatus())) {
                // 被驳回的记录
                submitRecord.setAction("首次提交");
                submitRecord.setComment(check.getReviewComment());
                history.add(submitRecord);
                
                // 添加驳回记录
                MyGraduationDTO.FinalVersion rejectRecord = new MyGraduationDTO.FinalVersion();
                rejectRecord.setCheckId(check.getCheckId());
                rejectRecord.setVersion(check.getVersion());
                rejectRecord.setSubmitTime(check.getReviewTime() != null ? check.getReviewTime().format(DATETIME_FORMATTER) : null);
                rejectRecord.setAction("被驳回");
                rejectRecord.setComment(check.getReviewComment());
                history.add(rejectRecord);
            } else if ("pending".equals(check.getStatus())) {
                // 待审核的记录
                submitRecord.setAction("首次提交");
                submitRecord.setComment(null);
                history.add(submitRecord);
            } else {
                // 其他状态（draft等）
                submitRecord.setAction("首次提交");
                submitRecord.setComment(check.getReviewComment());
                history.add(submitRecord);
            }
        }
        
        finalInfo.setHistory(history);
        
        return finalInfo;
    }
    
    /**
     * 构建答辩信息
     */
    private MyGraduationDTO.DefenseInfo buildDefenseInfo(Integer selectionId) {
        Defense defense = defenseMapper.selectOne(
            new LambdaQueryWrapper<Defense>()
                .eq(Defense::getSelectionId, selectionId)
                .orderByDesc(Defense::getDefenseId)
                .last("LIMIT 1")
        );
        
        if (defense == null) {
            return null;
        }
        
        MyGraduationDTO.DefenseInfo defenseInfo = new MyGraduationDTO.DefenseInfo();
        
        defenseInfo.setDefenseId(defense.getDefenseId());
        defenseInfo.setSelectionId(defense.getSelectionId());
        defenseInfo.setDefenseScore(defense.getDefenseScore());
        defenseInfo.setDefenseScoreNum(defense.getDefenseScoreNum());
        defenseInfo.setRecordFileId(defense.getRecordFileId());
        defenseInfo.setSubmitterType(defense.getSubmitterType());
        defenseInfo.setSubmitterId(defense.getSubmitterId());
        defenseInfo.setSubmitTime(defense.getSubmitTime() != null ? defense.getSubmitTime().format(DATETIME_FORMATTER) : null);
        defenseInfo.setDefenseDatetime(defense.getDefenseDatetime() != null ? defense.getDefenseDatetime().format(DATETIME_FORMATTER) : null);
        defenseInfo.setLocation(defense.getLocation());
        defenseInfo.setCommittee(defense.getCommittee());
        defenseInfo.setStatus(defense.getStatus());
        defenseInfo.setReviewerId(defense.getReviewerId());
        defenseInfo.setReviewTime(defense.getReviewTime() != null ? defense.getReviewTime().format(DATETIME_FORMATTER) : null);
        defenseInfo.setReviewComment(defense.getReviewComment());
        
        // 查询文件名
        if (defense.getRecordFileId() != null) {
            File file = fileMapper.selectById(defense.getRecordFileId());
            if (file != null) {
                defenseInfo.setRecordFileName(file.getOriginalName());
            }
        }
        
        // 查询审核人姓名
        if (defense.getReviewerId() != null) {
            User reviewer = userMapper.selectById(defense.getReviewerId());
            if (reviewer != null) {
                defenseInfo.setReviewerName(reviewer.getRealName());
            }
        }
        
        return defenseInfo;
    }
    
    /**
     * 构建成绩信息（实时计算总成绩）
     */
    private MyGraduationDTO.ScoreInfo buildScoreInfo(Integer selectionId) {
        // 1. 查询final_scores表中的记录（如果存在）
        FinalScore score = finalScoreMapper.selectOne(
            new LambdaQueryWrapper<FinalScore>()
                .eq(FinalScore::getSelectionId, selectionId)
                .last("LIMIT 1")
        );
        
        MyGraduationDTO.ScoreInfo scoreInfo = new MyGraduationDTO.ScoreInfo();
        
        if (score != null) {
            scoreInfo.setScoreId(score.getScoreId());
            scoreInfo.setSelectionId(score.getSelectionId());
            scoreInfo.setReportScore(score.getReportScore());
            scoreInfo.setDefenseScore(score.getDefenseScore());
            scoreInfo.setPublishedAt(score.getPublishedAt() != null ? score.getPublishedAt().format(DATETIME_FORMATTER) : null);
            scoreInfo.setCalculatedAt(score.getCalculatedAt() != null ? score.getCalculatedAt().format(DATETIME_FORMATTER) : null);
        }
        
        // 2. 【重要】实时查询并计算总成绩（不再依赖数据库缓存值）
        try {
            // 通过selectionId查询学生选题信息
            StudentSelection selection = studentSelectionMapper.selectById(selectionId);
            
            if (selection != null && selection.getStudentId() != null && selection.getBatchId() != null) {
                Integer studentId = selection.getStudentId();
                Integer batchId = selection.getBatchId();
                
                log.info("【实时计算】开始计算学生{}在批次{}的总成绩 (selectionId={})", 
                    studentId, batchId, selectionId);
                
                // 【重要】从数据库读取该批次的真实权重配置
                double reportRatio = 0.6;   // 默认报告占比60%
                double defenseRatio = 0.4;  // 默认答辩占比40%
                
                try {
                    ProjectBatch batch = projectBatchMapper.selectById(batchId);
                    if (batch != null) {
                        if (batch.getReportRatio() != null) {
                            reportRatio = batch.getReportRatio().doubleValue();
                            log.info("【权重配置】读取到批次{}的报告占比: {}%", batchId, reportRatio * 100);
                        }
                        if (batch.getDefenseRatio() != null) {
                            // 使用defense_ratio字段
                            defenseRatio = batch.getDefenseRatio().doubleValue();
                            log.info("【权重配置】读取到批次{}的答辩占比: {}%", batchId, defenseRatio * 100);
                        }
                        
                        log.info("【权重配置】✅ 批次{}最终使用权重: 报告={}%, 答辩={}%", 
                            batchId, reportRatio * 100, defenseRatio * 100);
                    } else {
                        log.warn("【权重配置】未找到批次{}的配置信息，使用默认值: 报告={}%, 答辩={}%", 
                            batchId, reportRatio * 100, defenseRatio * 100);
                    }
                } catch (Exception e) {
                    log.error("【权重配置】❌ 读取批次{}权重配置失败，使用默认值: {}", batchId, e.getMessage());
                }
                
                // 查询最终检查报告成绩
                FinalCheck finalCheck = finalCheckMapper.selectByStudentAndBatch(studentId, batchId);
                Double reportScore = (finalCheck != null && finalCheck.getIsFinal() && finalCheck.getReportScore() != null) ? 
                    finalCheck.getReportScore().doubleValue() : null;
                
                // 查询答辩成绩
                Defense defense = defenseMapper.selectByStudentAndBatch(studentId, batchId);
                Double defenseScore = (defense != null && defense.getDefenseScoreNum() != null) ?
                    defense.getDefenseScoreNum().doubleValue() : null;
                
                // 【重要】实时计算总成绩（传入真实的权重配置）
                Double totalScore = calculateTotalScoreRealtime(reportScore, defenseScore, reportRatio, defenseRatio);
                String gradeLevel = determineGradeLevelRealtime(totalScore);
                boolean isPartialScore = (reportScore == null || reportScore == 0) != (defenseScore == null || defenseScore == 0);
                
                // 设置到返回对象中
                scoreInfo.setTotalScore(totalScore != null ? java.math.BigDecimal.valueOf(totalScore) : null);  // ✅ 使用实时计算的值
                scoreInfo.setGradeLevel(gradeLevel);   // ✅ 使用实时判定的等级
                scoreInfo.setIsPartialScore(isPartialScore);  // ✅ 标记是否为部分成绩
                
                log.info("【实时计算】✅ 学生{}总成绩: 报告={} 答辩={} 总分={} 等级={}", 
                    studentId, reportScore, defenseScore, totalScore, gradeLevel);
                
                // 【可选】同步更新到final_scores表（确保下次读取也是最新值）
                if (score == null) {
                    // 如果final_scores表中没有记录，创建一条
                    saveOrUpdateFinalScore(selectionId, reportScore, defenseScore, totalScore, gradeLevel);
                } else if (score.getTotalScore() == null || 
                          Math.abs(score.getTotalScore().doubleValue() - (totalScore != null ? totalScore : 0.0)) > 0.01) {
                    // 如果分数变化超过0.01，更新记录
                    updateFinalScoreRecord(score, reportScore, defenseScore, totalScore, gradeLevel);
                }
            } else {
                log.warn("【实时计算】无法获取选题信息，使用默认值");
                scoreInfo.setTotalScore(score != null ? score.getTotalScore() : null);
                scoreInfo.setGradeLevel(score != null ? score.getGradeLevel() : null);
            }
        } catch (Exception e) {
            log.error("【实时计算】❌ 计算总成绩失败，使用默认值: {}", e.getMessage(), e);
            // 出错时使用原始数据
            scoreInfo.setTotalScore(score != null ? score.getTotalScore() : null);
            scoreInfo.setGradeLevel(score != null ? score.getGradeLevel() : null);
        }
        
        return scoreInfo;
    }
    
    /**
     * 实时计算总成绩（使用真实的批次权重配置）
     * @param reportScore 报告成绩（可为null）
     * @param defenseScore 答辩成绩（可为null）
     * @param reportRatio 报告占比（从数据库读取，如0.6表示60%）
     * @param defenseRatio 答辩占比（从数据库读取，如0.4表示40%）
     * @return 总成绩（保留2位小数），如果都没有成绩则返回null
     */
    private Double calculateTotalScoreRealtime(Double reportScore, Double defenseScore, 
                                               double reportRatio, double defenseRatio) {
        
        log.info("【计算参数】报告成绩={}, 答辩成绩={}, 报告占比={}%, 答辩占比={}%", 
            reportScore, defenseScore, reportRatio * 100, defenseRatio * 100);
        
        if (reportScore != null && defenseScore != null) {
            // 情况1：✅ 报告和答辩都有成绩 → 正常加权计算
            double total = reportScore * reportRatio + defenseScore * defenseRatio;
            log.info("【完整成绩】正常加权: 报告{}×{} + 答辩{}×{} = {}", 
                reportScore, reportRatio, defenseScore, defenseRatio, total);
            return Math.round(total * 100.0) / 100.0;  // 保留2位小数
            
        } else if (defenseScore != null && (reportScore == null || reportScore == 0)) {
            // 情况2：⚠️ 只有答辩成绩（报告未定稿）→ 按实际权重计算
            // 报告按0分计算，总成绩 = 0×reportRatio + 答辩×defenseRatio
            double total = defenseScore * defenseRatio;
            log.warn("【部分成绩】报告未定稿，按权重折算: 0×{} + {}×{} = {}", 
                reportRatio, defenseScore, defenseRatio, total);
            return Math.round(total * 100.0) / 100.0;  // 例如：80 × 0.5 = 40分（如果是2019届50%配置）
            
        } else if (reportScore != null && (defenseScore == null || defenseScore == 0)) {
            // 情况3：⚠️ 只有报告成绩（未答辩）→ 按实际权重计算
            double total = reportScore * reportRatio;
            log.warn("【部分成绩】未答辩，按权重折算: {}×{} + 0×{} = {}", 
                reportScore, reportRatio, defenseRatio, total);
            return Math.round(total * 100.0) / 100.0;  // 例如：90 × 0.6 = 54分
        }
        
        return null;  // 都没有成绩
    }
    
    /**
     * 实时判定成绩等级
     */
    private String determineGradeLevelRealtime(Double totalScore) {
        if (totalScore == null) return "F";
        
        if (totalScore >= 90) return "A";
        if (totalScore >= 80) return "B";
        if (totalScore >= 70) return "C";
        if (totalScore >= 60) return "D";
        return "F";
    }
    
    /**
     * 保存或更新final_scores记录
     */
    private void saveOrUpdateFinalScore(Integer selectionId, Double reportScore, Double defenseScore, 
                                        Double totalScore, String gradeLevel) {
        try {
            FinalScore newScore = new FinalScore();
            newScore.setSelectionId(selectionId);
            newScore.setReportScore(reportScore != null ? reportScore.intValue() : 0);
            newScore.setDefenseScore(defenseScore != null ? defenseScore.intValue() : 0);
            newScore.setTotalScore(totalScore != null ? java.math.BigDecimal.valueOf(totalScore) : java.math.BigDecimal.ZERO);
            newScore.setGradeLevel(gradeLevel);
            newScore.setIsPublished(true);
            newScore.setPublishedAt(java.time.LocalDateTime.now());
            newScore.setCalculatedAt(java.time.LocalDateTime.now());
            
            finalScoreMapper.insert(newScore);
            log.info("【同步】已创建新的总成绩记录: selectionId={}, totalScore={}", selectionId, totalScore);
        } catch (Exception e) {
            log.error("【同步】❌ 创建总成绩记录失败: {}", e.getMessage(), e);
        }
    }
    
    /**
     * 更新现有的final_scores记录
     */
    private void updateFinalScoreRecord(FinalScore existingScore, Double reportScore, Double defenseScore,
                                        Double totalScore, String gradeLevel) {
        try {
            if (reportScore != null) {
                existingScore.setReportScore(reportScore.intValue());
            }
            if (defenseScore != null) {
                existingScore.setDefenseScore(defenseScore.intValue());
            }
            if (totalScore != null) {
                existingScore.setTotalScore(java.math.BigDecimal.valueOf(totalScore));
            }
            existingScore.setGradeLevel(gradeLevel);
            existingScore.setCalculatedAt(java.time.LocalDateTime.now());
            
            finalScoreMapper.updateById(existingScore);
            log.info("【同步】已更新总成绩记录: scoreId={}, totalScore={}", existingScore.getScoreId(), totalScore);
        } catch (Exception e) {
            log.error("【同步】❌ 更新总成绩记录失败: {}", e.getMessage(), e);
        }
    }
}
