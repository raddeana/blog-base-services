package com.blog.base.message.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Email;
import jakarta.validation.constraints.NotBlank;
import jakarta.validation.constraints.Size;
import lombok.Data;

@Data
@Schema(description = "访客发表留言")
public class MessagePostDTO {

    @NotBlank(message = "昵称不能为空")
    @Size(max = 32, message = "昵称最长32字符")
    @Schema(description = "访客昵称", example = "张三")
    private String nickname;

    @Email(message = "邮箱格式不正确")
    @Size(max = 64, message = "邮箱最长64字符")
    @Schema(description = "访客邮箱", example = "zhangsan@example.com")
    private String email;

    @NotBlank(message = "内容不能为空")
    @Size(max = 1000, message = "内容最长1000字符")
    @Schema(description = "留言内容", example = "博客写得真好！")
    private String content;
}
