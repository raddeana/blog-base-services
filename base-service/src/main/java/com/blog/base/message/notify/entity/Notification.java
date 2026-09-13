package com.blog.base.message.notify.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.base.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

import java.time.LocalDateTime;

/**
 * In-site notification entity, table base_notification.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_notification")
public class Notification extends BaseEntity {

    public static final int TYPE_SYSTEM = 1;
    public static final int TYPE_COMMENT = 2;
    public static final int TYPE_LIKE = 3;

    /** 接收人用户ID（博客用户体系） */
    private Long userId;

    /** 标题 */
    private String title;

    /** 内容 */
    private String content;

    /** 类型：1系统通知 2评论 3点赞 */
    private Integer type;

    /** 是否已读：0未读 1已读 */
    private Integer isRead;

    /** 阅读时间 */
    private LocalDateTime readTime;
}
