package com.internship.controller;

import com.baomidou.mybatisplus.core.metadata.IPage;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.internship.common.Result;
import com.internship.entity.Notification;
import com.internship.entity.OperationLog;
import com.internship.entity.SystemConfig;
import com.internship.mapper.NotificationMapper;
import com.internship.mapper.OperationLogMapper;
import com.internship.mapper.SystemConfigMapper;
import com.internship.util.JwtUtil;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.security.access.prepost.PreAuthorize;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@RestController
@RequestMapping("/api")
@RequiredArgsConstructor
@Tag(name = "系统管理与日志")
public class LogController {

    private final OperationLogMapper operationLogMapper;
    private final NotificationMapper notificationMapper;
    private final SystemConfigMapper systemConfigMapper;
    private final JwtUtil jwtUtil;

    // ==================== 操作日志 ====================

    @GetMapping("/logs")
    @Operation(summary = "查询操作日志")
    @PreAuthorize("hasRole('college_admin')")
    public Result listLogs(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size,
            @RequestParam(required = false) String operation,
            @RequestParam(required = false) String targetType,
            @RequestParam(required = false) Integer userId) {
        
        Page<OperationLog> pageParam = new Page<>(page, size);
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<OperationLog>()
            .eq(operation != null, OperationLog::getOperation, operation)
            .eq(targetType != null, OperationLog::getTargetType, targetType)
            .eq(userId != null, OperationLog::getUserId, userId)
            .orderByDesc(OperationLog::getCreatedAt);
        
        return Result.success(operationLogMapper.selectPage(pageParam, wrapper));
    }

    // ==================== 消息通知 ====================

    @GetMapping("/notifications/unread-count")
    @Operation(summary = "未读消息数")
    public Result<Integer> getUnreadCount() {
        Integer receiverId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;
        
        Long count = notificationMapper.selectCount(
            new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification>()
                .eq(Notification::getReceiverId, receiverId)
                .eq(Notification::getIsRead, false)
        );
        
        return Result.success(count.intValue());
    }

    @GetMapping("/notifications")
    @Operation(summary = "消息列表")
    public Result listNotifications(
            @RequestParam(defaultValue = "1") int page,
            @RequestParam(defaultValue = "10") int size) {
        
        Integer receiverId = jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null;
        Page<Notification> pageParam = new Page<>(page, size);
        
        var wrapper = new com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper<Notification>()
            .eq(Notification::getReceiverId, receiverId)
            .orderByDesc(Notification::getCreatedAt);
        
        return Result.success(notificationMapper.selectPage(pageParam, wrapper));
    }

    @PutMapping("/notifications/{notifId}/read")
    @Operation(summary = "标记已读")
    public Result<Void> markAsRead(@PathVariable Integer notifId) {
        Notification notif = notificationMapper.selectById(notifId);
        if (notif != null) {
            notif.setIsRead(true);
            notif.setReadAt(java.time.LocalDateTime.now());
            notificationMapper.updateById(notif);
        }
        return Result.success();
    }

    @PostMapping("/notifications/send")
    @Operation(summary = "发送通知（内部使用）")
    public Result<Void> sendNotification(
            @RequestParam Integer receiverId,
            @RequestParam String title,
            @RequestParam String content,
            @RequestParam(required = false) String relatedType,
            @RequestParam(required = false) Integer relatedId) {
        
        Notification notification = new Notification();
        notification.setReceiverId(receiverId);
        notification.setTitle(title);
        notification.setContent(content);
        notification.setIsRead(false);
        notification.setRelatedType(relatedType);
        notification.setRelatedId(relatedId);
        notification.setPriority("normal");
        notification.setCategory("business");
        
        notificationMapper.insert(notification);
        return Result.success();
    }

    // ==================== 系统配置 ====================

    @GetMapping("/configs")
    @Operation(summary = "获取所有系统配置")
    @PreAuthorize("hasAnyRole('college_admin', 'major_admin')")
    public Result<List<SystemConfig>> listConfigs() {
        return Result.success(systemConfigMapper.selectList(null));
    }

    @GetMapping("/configs/{configKey}")
    @Operation(summary = "获取单个配置项")
    public Result<String> getConfig(@PathVariable String configKey) {
        return Result.success(systemConfigMapper.getConfigValue(configKey));
    }

    @PutMapping("/configs/{configId}")
    @Operation(summary = "更新系统配置")
    @PreAuthorize("hasRole('college_admin')")
    public Result<Void> updateConfig(
            @PathVariable Integer configId,
            @RequestBody Map<String, Object> body) {
        
        SystemConfig config = systemConfigMapper.selectById(configId);
        if (config != null) {
            config.setConfigValue(body.get("configValue").toString());
            config.setUpdatedBy(jwtUtil.getCurrentUserId() != null ? jwtUtil.getCurrentUserId().intValue() : null);
            systemConfigMapper.updateById(config);
        }
        return Result.success();
    }
}
