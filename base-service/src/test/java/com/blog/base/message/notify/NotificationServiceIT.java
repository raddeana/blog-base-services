package com.blog.base.message.notify;

import com.blog.base.common.api.ErrorCode;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.it.IntegrationTest;
import com.blog.base.message.notify.dto.NotificationQueryDTO;
import com.blog.base.message.notify.dto.NotificationSendDTO;
import com.blog.base.message.notify.dto.NotificationVO;
import com.blog.base.message.notify.service.NotificationService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.sql.Timestamp;
import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests for notification persistence: real unread-count query,
 * read_time update and user-boundary enforcement against H2.
 */
@IntegrationTest
class NotificationServiceIT {

    private static final long USER_A = 1001L;
    private static final long USER_B = 1002L;

    @Autowired
    private NotificationService notificationService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void sendThenRead_shouldUpdateUnreadCountAndReadTime() {
        NotificationVO sent = notificationService.send(sendDTO(USER_A, "title", "content"));

        assertThat(notificationService.unreadCount(USER_A)).isEqualTo(1);

        notificationService.markRead(USER_A, sent.getId());

        assertThat(notificationService.unreadCount(USER_A)).isZero();
        Timestamp readTime = jdbcTemplate.queryForObject(
                "SELECT read_time FROM base_notification WHERE id = ?", Timestamp.class, sent.getId());
        assertThat(readTime).isNotNull();
    }

    @Test
    void unreadCount_shouldBeIsolatedPerUser() {
        notificationService.send(sendDTO(USER_A, "a", "a"));
        notificationService.send(sendDTO(USER_A, "b", "b"));
        notificationService.send(sendDTO(USER_B, "c", "c"));

        assertThat(notificationService.unreadCount(USER_A)).isEqualTo(2);
        assertThat(notificationService.unreadCount(USER_B)).isEqualTo(1);
    }

    @Test
    void markRead_otherUsersNotification_shouldBeRejectedByRealCheck() {
        NotificationVO sent = notificationService.send(sendDTO(USER_B, "secret", "secret"));

        assertThatThrownBy(() -> notificationService.markRead(USER_A, sent.getId()))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.STATE_ERROR);

        // still unread in DB
        Integer isRead = jdbcTemplate.queryForObject(
                "SELECT is_read FROM base_notification WHERE id = ?", Integer.class, sent.getId());
        assertThat(isRead).isZero();
    }

    @Test
    void pageForUser_shouldOnlyReturnOwnNotifications() {
        notificationService.send(sendDTO(USER_A, "mine", "x"));
        notificationService.send(sendDTO(USER_B, "others", "x"));

        NotificationQueryDTO query = new NotificationQueryDTO();
        query.setPage(1);
        query.setSize(10);

        PageResult<NotificationVO> page = notificationService.pageForUser(USER_A, query);

        assertThat(page.getRecords()).extracting(NotificationVO::getTitle).containsExactly("mine");
    }

    @Test
    void deleteBatch_shouldOnlyDeleteOwnRows() {
        NotificationVO a = notificationService.send(sendDTO(USER_A, "a", "x"));
        NotificationVO b = notificationService.send(sendDTO(USER_B, "b", "x"));

        notificationService.deleteBatch(USER_A, List.of(a.getId(), b.getId()));

        assertThat(notificationService.getById(a.getId())).isNull();
        assertThat(notificationService.getById(b.getId())).isNotNull();
    }

    private NotificationSendDTO sendDTO(long userId, String title, String content) {
        NotificationSendDTO dto = new NotificationSendDTO();
        dto.setUserId(userId);
        dto.setTitle(title);
        dto.setContent(content);
        return dto;
    }
}
