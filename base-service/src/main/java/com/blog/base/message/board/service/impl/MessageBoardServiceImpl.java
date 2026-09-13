package com.blog.base.message.board.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.message.board.dto.*;
import com.blog.base.message.board.entity.MessageBoard;
import com.blog.base.message.board.mapper.MessageBoardMapper;
import com.blog.base.message.board.service.MessageBoardService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;
import java.util.Map;
import java.util.stream.Collectors;

/**
 * Message board service implementation. Tree is 2-level flat: replies always
 * point to the root message via parent_id.
 */
@Service
public class MessageBoardServiceImpl extends ServiceImpl<MessageBoardMapper, MessageBoard>
        implements MessageBoardService {

    @Override
    public MessageBoardVO post(MessagePostDTO dto, String ip) {
        MessageBoard message = new MessageBoard();
        message.setParentId(null);
        message.setNickname(dto.getNickname());
        message.setEmail(dto.getEmail());
        message.setContent(dto.getContent());
        message.setIp(ip == null ? "" : ip);
        message.setStatus(MessageBoard.STATUS_PENDING);
        message.setIsHidden(0);
        message.setIsAdmin(0);
        this.save(message);
        return toVO(message);
    }

    @Override
    public PageResult<MessageBoardVO> pagePublic(MessageBoardQueryDTO query) {
        // top-level approved & visible messages, paged
        Page<MessageBoard> result = this.page(new Page<>(query.getPage(), query.getSize()),
                new LambdaQueryWrapper<MessageBoard>()
                        .isNull(MessageBoard::getParentId)
                        .eq(MessageBoard::getStatus, MessageBoard.STATUS_APPROVED)
                        .eq(MessageBoard::getIsHidden, 0)
                        .orderByDesc(MessageBoard::getCreateTime)
                        .orderByDesc(MessageBoard::getId));
        List<MessageBoard> tops = result.getRecords();
        if (tops.isEmpty()) {
            return PageResult.of(0, query.getPage(), query.getSize(), List.of());
        }
        // attach visible replies
        Map<Long, List<MessageBoardVO>> repliesByParent = this.list(
                        new LambdaQueryWrapper<MessageBoard>()
                                .in(MessageBoard::getParentId, tops.stream().map(MessageBoard::getId).toList())
                                .eq(MessageBoard::getStatus, MessageBoard.STATUS_APPROVED)
                                .eq(MessageBoard::getIsHidden, 0)
                                .orderByAsc(MessageBoard::getCreateTime))
                .stream()
                .map(this::toVO)
                .collect(Collectors.groupingBy(MessageBoardVO::getParentId));
        List<MessageBoardVO> records = tops.stream()
                .map(m -> {
                    MessageBoardVO vo = toVO(m);
                    vo.setChildren(repliesByParent.getOrDefault(m.getId(), List.of()));
                    return vo;
                })
                .toList();
        return PageResult.of(result.getTotal(), query.getPage(), query.getSize(), records);
    }

    @Override
    public PageResult<MessageBoardVO> pageForAdmin(MessageBoardQueryDTO query) {
        LambdaQueryWrapper<MessageBoard> wrapper = new LambdaQueryWrapper<MessageBoard>()
                .eq(query.getStatus() != null, MessageBoard::getStatus, query.getStatus())
                .and(StringUtils.hasText(query.getKeyword()), w -> w
                        .like(MessageBoard::getNickname, query.getKeyword())
                        .or()
                        .like(MessageBoard::getContent, query.getKeyword()))
                .orderByDesc(MessageBoard::getCreateTime)
                .orderByDesc(MessageBoard::getId);
        Page<MessageBoard> result = this.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        List<MessageBoardVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result.getTotal(), query.getPage(), query.getSize(), records);
    }

    @Override
    public void audit(Long id, Integer status) {
        MessageBoard message = getByIdOrThrow(id);
        message.setStatus(status);
        this.updateById(message);
    }

    @Override
    public void updateHidden(Long id, Integer hidden) {
        MessageBoard message = getByIdOrThrow(id);
        message.setIsHidden(hidden);
        this.updateById(message);
    }

    @Override
    public MessageBoardVO reply(Long id, String content) {
        MessageBoard target = getByIdOrThrow(id);
        MessageBoard reply = new MessageBoard();
        // always attach the reply to the root message
        reply.setParentId(target.getParentId() == null ? target.getId() : target.getParentId());
        reply.setNickname("管理员");
        reply.setEmail(null);
        reply.setContent(content);
        reply.setIp("");
        reply.setStatus(MessageBoard.STATUS_APPROVED);
        reply.setIsHidden(0);
        reply.setIsAdmin(1);
        this.save(reply);
        return toVO(reply);
    }

    @Override
    public void delete(Long id) {
        getByIdOrThrow(id);
        this.removeById(id);
    }

    private MessageBoard getByIdOrThrow(Long id) {
        MessageBoard message = this.getById(id);
        if (message == null) {
            throw BizException.notFound();
        }
        return message;
    }

    private MessageBoardVO toVO(MessageBoard message) {
        MessageBoardVO vo = new MessageBoardVO();
        vo.setId(message.getId());
        vo.setParentId(message.getParentId());
        vo.setNickname(message.getNickname());
        vo.setEmail(message.getEmail());
        vo.setContent(message.getContent());
        vo.setIp(message.getIp());
        vo.setStatus(message.getStatus());
        vo.setIsHidden(message.getIsHidden());
        vo.setIsAdmin(message.getIsAdmin());
        vo.setCreateTime(message.getCreateTime());
        return vo;
    }
}
