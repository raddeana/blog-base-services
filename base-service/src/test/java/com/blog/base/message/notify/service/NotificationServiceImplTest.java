package com.blog.base.message.notify.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.base.common.api.ErrorCode;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.message.notify.dto.NotificationQueryDTO;
import com.blog.base.message.notify.dto.NotificationSendDTO;
import com.blog.base.message.notify.dto.NotificationVO;
import com.blog.base.message.notify.entity.Notification;
import com.blog.base.message.notify.mapper.NotificationMapper;
import com.blog.base.message.notify.service.impl.NotificationServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link NotificationServiceImpl}, including the user-boundary
 * negative cases: a user must not touch another user's notifications.
 */
@ExtendWith(MockitoExtension.class)
class NotificationServiceImplTest {

    private static final long USER_A = 1L;
    private static final long USER_B = 2L;

    @Mock
    private NotificationMapper notificationMapper;

    @InjectMocks
    private NotificationServiceImpl notificationService;

    @BeforeEach
    void injectBaseMapper() {
        ReflectionTestUtils.setField(notificationService, "baseMapper", notificationMapper);
    }

    @Test
    void send_shouldDefaultTypeAndUnread() {
        when(notificationMapper.insert(any(Notification.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Notification.class).setId(10L);
            return 1;
        });

        NotificationSendDTO dto = new NotificationSendDTO();
        dto.setUserId(USER_A);
        dto.setTitle("hello");
        dto.setContent("world");

        NotificationVO vo = notificationService.send(dto);

        assertThat(vo.getId()).isEqualTo(10L);
        assertThat(vo.getType()).isEqualTo(Notification.TYPE_SYSTEM);
        assertThat(vo.getIsRead()).isZero();

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).insert(captor.capture());
        assertThat(captor.getValue().getUserId()).isEqualTo(USER_A);
    }

    @Test
    void unreadCount_shouldDelegateToCount() {
        when(notificationMapper.selectCount(any())).thenReturn(3L);

        assertThat(notificationService.unreadCount(USER_A)).isEqualTo(3L);
    }

    @Test
    void pageForUser_shouldReturnMappedRecords() {
        when(notificationMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Notification> page = invocation.getArgument(0);
            page.setRecords(List.of(notification(1L, USER_A)));
            page.setTotal(1);
            return page;
        });

        NotificationQueryDTO query = new NotificationQueryDTO();
        query.setPage(1);
        query.setSize(10);

        PageResult<NotificationVO> result = notificationService.pageForUser(USER_A, query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getUserId()).isEqualTo(USER_A);
    }

    @Test
    void markRead_owner_shouldSetReadAndTime() {
        when(notificationMapper.selectById(1L)).thenReturn(notification(1L, USER_A));
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        notificationService.markRead(USER_A, 1L);

        ArgumentCaptor<Notification> captor = ArgumentCaptor.forClass(Notification.class);
        verify(notificationMapper).updateById(captor.capture());
        assertThat(captor.getValue().getIsRead()).isEqualTo(1);
        assertThat(captor.getValue().getReadTime()).isNotNull();
    }

    @Test
    void markRead_alreadyRead_shouldSkipUpdate() {
        Notification n = notification(1L, USER_A);
        n.setIsRead(1);
        when(notificationMapper.selectById(1L)).thenReturn(n);

        notificationService.markRead(USER_A, 1L);

        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    @Test
    void markRead_notFound_shouldThrow() {
        when(notificationMapper.selectById(404L)).thenReturn(null);

        assertThatThrownBy(() -> notificationService.markRead(USER_A, 404L))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }

    @Test
    void markRead_otherUsersNotification_shouldThrow() {
        when(notificationMapper.selectById(1L)).thenReturn(notification(1L, USER_B));

        assertThatThrownBy(() -> notificationService.markRead(USER_A, 1L))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.STATE_ERROR);

        verify(notificationMapper, never()).updateById(any(Notification.class));
    }

    @Test
    void markReadBatch_shouldOnlyTouchOwnNotifications() {
        Notification own = notification(1L, USER_A);
        Notification others = notification(2L, USER_B);
        when(notificationMapper.selectBatchIds(any())).thenReturn(List.of(own, others));
        when(notificationMapper.updateById(any(Notification.class))).thenReturn(1);

        notificationService.markReadBatch(USER_A, List.of(1L, 2L));

        verify(notificationMapper, times(1)).updateById(any(Notification.class));
    }

    @Test
    void deleteBatch_shouldOnlyDeleteOwnNotifications() {
        Notification own = notification(1L, USER_A);
        Notification others = notification(2L, USER_B);
        when(notificationMapper.selectBatchIds(any())).thenReturn(List.of(own, others));
        when(notificationMapper.deleteById(any(java.io.Serializable.class))).thenReturn(1);

        notificationService.deleteBatch(USER_A, List.of(1L, 2L));

        verify(notificationMapper, times(1)).deleteById(any(java.io.Serializable.class));
    }

    private Notification notification(Long id, Long userId) {
        Notification n = new Notification();
        n.setId(id);
        n.setUserId(userId);
        n.setTitle("title");
        n.setContent("content");
        n.setType(Notification.TYPE_SYSTEM);
        n.setIsRead(0);
        return n;
    }
}
