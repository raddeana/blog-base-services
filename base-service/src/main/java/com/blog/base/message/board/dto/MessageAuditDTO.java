package com.blog.base.message.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import jakarta.validation.constraints.NotNull;
import lombok.Data;

@Data
@Schema(description = "留言审核")
public class MessageAuditDTO {

    @NotNull(message = "status 不能为空")
    @Min(value = 1, message = "status 只能为 1(通过) 或 2(驳回)")
    @Max(value = 2, message = "status 只能为 1(通过) 或 2(驳回)")
    @Schema(description = "审核结果：1通过 2驳回", example = "1")
    private Integer status;
}
