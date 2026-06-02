package com.internship.config;

import com.baomidou.mybatisplus.core.handlers.MetaObjectHandler;
import lombok.extern.slf4j.Slf4j;
import org.apache.ibatis.reflection.MetaObject;
import org.springframework.stereotype.Component;

import java.time.LocalDateTime;

/**
 * MyBatis-Plus自动填充处理器
 * 在执行INSERT和UPDATE操作时，自动填充公共字段（创建时间、更新时间、逻辑删除标志等）
 */
@Slf4j
@Component
public class MyBatisMetaObjectHandler implements MetaObjectHandler {

    /**
     * 插入时自动填充
     * 填充字段：createTime、updateTime、deleted、status、isRead、approvalStatus
     */
    @Override
    public void insertFill(MetaObject metaObject) {
        log.debug("执行插入自动填充");
        this.strictInsertFill(metaObject, "createTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
        this.strictInsertFill(metaObject, "deleted", Integer.class, 0);
        this.strictInsertFill(metaObject, "status", Integer.class, 1);
        this.strictInsertFill(metaObject, "isRead", Integer.class, 0);
        this.strictInsertFill(metaObject, "approvalStatus", Integer.class, 0);
    }

    /**
     * 更新时自动填充
     * 填充字段：updateTime
     */
    @Override
    public void updateFill(MetaObject metaObject) {
        log.debug("执行更新自动填充");
        this.strictUpdateFill(metaObject, "updateTime", LocalDateTime.class, LocalDateTime.now());
    }
}
