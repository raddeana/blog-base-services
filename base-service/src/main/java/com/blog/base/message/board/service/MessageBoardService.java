package com.blog.base.message.board.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.base.common.api.PageResult;
import com.blog.base.message.board.dto.*;
import com.blog.base.message.board.entity.MessageBoard;

/**
 * Visitor message board service.
 */
public interface MessageBoardService extends IService<MessageBoard> {

    MessageBoardVO post(MessagePostDTO dto, String ip);

    PageResult<MessageBoardVO> pagePublic(MessageBoardQueryDTO query);

    PageResult<MessageBoardVO> pageForAdmin(MessageBoardQueryDTO query);

    void audit(Long id, Integer status);

    void updateHidden(Long id, Integer hidden);

    MessageBoardVO reply(Long id, String content);

    void delete(Long id);
}
