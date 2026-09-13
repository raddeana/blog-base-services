package com.blog.base.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class NotificationDTO implements Serializable {

    private Long id;
    private Long userId;
    private String title;
    private String content;
    private Integer type;
    private Integer isRead;
}
