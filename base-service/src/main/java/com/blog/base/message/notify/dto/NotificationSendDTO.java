package com.blog.base.message.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.NotNull;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "发送站内通知")
public class NotificationSendDTO {

    @NotNull(message = "接收人不能为空")
    @Schema(description = "接收人用户ID", example = "1")
    private Long userId;

    @NotBlank(message = "标题不能为空")
    @Size(max = 128, message = "标题最长128字符")
    @Schema(description = "标题", example = "系统通知")
    private String title;

    @NotBlank(message = "内容不能为空")
    @Size(max = 1024, message = "内容最长1024字符")
    @Schema(description = "内容", example = "欢迎使用博客平台")
    private String content;

    @Schema(description = "类型：1系统通知 2评论 3点赞，默认1", example = "1")
    private Integer type;
}
