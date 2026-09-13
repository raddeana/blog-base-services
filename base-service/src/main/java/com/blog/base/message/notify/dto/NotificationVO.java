package com.blog.base.message.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "站内通知信息")
public class NotificationVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "接收人用户ID", example = "1")
    private Long userId;

    @Schema(description = "标题", example = "系统通知")
    private String title;

    @Schema(description = "内容")
    private String content;

    @Schema(description = "类型：1系统通知 2评论 3点赞", example = "1")
    private Integer type;

    @Schema(description = "是否已读：0未读 1已读", example = "0")
    private Integer isRead;

    @Schema(description = "阅读时间")
    private LocalDateTime readTime;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;
}
