package com.blog.base.message.board.dto;

import com.blog.base.common.api.PageQuery;
import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;
import lombok.EqualsAndHashCode;

@Data
@EqualsAndHashCode(callSuper = true)
@Schema(description = "留言分页查询（后台）")
public class MessageBoardQueryDTO extends PageQuery {

    @Schema(description = "关键字（模糊匹配昵称/内容）", example = "博客")
    private String keyword;

    @Schema(description = "审核状态：0待审 1通过 2驳回，不传查全部", example = "0")
    private Integer status;
}
