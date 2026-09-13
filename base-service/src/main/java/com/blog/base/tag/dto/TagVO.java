package com.blog.base.tag.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;

@Data
@Schema(description = "标签信息")
public class TagVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "标签名", example = "Java")
    private String name;

    @Schema(description = "状态：1启用 0禁用", example = "1")
    private Integer status;

    @Schema(description = "排序", example = "0")
    private Integer sort;

    @Schema(description = "备注", example = "后端语言")
    private String remark;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "更新时间")
    private LocalDateTime updateTime;
}
