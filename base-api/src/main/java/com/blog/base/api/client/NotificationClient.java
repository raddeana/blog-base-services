package com.blog.base.api.client;

import com.blog.base.api.dto.ApiResult;
import com.blog.base.api.dto.NotificationDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

@FeignClient(name = "blog-base-services", path = "/api/v1", contextId = "notificationClient")
public interface NotificationClient {

    @PostMapping("/notifications")
    ApiResult<NotificationDTO> send(@RequestBody NotificationDTO dto);

    @GetMapping("/users/{userId}/notifications/unread-count")
    ApiResult<Long> unreadCount(@PathVariable("userId") Long userId);
}
