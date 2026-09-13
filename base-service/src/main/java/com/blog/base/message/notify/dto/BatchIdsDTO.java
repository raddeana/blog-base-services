package com.blog.base.message.notify.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import jakarta.validation.constraints.NotEmpty;
import lombok.Data;

import java.util.List;

@Data
@Schema(description = "批量已读/删除")
public class BatchIdsDTO {

    @NotEmpty(message = "ids 不能为空")
    @Schema(description = "ID列表", example = "[1,2,3]")
    private List<Long> ids;
}
