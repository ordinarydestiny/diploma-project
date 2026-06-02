package com.internship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.common.Result;
import com.internship.entity.College;
import com.internship.entity.Major;
import com.internship.mapper.CollegeMapper;
import com.internship.mapper.MajorMapper;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/colleges")
@RequiredArgsConstructor
@Tag(name = "学院与专业管理")
public class CollegeController {

    private final CollegeMapper collegeMapper;
    private final MajorMapper majorMapper;

    @GetMapping
    @Operation(summary = "获取所有学院")
    public Result<List<College>> listColleges() {
        return Result.success(collegeMapper.selectList(null));
    }

    @GetMapping("/{collegeId}/majors")
    @Operation(summary = "获取学院下的专业列表")
    public Result<List<Major>> listMajors(@PathVariable Integer collegeId) {
        return Result.success(
            majorMapper.selectList(
                new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Major>()
                    .eq(Major::getCollegeId, collegeId)
            )
        );
    }

    @GetMapping("/major/{majorId}")
    @Operation(summary = "获取专业详情")
    public Result<Major> getMajorDetail(@PathVariable Integer majorId) {
        return Result.success(majorMapper.selectById(majorId));
    }
}
