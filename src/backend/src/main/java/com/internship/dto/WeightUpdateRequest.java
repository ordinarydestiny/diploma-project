package com.internship.dto;

import jakarta.validation.constraints.DecimalMax;
import jakarta.validation.constraints.DecimalMin;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

import java.math.BigDecimal;

@Data
public class WeightUpdateRequest {

    @NotNull(message = "签到占比不能为空")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal weightCheckin;

    @NotNull(message = "日报占比不能为空")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal weightDailyReport;

    @NotNull(message = "周报占比不能为空")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal weightWeeklyReport;

    @NotNull(message = "月报占比不能为空")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal weightMonthlyReport;

    @NotNull(message = "校内考核占比不能为空")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal weightInnerAssessment;

    @NotNull(message = "校外考核占比不能为空")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal weightOuterAssessment;

    @NotNull(message = "实习报告占比不能为空")
    @DecimalMin(value = "0.00")
    @DecimalMax(value = "100.00")
    private BigDecimal weightReport;
}
