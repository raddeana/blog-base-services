package com.blog.base.tag.entity;

import com.baomidou.mybatisplus.annotation.TableName;
import com.blog.base.common.entity.BaseEntity;
import lombok.Data;
import lombok.EqualsAndHashCode;

/**
 * Tag entity, table base_tag.
 */
@Data
@EqualsAndHashCode(callSuper = true)
@TableName("base_tag")
public class Tag extends BaseEntity {

    public static final int STATUS_ENABLED = 1;
    public static final int STATUS_DISABLED = 0;

    /** 标签名 */
    private String name;

    /** 状态：1启用 0禁用 */
    private Integer status;

    /** 排序，越小越前 */
    private Integer sort;

    /** 备注 */
    private String remark;
}
