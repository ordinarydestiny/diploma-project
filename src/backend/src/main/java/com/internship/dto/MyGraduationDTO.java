package com.internship.dto;

import lombok.Data;
import java.math.BigDecimal;
import java.time.LocalDateTime;
import java.util.List;

/**
 * 学生个人毕设信息聚合DTO
 * 一次性返回学生的批次、选题、任务书、中期检查、最终检查、答辩等全部信息
 */
@Data
public class MyGraduationDTO {
    
    /**
     * 批次信息内部类
     */
    @Data
    public static class BatchInfo {
        private Integer batchId;
        private String batchName;
        private String batchCode;
        private String semester;
        private String startDate;
        private String endDate;
        private String status; // draft/active/finished
        private String currentPhase; // preparation/selection/taskbook/midterm/final/defense/finished
        private String description;
        private BigDecimal defenseRatio; // 答辩成绩占比
        private BigDecimal reportRatio; // 报告成绩占比
        private String activatedAt;
        private String finishedAt;
        
        // 扩展信息（关联查询）
        private Integer majorId;
        private String majorName;
        private String collegeName;
        private String className; // 班级名称
        private String teacherName; // 指导老师姓名
        private Integer teacherId; // 指导老师ID
        
        // 时间线
        private List<TimelineItem> timeline;
    }
    
    /**
     * 时间线条目
     */
    @Data
    public static class TimelineItem {
        private String date;
        private String title;
        private String description;
        private String type; // primary/success/warning/danger/info
    }
    
    /**
     * 题目信息内部类
     */
    @Data
    public static class TopicInfo {
        private Integer topicId;
        private String topicName;
        private String description;
        private Integer difficulty; // 1-5星
        private String category; // 分类标签
        private String topicType; // research/engineering/thesis
        private String source; // teacher/student/enterprise
        private String requirements; // 技术要求
        private String references; // 参考文献
        private Integer selectionCount; // 已选次数
        private Integer maxStudents; // 最大允许人数
        private String status; // available/unavailable/archived
        
        // 选题相关信息
        private Integer selectionId;
        private String selectionStatus; // pending/approved/rejected/cancelled
        private String topicTypeSelection; // library/self (选题类型)
        private String selfTopicName; // 自主命题名称
        private String selfTopicDesc; // 自主命题描述
        private Integer version; // 选题版本号
        private String selectTime; // 选题时间
        private String reviewTime; // 审核时间
        private String reviewComment; // 审核意见
        private Integer reviewerId; // 审核人ID
        private String reviewerName; // 审核人姓名
    }
    
    /**
     * 任务书信息内部类
     */
    @Data
    public static class TaskBookInfo {
        private Integer taskId;
        private Integer selectionId;
        private String content; // 主要任务描述
        private String deadline; // 完成期限
        private Integer version; // 版本号
        private String requirementsJson; // 基本要求JSON
        private String techParamsJson; // 技术参数JSON
        private String references; // 参考资料
        private String status; // unissued/issued/confirmed/rejected
        private Integer issuerId; // 下达人ID
        private String issuerName; // 下达人姓名
        private String issuedAt; // 下达时间
        private Integer confirmBy; // 确认人ID
        private String confirmAt; // 确认时间
        private Integer rejectorId; // 驳回人ID
        private String rejectTime; // 驳回时间
        private String rejectComment; // 驳回原因
        
        // 解析后的列表数据
        private List<RequirementItem> requirementList;
        private List<TechParamItem> techParamList;
        private List<String> referenceList;
    }
    
    /**
     * 基本要求项
     */
    @Data
    public static class RequirementItem {
        private Integer id;
        private String text;
    }
    
    /**
     * 技术参数项
     */
    @Data
    public static class TechParamItem {
        private String name;
        private String value;
        private String note;
    }
    
    /**
     * 中期检查信息内部类
     */
    @Data
    public static class MidtermInfo {
        private Integer checkId;
        private Integer selectionId;
        private Integer fileId; // 中期报告文件ID
        private String fileName; // 文件名
        private String submitTime; // 提交时间
        private Integer version; // 版本号
        private Boolean isCurrent; // 是否当前版本
        private Integer progress; // 完成进度(0-100%)
        private String status; // draft/submitted/pending/approved/rejected
        private Integer reviewerId; // 审核老师ID
        private String reviewerName; // 审核老师姓名
        private String reviewTime; // 审核时间
        private String reviewComment; // 教师审核意见
        private Integer guideFileId; // 教师批阅附件ID
        
        // 历史版本列表
        private List<MidtermVersion> versions;
    }
    
    /**
     * 中期检查版本
     */
    @Data
    public static class MidtermVersion {
        private Integer checkId;
        private Integer version;
        private String submitTime;
        private Integer progress;
        private String status;
        private String summary; // 报告摘要（从comment提取或单独字段）
        private String comment; // 老师反馈
    }
    
    /**
     * 最终检查信息内部类
     */
    @Data
    public static class FinalInfo {
        private Integer checkId;
        private Integer selectionId;
        private Integer fileId; // 最终报告文件ID
        private String fileName; // 文件名
        private String submitTime; // 提交时间
        private Integer version; // 版本号
        private Boolean isCurrent; // 是否当前版本
        private Boolean isFinal; // 是否定稿版本
        private String finalizedAt; // 定稿时间
        private String status; // pending/approved/rejected
        private Integer reviewerId; // 审核老师ID
        private String reviewerName; // 审核老师姓名
        private String reviewTime; // 审核时间
        private String reviewComment; // 教师评语（存档用）
        private Integer reportScore; // 报告分数(0-100)
        private Integer guideFileId; // 教师批阅附件ID
        
        // 历史版本列表
        private List<FinalVersion> history;
    }
    
    /**
     * 最终检查版本
     */
    @Data
    public static class FinalVersion {
        private Integer checkId;
        private Integer version;
        private String submitTime;
        private String action; // 首次提交/通过并定稿/驳回
        private String comment; // 老师反馈
        private Integer score; // 分数
    }
    
    /**
     * 答辩信息内部类
     */
    @Data
    public static class DefenseInfo {
        private Integer defenseId;
        private Integer selectionId;
        private String defenseScore; // 五级制 excellent/good/medium/pass/fail
        private BigDecimal defenseScoreNum; // 数值分数
        private Integer recordFileId; // 答辩记录文件ID
        private String recordFileName; // 文件名
        private String submitterType; // student/teacher
        private Integer submitterId; // 提交人ID
        private String submitTime; // 提交时间
        private String defenseDatetime; // 答辩日期时间
        private String location; // 答辩地点
        private String committee; // 答辩委员会成员
        private String status; // not_started/submitted/pending/approved/rejected
        private Integer reviewerId; // 审核人ID
        private String reviewerName; // 审核人姓名
        private String reviewTime; // 审核时间
        private String reviewComment; // 答辩评语
    }
    
    /**
     * 成绩信息内部类
     */
    @Data
    public static class ScoreInfo {
        private Integer scoreId;
        private Integer selectionId;
        private Integer reportScore; // 报告分数
        private Integer defenseScore; // 答辩数值分数
        private BigDecimal totalScore; // 总成绩
        private String gradeLevel; // A/B/C/D/F
        private Boolean isPublished; // 是否公布
        private Boolean isPartialScore; // 是否为部分成绩（报告或答辩有一项未完成）
        private String publishedAt; // 公布时间
        private String calculatedAt; // 计算时间
    }
    
    // ==================== 主要字段 ====================
    
    private BatchInfo batchInfo; // 批次信息
    private TopicInfo topicInfo; // 题目信息
    private TaskBookInfo taskBookInfo; // 任务书信息
    private MidtermInfo midtermInfo; // 中期检查信息
    private FinalInfo finalInfo; // 最终检查信息
    private DefenseInfo defenseInfo; // 答辩信息
    private ScoreInfo scoreInfo; // 成绩信息
    
    // 用户基础信息
    private Integer userId;
    private String username;
    private String realName;
    private String roleName; // student/teacher等
    private String collegeName;
    private String majorName;
    private String className;
    
    /**
     * 是否有完整毕设流程数据
     */
    public boolean hasCompleteData() {
        return batchInfo != null && topicInfo != null && "approved".equals(topicInfo.getSelectionStatus());
    }
}
