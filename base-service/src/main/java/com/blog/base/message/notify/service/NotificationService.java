package com.blog.base.message.notify.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.base.common.api.PageResult;
import com.blog.base.message.notify.dto.NotificationQueryDTO;
import com.blog.base.message.notify.dto.NotificationSendDTO;
import com.blog.base.message.notify.dto.NotificationVO;
import com.blog.base.message.notify.entity.Notification;

import java.util.List;

/**
 * Notification service.
 */
public interface NotificationService extends IService<Notification> {

    NotificationVO send(NotificationSendDTO dto);

    PageResult<NotificationVO> pageForAdmin(NotificationQueryDTO query);

    PageResult<NotificationVO> pageForUser(Long userId, NotificationQueryDTO query);

    long unreadCount(Long userId);

    void markRead(Long userId, Long id);

    void markReadBatch(Long userId, List<Long> ids);

    void deleteBatch(Long userId, List<Long> ids);
}
