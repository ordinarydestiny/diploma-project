package com.internship.dto;

import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

/**
 * 学生导入请求DTO
 * 封装将学生纳入实习计划的数据
 */
@Data
public class StudentImportRequest {

    /** 学生ID列表 */
    @NotEmpty(message = "学生列表不能为空")
    private List<Long> studentIds;
}
