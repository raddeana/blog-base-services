package com.blog.base.message.notify.controller;

import com.blog.base.common.api.PageResult;
import com.blog.base.common.api.Result;
import com.blog.base.message.notify.dto.BatchIdsDTO;
import com.blog.base.message.notify.dto.NotificationQueryDTO;
import com.blog.base.message.notify.dto.NotificationVO;
import com.blog.base.message.notify.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

/**
 * User-side notification endpoints. userId comes from the path for now;
 * switch to token-based identity later without touching the service layer.
 */
@Tag(name = "站内通知-用户侧")
@RestController
@RequestMapping("/api/v1/users/{userId}/notifications")
@RequiredArgsConstructor
public class NotificationUserController {

    private final NotificationService notificationService;

    @Operation(summary = "我的通知分页")
    @GetMapping
    public Result<PageResult<NotificationVO>> page(@PathVariable Long userId, NotificationQueryDTO query) {
        return Result.ok(notificationService.pageForUser(userId, query));
    }

    @Operation(summary = "未读数量")
    @GetMapping("/unread-count")
    public Result<Long> unreadCount(@PathVariable Long userId) {
        return Result.ok(notificationService.unreadCount(userId));
    }

    @Operation(summary = "标记单条已读")
    @PutMapping("/{id}/read")
    public Result<Void> markRead(@PathVariable Long userId, @PathVariable Long id) {
        notificationService.markRead(userId, id);
        return Result.ok();
    }

    @Operation(summary = "批量标记已读", description = "body: {\"ids\": [1,2]}")
    @PutMapping("/read")
    public Result<Void> markReadBatch(@PathVariable Long userId, @Valid @RequestBody BatchIdsDTO body) {
        notificationService.markReadBatch(userId, body.getIds());
        return Result.ok();
    }

    @Operation(summary = "批量删除", description = "body: {\"ids\": [1,2]}")
    @DeleteMapping
    public Result<Void> deleteBatch(@PathVariable Long userId, @Valid @RequestBody BatchIdsDTO body) {
        notificationService.deleteBatch(userId, body.getIds());
        return Result.ok();
    }
}
