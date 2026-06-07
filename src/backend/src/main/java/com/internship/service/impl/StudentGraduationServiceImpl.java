package com.internship.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.internship.dto.MyGraduationDTO;
import com.internship.entity.*;
import com.internship.mapper.*;
import com.internship.service.StudentGraduationService;
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
        
        // 构建历史版本列表
        List<MyGraduationDTO.MidtermVersion> versions = checks.stream()
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
        
        // 构建历史版本列表
        List<MyGraduationDTO.FinalVersion> history = checks.stream()
            .map(check -> {
                MyGraduationDTO.FinalVersion v = new MyGraduationDTO.FinalVersion();
                v.setCheckId(check.getCheckId());
                v.setVersion(check.getVersion());
                v.setSubmitTime(check.getSubmitTime() != null ? check.getSubmitTime().format(DATETIME_FORMATTER) : null);
                
                // 判断操作类型
                if ("approved".equals(check.getStatus()) && check.getIsFinal() != null && check.getIsFinal().equals(1)) {
                    v.setAction("通过并定稿");
                } else if ("rejected".equals(check.getStatus())) {
                    v.setAction("驳回");
                } else if ("pending".equals(check.getStatus())) {
                    v.setAction("待审核");
                } else {
                    v.setAction("首次提交");
                }
                
                v.setComment(check.getReviewComment());
                v.setScore(check.getReportScore());
                return v;
            })
            .collect(Collectors.toList());
        
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
     * 构建成绩信息
     */
    private MyGraduationDTO.ScoreInfo buildScoreInfo(Integer selectionId) {
        FinalScore score = finalScoreMapper.selectOne(
            new LambdaQueryWrapper<FinalScore>()
                .eq(FinalScore::getSelectionId, selectionId)
                .last("LIMIT 1")
        );
        
        if (score == null) {
            return null;
        }
        
        MyGraduationDTO.ScoreInfo scoreInfo = new MyGraduationDTO.ScoreInfo();
        
        scoreInfo.setScoreId(score.getScoreId());
        scoreInfo.setSelectionId(score.getSelectionId());
        scoreInfo.setReportScore(score.getReportScore());
        scoreInfo.setDefenseScore(score.getDefenseScore());
        scoreInfo.setTotalScore(score.getTotalScore());
        scoreInfo.setGradeLevel(score.getGradeLevel());
        scoreInfo.setPublishedAt(score.getPublishedAt() != null ? score.getPublishedAt().format(DATETIME_FORMATTER) : null);
        scoreInfo.setCalculatedAt(score.getCalculatedAt() != null ? score.getCalculatedAt().format(DATETIME_FORMATTER) : null);
        
        return scoreInfo;
    }
}
