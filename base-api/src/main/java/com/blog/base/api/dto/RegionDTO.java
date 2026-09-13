package com.blog.base.api.dto;

import lombok.Data;

import java.io.Serializable;

@Data
public class RegionDTO implements Serializable {

    private Long id;
    private Long parentId;
    private String code;
    private String name;
    private Integer level;
}
