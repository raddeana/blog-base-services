package com.blog.base.tag.dto;

import com.blog.base.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "标签分页查询")
public class TagQueryDTO extends PageQuery {

    @Schema(description = "关键字（模糊匹配标签名）", example = "Java")
    private String keyword;

    @Schema(description = "状态：1启用 0禁用，不传查全部", example = "1")
    private Integer status;
}
