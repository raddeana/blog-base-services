package com.blog.base.region.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.base.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Region entity (China administrative divisions), table base_region.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_region")
public class Region extends BaseEntity {

    public static final int LEVEL_PROVINCE = 1;
    public static final int LEVEL_CITY = 2;
    public static final int LEVEL_DISTRICT = 3;

    /** 父级ID，0=省级根 */
    private Long parentId;

    /** 行政区划码（GB/T 2260；直筒子市下辖区县为9位扩展码） */
    private String code;

    /** 名称 */
    private String name;

    /** 层级：1省 2市 3区县 */
    private Integer level;

    /** 排序 */
    private Integer sort;
}
