package com.blog.base.message.board.controller;

import com.blog.base.common.api.PageResult;
import com.blog.base.common.api.Result;
import com.blog.base.message.board.dto.*;
import com.blog.base.message.board.service.MessageBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "留言板-管理端")
@RestController
@RequestMapping("/api/v1/admin/messages")
@RequiredArgsConstructor
public class MessageBoardAdminController {

    private final MessageBoardService messageBoardService;

    @Operation(summary = "留言分页（后台）")
    @GetMapping
    public Result<PageResult<MessageBoardVO>> page(MessageBoardQueryDTO query) {
        return Result.ok(messageBoardService.pageForAdmin(query));
    }

    @Operation(summary = "审核留言", description = "body: {\"status\": 1|2}，1通过 2驳回")
    @PutMapping("/{id}/audit")
    public Result<Void> audit(@PathVariable Long id, @Valid @RequestBody MessageAuditDTO dto) {
        messageBoardService.audit(id, dto.getStatus());
        return Result.ok();
    }

    @Operation(summary = "隐藏/取消隐藏", description = "body: {\"hidden\": 0|1}")
    @PutMapping("/{id}/hidden")
    public Result<Void> hidden(@PathVariable Long id, @Valid @RequestBody MessageHiddenDTO dto) {
        messageBoardService.updateHidden(id, dto.getHidden());
        return Result.ok();
    }

    @Operation(summary = "管理员回复")
    @PostMapping("/{id}/reply")
    public Result<MessageBoardVO> reply(@PathVariable Long id, @Valid @RequestBody MessageReplyDTO dto) {
        return Result.ok(messageBoardService.reply(id, dto.getContent()));
    }

    @Operation(summary = "删除留言（逻辑删除）")
    @DeleteMapping("/{id}")
    public Result<Void> delete(@PathVariable Long id) {
        messageBoardService.delete(id);
        return Result.ok();
    }
}
