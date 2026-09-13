package com.blog.base.message.board.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.base.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Visitor message board entity, table base_message_board.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_message_board")
public class MessageBoard extends BaseEntity {

    public static final int STATUS_PENDING = 0;
    public static final int STATUS_APPROVED = 1;
    public static final int STATUS_REJECTED = 2;

    /** 回复的根留言ID，NULL=顶层留言 */
    private Long parentId;

    /** 访客昵称 */
    private String nickname;

    /** 访客邮箱 */
    private String email;

    /** 留言内容 */
    private String content;

    /** 留言IP（IPv4/IPv6） */
    private String ip;

    /** 审核状态：0待审 1通过 2驳回 */
    private Integer status;

    /** 是否隐藏：1隐藏 */
    private Integer isHidden;

    /** 是否管理员回复：1是 */
    private Integer isAdmin;
}
