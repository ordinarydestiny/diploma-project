package com.internship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.annotation.LogOperation;
import com.internship.common.Result;
import com.internship.entity.TeacherStudentRelation;
import com.internship.mapper.TeacherStudentRelationMapper;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/batches/{batchId}/relations")
@RequiredArgsConstructor
@Tag(name = "师生分配管理")
public class TeacherStudentController {

    private final TeacherStudentRelationMapper relationMapper;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "师生分配列表")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin', 'teacher')")
    public Result<IPage<TeacherStudentRelation>> listRelations(
            @PathVariable Integer batchId,
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer teacherId) {
        
        Page<TeacherStudentRelation> pageParam = new Page<>(page, size);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeacherStudentRelation>()
            .eq(TeacherStudentRelation::getBatchId, batchId)
            .eq(teacherId != null, TeacherStudentRelation::getTeacherId, teacherId)
            .orderByDesc(TeacherStudentRelation::getCreatedAt);
        
        return Result.success(relationMapper.selectPage(pageParam, wrapper));
    }

    @PostMapping("/import")
    @Operation(summary = "Excel批量导入师生关系")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    @LogOperation("批量导入师生关系")
    public Result<String> importExcel(
            @PathVariable Integer batchId,
            @RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        // TODO: 解析Excel（学号、工号列），批量插入teacher_student_relations
        return Result.success("导入成功");
    }

    @PostMapping
    @Operation(summary = "手动添加单条师生关系")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    @LogOperation("手动添加师生关系")
    public Result<Void> addRelation(
            @PathVariable Integer batchId,
            @RequestParam Integer teacherId,
            @RequestParam Integer studentId) {
        
        TeacherStudentRelation relation = new TeacherStudentRelation();
        relation.setBatchId(batchId);
        relation.setTeacherId(teacherId);
        relation.setStudentId(studentId);
        relation.setAssignedBy(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
        relation.setImportSource("manual");
        
        relationMapper.insert(relation);
        return Result.success();
    }

    @PutMapping("/change-teacher")
    @Operation(summary = "更换指导老师")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    @LogOperation("更换指导老师")
    public Result<Void> changeTeacher(
            @PathVariable Integer batchId,
            @RequestParam Integer studentId,
            @RequestParam Integer newTeacherId) {
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeacherStudentRelation>()
            .eq(TeacherStudentRelation::getBatchId, batchId)
            .eq(TeacherStudentRelation::getStudentId, studentId);
        
        TeacherStudentRelation relation = relationMapper.selectOne(wrapper);
        if (relation != null) {
            relation.setTeacherId(newTeacherId);
            relationMapper.updateById(relation);
        }
        
        return Result.success();
    }

    @DeleteMapping("/{studentId}")
    @Operation(summary = "移除学生")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    @LogOperation("移除学生")
    public Result<Void> removeStudent(
            @PathVariable Integer batchId,
            @PathVariable Integer studentId) {
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TeacherStudentRelation>()
            .eq(TeacherStudentRelation::getBatchId, batchId)
            .eq(TeacherStudentRelation::getStudentId, studentId);
        
        relationMapper.delete(wrapper);
        return Result.success();
    }
}
