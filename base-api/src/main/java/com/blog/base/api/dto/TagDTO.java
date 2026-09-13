package com.blog.base.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class TagDTO implements Serializable {

    private Long id;
    private String name;
    private Integer status;
    private Integer sort;
}
