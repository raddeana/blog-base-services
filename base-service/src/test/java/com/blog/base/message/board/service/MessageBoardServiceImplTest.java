package com.blog.base.message.board.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.base.common.api.ErrorCode;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.message.board.dto.MessageBoardQueryDTO;
import com.blog.base.message.board.dto.MessageBoardVO;
import com.blog.base.message.board.dto.MessagePostDTO;
import com.blog.base.message.board.entity.MessageBoard;
import com.blog.base.message.board.mapper.MessageBoardMapper;
import com.blog.base.message.board.service.impl.MessageBoardServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link MessageBoardServiceImpl}.
 */
@ExtendWith(MockitoExtension.class)
class MessageBoardServiceImplTest {

    @Mock
    private MessageBoardMapper messageBoardMapper;

    @InjectMocks
    private MessageBoardServiceImpl messageBoardService;

    @BeforeEach
    void injectBaseMapper() {
        ReflectionTestUtils.setField(messageBoardService, "baseMapper", messageBoardMapper);
    }

    @Test
    void post_shouldCreatePendingMessageWithServerIp() {
        when(messageBoardMapper.insert(any(MessageBoard.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, MessageBoard.class).setId(1L);
            return 1;
        });

        MessagePostDTO dto = new MessagePostDTO();
        dto.setNickname("visitor");
        dto.setEmail("v@example.com");
        dto.setContent("nice blog");

        MessageBoardVO vo = messageBoardService.post(dto, "10.0.0.1");

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getStatus()).isEqualTo(MessageBoard.STATUS_PENDING);
        assertThat(vo.getIsAdmin()).isZero();
        assertThat(vo.getParentId()).isNull();

        ArgumentCaptor<MessageBoard> captor = ArgumentCaptor.forClass(MessageBoard.class);
        verify(messageBoardMapper).insert(captor.capture());
        MessageBoard saved = captor.getValue();
        assertThat(saved.getIp()).isEqualTo("10.0.0.1");
        assertThat(saved.getStatus()).isEqualTo(MessageBoard.STATUS_PENDING);
    }

    @Test
    void pagePublic_shouldAttachRepliesToTopMessage() {
        MessageBoard top = message(1L, null, MessageBoard.STATUS_APPROVED, 0);
        MessageBoard reply = message(2L, 1L, MessageBoard.STATUS_APPROVED, 1);
        when(messageBoardMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<MessageBoard> page = invocation.getArgument(0);
            page.setRecords(List.of(top));
            page.setTotal(1);
            return page;
        });
        when(messageBoardMapper.selectList(any())).thenReturn(List.of(reply));

        MessageBoardQueryDTO query = new MessageBoardQueryDTO();
        query.setPage(1);
        query.setSize(10);

        PageResult<MessageBoardVO> result = messageBoardService.pagePublic(query);

        assertThat(result.getRecords()).hasSize(1);
        MessageBoardVO root = result.getRecords().get(0);
        assertThat(root.getId()).isEqualTo(1L);
        assertThat(root.getChildren()).hasSize(1);
        assertThat(root.getChildren().get(0).getIsAdmin()).isEqualTo(1);
    }

    @Test
    void pagePublic_emptyPage_shouldNotQueryReplies() {
        when(messageBoardMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<MessageBoard> page = invocation.getArgument(0);
            page.setRecords(List.of());
            page.setTotal(0);
            return page;
        });

        MessageBoardQueryDTO query = new MessageBoardQueryDTO();
        PageResult<MessageBoardVO> result = messageBoardService.pagePublic(query);

        assertThat(result.getRecords()).isEmpty();
        verify(messageBoardMapper, never()).selectList(any());
    }

    @Test
    void audit_shouldUpdateStatus() {
        when(messageBoardMapper.selectById(1L)).thenReturn(message(1L, null, MessageBoard.STATUS_PENDING, 0));
        when(messageBoardMapper.updateById(any(MessageBoard.class))).thenReturn(1);

        messageBoardService.audit(1L, MessageBoard.STATUS_APPROVED);

        ArgumentCaptor<MessageBoard> captor = ArgumentCaptor.forClass(MessageBoard.class);
        verify(messageBoardMapper).updateById(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(MessageBoard.STATUS_APPROVED);
    }

    @Test
    void replyToTopMessage_shouldAttachToRootAndAutoApprove() {
        when(messageBoardMapper.selectById(1L)).thenReturn(message(1L, null, MessageBoard.STATUS_APPROVED, 0));
        when(messageBoardMapper.insert(any(MessageBoard.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, MessageBoard.class).setId(100L);
            return 1;
        });

        MessageBoardVO reply = messageBoardService.reply(1L, "thanks");

        assertThat(reply.getParentId()).isEqualTo(1L);
        assertThat(reply.getIsAdmin()).isEqualTo(1);
        assertThat(reply.getStatus()).isEqualTo(MessageBoard.STATUS_APPROVED);
        assertThat(reply.getNickname()).isEqualTo("管理员");
    }

    @Test
    void replyToNestedReply_shouldStillAttachToRoot() {
        when(messageBoardMapper.selectById(100L)).thenReturn(message(100L, 1L, MessageBoard.STATUS_APPROVED, 1));
        when(messageBoardMapper.insert(any(MessageBoard.class))).thenReturn(1);

        MessageBoardVO reply = messageBoardService.reply(100L, "again");

        assertThat(reply.getParentId()).isEqualTo(1L);
    }

    @Test
    void delete_notFound_shouldThrow() {
        when(messageBoardMapper.selectById(404L)).thenReturn(null);

        assertThatThrownBy(() -> messageBoardService.delete(404L))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);

        verify(messageBoardMapper, never()).deleteById(any(java.io.Serializable.class));
    }

    private MessageBoard message(Long id, Long parentId, Integer status, Integer isAdmin) {
        MessageBoard m = new MessageBoard();
        m.setId(id);
        m.setParentId(parentId);
        m.setNickname(isAdmin == 1 ? "管理员" : "visitor");
        m.setContent("content-" + id);
        m.setIp("127.0.0.1");
        m.setStatus(status);
        m.setIsHidden(0);
        m.setIsAdmin(isAdmin);
        return m;
    }
}
