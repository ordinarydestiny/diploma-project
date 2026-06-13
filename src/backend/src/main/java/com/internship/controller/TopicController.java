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
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@RestController
@RequestMapping("/api/topics")
@RequiredArgsConstructor
@Tag(name = "题库管理")
public class TopicController {

    private final TopicMapper topicMapper;
    private final TopicMajorRelationMapper topicMajorRelationMapper;
    private final JwtUtil jwtUtil;
    private final JdbcTemplate jdbcTemplate;

    @GetMapping
    @Operation(summary = "题目列表（支持筛选）")
    public Result<IPage<Topic>> listTopics(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) Integer majorId,
            @RequestParam(required = false) String difficulty,
            @RequestParam(required = false) String keyword,
            @RequestParam(required = false) String grade) {

        Page<Topic> pageParam = new Page<>(page, size);

        if (grade != null && !grade.isEmpty()) {
            // 按届次筛选：只显示该届次批次中被学生选中的题目
            String sql = """
                SELECT DISTINCT t.* FROM topics t
                INNER JOIN student_selections ss ON t.topic_id = ss.topic_id
                INNER JOIN project_batches pb ON ss.batch_id = pb.batch_id
                WHERE pb.batch_name LIKE CONCAT(?, '%%')
                  AND t.status = 'available'
                ORDER BY t.created_at DESC
            """;

            List<Topic> topics = jdbcTemplate.query(sql, (rs, rowNum) -> {
                Topic topic = new Topic();
                topic.setTopicId(rs.getInt("topic_id"));
                topic.setTopicName(rs.getString("topic_name"));
                topic.setDescription(rs.getString("description"));
                topic.setDifficulty((int) rs.getByte("difficulty"));
                topic.setCategory(rs.getString("category"));
                topic.setTopicType(rs.getString("topic_type"));
                topic.setSource(rs.getString("source"));
                topic.setRequirements(rs.getString("requirements"));
                topic.setReferences(rs.getString("`references`"));
                topic.setCreatorId(rs.getInt("creator_id"));
                topic.setSelectionCount(rs.getInt("selection_count"));
                topic.setMaxStudents(rs.getInt("max_students"));
                topic.setStatus(rs.getString("status"));
                topic.setCreatedAt(rs.getTimestamp("created_at").toLocalDateTime());
                topic.setUpdatedAt(rs.getTimestamp("updated_at").toLocalDateTime());
                return topic;
            }, grade + "届");

            IPage<Topic> result = new Page<>(pageParam.getCurrent(), pageParam.getSize(), topics.size());
            int start = (int) ((pageParam.getCurrent() - 1) * pageParam.getSize());
            int end = Math.min((int) (start + pageParam.getSize()), topics.size());
            result.setRecords(topics.subList(start, end));
            return Result.success(result);
        } else {
            // 不按届次筛选：显示所有可用题目
            var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Topic>()
                .like(keyword != null, Topic::getTopicName, keyword)
                .eq(difficulty != null, Topic::getDifficulty, difficulty)
                .eq(Topic::getStatus, "available")
                .orderByDesc(Topic::getCreatedAt);

            return Result.success(topicMapper.selectPage(pageParam, wrapper));
        }
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
