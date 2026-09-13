package com.blog.base.message.notify.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.message.notify.dto.NotificationQueryDTO;
import com.blog.base.message.notify.dto.NotificationSendDTO;
import com.blog.base.message.notify.dto.NotificationVO;
import com.blog.base.message.notify.entity.Notification;
import com.blog.base.message.notify.mapper.NotificationMapper;
import com.blog.base.message.notify.service.NotificationService;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;
import java.util.List;

/**
 * Notification service implementation.
 */
@Service
public class NotificationServiceImpl extends ServiceImpl<NotificationMapper, Notification>
        implements NotificationService {

    @Override
    public NotificationVO send(NotificationSendDTO dto) {
        Notification notification = new Notification();
        notification.setUserId(dto.getUserId());
        notification.setTitle(dto.getTitle());
        notification.setContent(dto.getContent());
        notification.setType(dto.getType() == null ? Notification.TYPE_SYSTEM : dto.getType());
        notification.setIsRead(0);
        this.save(notification);
        return toVO(notification);
    }

    @Override
    public PageResult<NotificationVO> pageForAdmin(NotificationQueryDTO query) {
        return page(query.getUserId(), query, query.getPage(), query.getSize());
    }

    @Override
    public PageResult<NotificationVO> pageForUser(Long userId, NotificationQueryDTO query) {
        return page(userId, query, query.getPage(), query.getSize());
    }

    private PageResult<NotificationVO> page(Long userId, NotificationQueryDTO query, int page, int size) {
        LambdaQueryWrapper<Notification> wrapper = new LambdaQueryWrapper<Notification>()
                .eq(userId != null, Notification::getUserId, userId)
                .eq(query.getType() != null, Notification::getType, query.getType())
                .eq(query.getIsRead() != null, Notification::getIsRead, query.getIsRead())
                .orderByDesc(Notification::getCreateTime)
                .orderByDesc(Notification::getId);
        Page<Notification> result = this.page(new Page<>(page, size), wrapper);
        List<NotificationVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result.getTotal(), page, size, records);
    }

    @Override
    public long unreadCount(Long userId) {
        return this.count(new LambdaQueryWrapper<Notification>()
                .eq(Notification::getUserId, userId)
                .eq(Notification::getIsRead, 0));
    }

    @Override
    @Transactional
    public void markRead(Long userId, Long id) {
        Notification notification = this.getById(id);
        if (notification == null) {
            throw BizException.notFound();
        }
        if (!notification.getUserId().equals(userId)) {
            throw new BizException(com.blog.base.common.api.ErrorCode.STATE_ERROR, "无权操作该通知");
        }
        doMarkRead(notification);
    }

    @Override
    @Transactional
    public void markReadBatch(Long userId, List<Long> ids) {
        this.listByIds(ids).forEach(notification -> {
            if (notification.getUserId().equals(userId)) {
                doMarkRead(notification);
            }
        });
    }

    private void doMarkRead(Notification notification) {
        if (notification.getIsRead() != null && notification.getIsRead() == 1) {
            return;
        }
        notification.setIsRead(1);
        notification.setReadTime(LocalDateTime.now());
        this.updateById(notification);
    }

    @Override
    @Transactional
    public void deleteBatch(Long userId, List<Long> ids) {
        this.listByIds(ids).forEach(notification -> {
            if (notification.getUserId().equals(userId)) {
                this.removeById(notification.getId());
            }
        });
    }

    private NotificationVO toVO(Notification notification) {
        NotificationVO vo = new NotificationVO();
        vo.setId(notification.getId());
        vo.setUserId(notification.getUserId());
        vo.setTitle(notification.getTitle());
        vo.setContent(notification.getContent());
        vo.setType(notification.getType());
        vo.setIsRead(notification.getIsRead());
        vo.setReadTime(notification.getReadTime());
        vo.setCreateTime(notification.getCreateTime());
        return vo;
    }
}
