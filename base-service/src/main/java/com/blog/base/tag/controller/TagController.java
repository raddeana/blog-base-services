package com.blog.base.tag.controller;

import com.blog.base.common.api.PageResult;
import com.blog.base.common.api.Result;
import com.blog.base.tag.dto.*;
import com.blog.base.tag.service.TagService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

import java.util.List;
import java.util.Map;

@Tag(name = "标签管理")
@RestController
@RequestMapping("/api/v1/tags")
@RequiredArgsConstructor
public class TagController {

    private final TagService tagService;

    @Operation(summary = "分页查询标签")
    @GetMapping
    public Result<PageResult<TagVO>> page(TagQueryDTO query) {
        return Result.ok(tagService.page(query));
    }

    @Operation(summary = "全部启用标签（下拉用）")
    @GetMapping("/all")
    public Result<List<TagVO>> listAll() {
        return Result.ok(tagService.listEnabled());
    }

    @Operation(summary = "标签详情")
    @GetMapping("/{id}")
    public Result<TagVO> detail(@PathVariable Long id) {
        return Result.ok(tagService.detail(id));
    }

    @Operation(summary = "新增标签")
    @PostMapping
    public Result<TagVO> create(@Valid @RequestBody TagCreateDTO dto) {
        return Result.ok(tagService.create(dto));
    }

    @Operation(summary = "更新标签")
    @PutMapping("/{id}")
    public Result<TagVO> update(@PathVariable Long id, @Valid @RequestBody TagUpdateDTO dto) {
        return Result.ok(tagService.update(id, dto));
    }

    @Operation(summary = "启用/禁用标签", description = "body: {\"status\": 0|1}")
    @PutMapping("/{id}/status")
    public Result<Void> updateStatus(@PathVariable Long id, @RequestBody Map<String, Integer> body) {
        tagService.updateStatus(id, body.get("status"));
        return Result.ok();
    }

    @Operation(summary = "删除标签（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        tagService.delete(id);
        return Result.ok();
    }
}
