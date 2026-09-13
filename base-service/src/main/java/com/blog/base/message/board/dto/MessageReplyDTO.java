package com.blog.base.message.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "管理员回复留言")
public class MessageReplyDTO {

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容最长1000字符")
    @Schema(description = "回复内容", example = "感谢支持！")
    private String content;
}
