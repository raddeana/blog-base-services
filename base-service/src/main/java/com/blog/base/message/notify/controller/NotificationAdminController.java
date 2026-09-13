package com.blog.base.message.notify.controller;

import com.blog.base.common.api.PageResult;
import com.blog.base.common.api.Result;
import com.blog.base.message.notify.dto.NotificationQueryDTO;
import com.blog.base.message.notify.dto.NotificationSendDTO;
import com.blog.base.message.notify.dto.NotificationVO;
import com.blog.base.message.notify.service.NotificationService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "站内通知-管理端")
@RestController
@RequestMapping("/api/v1/notifications")
@RequiredArgsConstructor
public class NotificationAdminController {

    private final NotificationService notificationService;

    @Operation(summary = "发送通知")
    @PostMapping
    public Result<NotificationVO> send(@Valid @RequestBody NotificationSendDTO dto) {
        return Result.ok(notificationService.send(dto));
    }

    @Operation(summary = "通知分页（管理端）")
    @GetMapping
    public Result<PageResult<NotificationVO>> page(NotificationQueryDTO query) {
        return Result.ok(notificationService.pageForAdmin(query));
    }
}
