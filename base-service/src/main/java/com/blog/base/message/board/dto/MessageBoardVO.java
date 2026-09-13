package com.blog.base.message.board.dto;

import io.swagger.v3.oas.annotations.media.Schema;
import lombok.Data;

import java.time.LocalDateTime;
import java.util.ArrayList;
import java.util.List;

@Data
@Schema(description = "留言信息")
public class MessageBoardVO {

    @Schema(description = "ID", example = "1")
    private Long id;

    @Schema(description = "回复的根留言ID，null=顶层", example = "1")
    private Long parentId;

    @Schema(description = "访客昵称", example = "张三")
    private String nickname;

    @Schema(description = "访客邮箱")
    private String email;

    @Schema(description = "留言内容")
    private String content;

    @Schema(description = "留言IP")
    private String ip;

    @Schema(description = "审核状态：0待审 1通过 2驳回", example = "1")
    private Integer status;

    @Schema(description = "是否隐藏：1隐藏", example = "0")
    private Integer isHidden;

    @Schema(description = "是否管理员回复：1是", example = "0")
    private Integer isAdmin;

    @Schema(description = "创建时间")
    private LocalDateTime createTime;

    @Schema(description = "回复列表（公开树接口使用）")
    private List<MessageBoardVO> children = new ArrayList<>();
}
