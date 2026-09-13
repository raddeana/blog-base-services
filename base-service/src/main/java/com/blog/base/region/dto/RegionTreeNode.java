package com.blog.base.region.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "地区树节点")
public class RegionTreeNode {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "父级ID，0=省级根", example = "0")
    private Long parentId;

    @Schema(description = "行政区划码", example = "110000")
    private String code;

    @Schema(description = "名称", example = "北京市")
    private String name;

    @Schema(description = "层级：1省 2市 3区县", example = "1")
    private Integer level;

    @Schema(description = "子节点")
    private List<RegionTreeNode> children = new ArrayList<>();
}
