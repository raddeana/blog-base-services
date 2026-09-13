package com.blog.base.message.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "留言隐藏/取消隐藏")
public class MessageHiddenDTO {

    @NotNull(message = "hidden 不能为空")
    @Min(value = 0, message = "hidden 只能为 0 或 1")
    @Max(value = 1, message = "hidden 只能为 0 或 1")
    @Schema(description = "1隐藏 0取消隐藏", example = "1")
    private Integer hidden;
}
