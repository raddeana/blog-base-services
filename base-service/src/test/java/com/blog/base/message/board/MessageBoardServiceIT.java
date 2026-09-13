package com.blog.base.message.board;

import com.blog.base.common.api.PageResult;
import com.blog.base.it.IntegrationTest;
import com.blog.base.message.board.dto.MessageBoardQueryDTO;
import com.blog.base.message.board.dto.MessageBoardVO;
import com.blog.base.message.board.dto.MessagePostDTO;
import com.blog.base.message.board.entity.MessageBoard;
import com.blog.base.message.board.service.MessageBoardService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import static org.assertj.core.api.Assertions.assertThat;

/**
 * Integration tests for the message board audit workflow and public tree,
 * exercised through real SQL conditions (status / is_hidden / parent_id).
 */
@IntegrationTest
class MessageBoardServiceIT {

    @Autowired
    private MessageBoardService messageBoardService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void post_shouldBePendingAndInvisiblePublicly() {
        MessageBoardVO posted = messageBoardService.post(postDTO("visitor", "hello"), "10.0.0.9");

        assertThat(posted.getStatus()).isEqualTo(MessageBoard.STATUS_PENDING);
        assertThat(pagePublic().getRecords()).isEmpty();

        String ip = jdbcTemplate.queryForObject(
                "SELECT ip FROM base_message_board WHERE id = ?", String.class, posted.getId());
        assertThat(ip).isEqualTo("10.0.0.9");
    }

    @Test
    void approveThenReply_shouldAppearAsTreeInPublicList() {
        MessageBoardVO root = messageBoardService.post(postDTO("visitor", "root"), "10.0.0.1");
        // pending reply-like rows must not show before approval either
        messageBoardService.reply(root.getId(), "waiting"); // admin reply is auto-approved
        messageBoardService.audit(root.getId(), MessageBoard.STATUS_APPROVED);

        PageResult<MessageBoardVO> page = pagePublic();

        assertThat(page.getTotal()).isEqualTo(1);
        MessageBoardVO publicRoot = page.getRecords().get(0);
        assertThat(publicRoot.getContent()).isEqualTo("root");
        assertThat(publicRoot.getChildren()).hasSize(1);
        MessageBoardVO reply = publicRoot.getChildren().get(0);
        assertThat(reply.getIsAdmin()).isEqualTo(1);
        assertThat(reply.getParentId()).isEqualTo(root.getId());
    }

    @Test
    void hidden_shouldDisappearFromPublicList() {
        MessageBoardVO root = messageBoardService.post(postDTO("visitor", "hide-me"), "10.0.0.1");
        messageBoardService.audit(root.getId(), MessageBoard.STATUS_APPROVED);
        assertThat(pagePublic().getTotal()).isEqualTo(1);

        messageBoardService.updateHidden(root.getId(), 1);

        assertThat(pagePublic().getRecords()).isEmpty();
    }

    @Test
    void rejected_shouldNotAppearPublicly() {
        MessageBoardVO root = messageBoardService.post(postDTO("visitor", "reject-me"), "10.0.0.1");
        messageBoardService.audit(root.getId(), MessageBoard.STATUS_REJECTED);

        assertThat(pagePublic().getRecords()).isEmpty();
    }

    private PageResult<MessageBoardVO> pagePublic() {
        MessageBoardQueryDTO query = new MessageBoardQueryDTO();
        query.setPage(1);
        query.setSize(10);
        return messageBoardService.pagePublic(query);
    }

    private MessagePostDTO postDTO(String nickname, String content) {
        MessagePostDTO dto = new MessagePostDTO();
        dto.setNickname(nickname);
        dto.setEmail(nickname + "@example.com");
        dto.setContent(content);
        return dto;
    }
}
