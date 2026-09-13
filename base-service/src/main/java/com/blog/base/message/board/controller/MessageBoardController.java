package com.blog.base.message.board.controller;

import com.blog.base.common.api.PageResult;
import com.blog.base.common.api.Result;
import com.blog.base.message.board.dto.MessageBoardQueryDTO;
import com.blog.base.message.board.dto.MessageBoardVO;
import com.blog.base.message.board.dto.MessagePostDTO;
import com.blog.base.message.board.service.MessageBoardService;
import io.swagger.v3.oas.annotations.Operation;
import io.swagger.v3.oas.annotations.tags.Tag;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.validation.Valid;
import lombok.RequiredArgsConstructor;
import org.springframework.web.bind.annotation.*;

@Tag(name = "留言板-公开")
@RestController
@RequestMapping("/api/v1/messages")
@RequiredArgsConstructor
public class MessageBoardController {

    private final MessageBoardService messageBoardService;

    @Operation(summary = "发表留言")
    @PostMapping
    public Result<MessageBoardVO> post(@Valid @RequestBody MessagePostDTO dto, HttpServletRequest request) {
        return Result.ok(messageBoardService.post(dto, resolveIp(request)));
    }

    @Operation(summary = "公开留言列表", description = "仅展示已通过审核且未隐藏的留言，按顶层留言分页并附回复")
    @GetMapping
    public Result<PageResult<MessageBoardVO>> pagePublic(MessageBoardQueryDTO query) {
        return Result.ok(messageBoardService.pagePublic(query));
    }

    private String resolveIp(HttpServletRequest request) {
        String xff = request.getHeader("X-Forwarded-For");
        if (xff != null && !xff.isBlank()) {
            return xff.split(",")[0].trim();
        }
        return request.getRemoteAddr();
    }
}
