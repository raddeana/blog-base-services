package com.blog.base.message.notify.dto;

import com.blog.base.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "通知分页查询")
public class NotificationQueryDTO extends PageQuery {

    @Schema(description = "接收人用户ID（管理端筛选用）", example = "1")
    private Long userId;

    @Schema(description = "类型：1系统通知 2评论 3点赞", example = "1")
    private Integer type;

    @Schema(description = "是否已读：0未读 1已读，不传查全部", example = "0")
    private Integer isRead;
}
