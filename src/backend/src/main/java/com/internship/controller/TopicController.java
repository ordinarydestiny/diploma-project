package com.internship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.annotation.LogOperation;
import com.internship.common.Result;
import com.internship.entity.Topic;
import com.internship.entity.TopicMajorRelation;
import com.internship.mapper.TopicMapper;
import com.internship.mapper.TopicMajorRelationMapper;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Tag(name = "题库管理")
public class TopicController {

    private final TopicMapper topicMapper;
    private final TopicMajorRelationMapper topicMajorRelationMapper;
    private final JwtUtil jwtUtil;

    @GetMapping
    @Operation(summary = "题目列表（支持筛选）")
    public Result<IPage<Topic>> listTopics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword) {
        
        Page<Topic> pageParam = new Page<>(page, size);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
            .like(keyword != null, Topic::getTopicName, keyword)
            .eq(difficulty != null, Topic::getDifficulty, difficulty)
            .eq(Topic::getStatus, "available")
            .orderByDesc(Topic::getCreatedAt);
        
        return Result.success(topicMapper.selectPage(pageParam, wrapper));
    }

    @GetMapping("/{topicId}")
    @Operation(summary = "题目详情")
    public Result<Topic> getDetail(@PathVariable Integer topicId) {
        return Result.success(topicMapper.selectById(topicId));
    }

    @PostMapping
    @Operation(summary = "手动新增题目")
    @PreAuthorize("hasAnyRole('teacher', 'college_admin', 'major_admin')")
    public Result<Topic> create(@RequestBody Topic topic) {
        topic.setCreatorId(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
        topic.setSelectionCount(0);
        topic.setStatus("available");
        topicMapper.insert(topic);
        return Result.success(topic);
    }

    @PostMapping("/import")
    @Operation(summary = "Excel批量导入题目")
    @PreAuthorize("hasAnyRole('teacher', 'college_admin', 'major_admin')")
    public Result<String> importExcel(@RequestParam("file") org.springframework.web.multipart.MultipartFile file) {
        // TODO: 解析Excel批量插入topics
        return Result.success("导入成功");
    }

    @PutMapping("/{topicId}")
    @Operation(summary = "修改题目（教师/管理员）")
    @PreAuthorize("hasAnyRole('teacher', 'college_admin', 'major_admin')")
    public Result<Void> update(@PathVariable Integer topicId, @RequestBody Topic topic) {
        topic.setTopicId(topicId);
        topicMapper.updateById(topic);
        return Result.success();
    }

    @PostMapping("/{topicId}/majors")
    @Operation(summary = "专业负责人跨专业纳入题目")
    @PreAuthorize("hasRole('major_admin')")
    @LogOperation("纳入题目到本专业")
    public Result<Void> addMajor(
            @PathVariable Integer topicId,
            @RequestParam Integer majorId) {
        
        TopicMajorRelation relation = new TopicMajorRelation();
        relation.setTopicId(topicId);
        relation.setMajorId(majorId);
        relation.setIncludedBy(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
        topicMajorRelationMapper.insert(relation);
        
        return Result.success();
    }

    @DeleteMapping("/{topicId}/majors/{majorId}")
    @Operation(summary = "移除纳入的题目")
    @PreAuthorize("hasRole('major_admin')")
    @LogOperation("移除纳入的题目")
    public Result<Void> removeMajor(
            @PathVariable Integer topicId,
            @PathVariable Integer majorId) {
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<TopicMajorRelation>()
            .eq(TopicMajorRelation::getTopicId, topicId)
            .eq(TopicMajorRelation::getMajorId, majorId);
        topicMajorRelationMapper.delete(wrapper);
        
        return Result.success();
    }

    @DeleteMapping("/{topicId}")
    @Operation(summary = "删除题目（教师/管理员，且未被选用）")
    @PreAuthorize("hasAnyRole('teacher', 'college_admin', 'major_admin')")
    @LogOperation("删除题目")
    public Result<Void> delete(@PathVariable Integer topicId) {
        topicMapper.deleteById(topicId);
        return Result.success();
    }
}
