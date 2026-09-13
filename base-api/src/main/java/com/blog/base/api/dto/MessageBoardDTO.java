package com.blog.base.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class MessageBoardDTO implements Serializable {

    private Long id;
    private Long parentId;
    private String nickname;
    private String content;
    private Integer status;
    private Integer isHidden;
    private Integer isAdmin;
}
