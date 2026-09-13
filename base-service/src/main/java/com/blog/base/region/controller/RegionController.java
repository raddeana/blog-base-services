package com.blog.base.region.controller;

import com.blog.base.common.api.Result;
import com.blog.base.region.dto.RegionTreeNode;
import com.blog.base.region.dto.RegionVO;
import com.blog.base.region.service.RegionService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;

@Tag(name = "地区管理")
@RestController
@RequestMapping("/api/v1/regions")
@RequiredArgsConstructor
public class RegionController {

    private final RegionService regionService;

    @Operation(summary = "省级行政区列表")
    @GetMapping("/provinces")
    public Result<List<RegionVO>> provinces() {
        return Result.ok(regionService.listProvinces());
    }

    @Operation(summary = "子树查询", description = "parentId=0 或不传返回省级列表")
    @GetMapping("/tree")
    public Result<List<RegionTreeNode>> tree(@RequestParam(required = false, defaultValue = "0") Long parentId) {
        return Result.ok(regionService.subtree(parentId));
    }

    @Operation(summary = "全国三级树（含缓存）")
    @GetMapping("/full-tree")
    public Result<List<RegionTreeNode>> fullTree() {
        return Result.ok(regionService.fullTree());
    }

    @Operation(summary = "按行政区划码查询")
    @GetMapping("/code/{code}")
    public Result<RegionVO> byCode(@PathVariable String code) {
        return Result.ok(regionService.getByCode(code));
    }
}
