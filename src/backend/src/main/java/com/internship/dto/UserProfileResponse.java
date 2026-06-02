package com.internship.dto;

import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.List;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
public class UserProfileResponse {

    private Long userId;

    private String username;

    private String realName;

    private String phone;

    private String email;

    private String currentRoleCode;

    private String currentRoleName;

    private LocalDateTime createTime;

    private List<IdentityInfo> identities;

    @Data
    @Builder
    @NoArgsConstructor
    @AllArgsConstructor
    public static class IdentityInfo {

        private String roleCode;

        private String roleName;

        private String teacherNo;

        private String teacherName;

        private String teacherPhone;

        private String teacherEmail;

        private String teacherIdCard;

        private Integer teacherType;

        private Integer teacherStatus;

        private String teacherRemark;

        private String deptName;

        private List<String> majorNames;

        private String studentNo;

        private String studentName;

        private String studentPhone;

        private String studentEmail;

        private String studentIdCard;

        private String className;

        private String majorName;

        private String gradeName;

        private String gender;

        private String educationLevel;

        private Integer schoolingYear;

        private String homeAddress;

        private String parentPhone;

        private Integer exemptInternship;

        private String exemptReason;

        private String studentRemark;
    }
}
