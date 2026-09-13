package com.blog.base.tag.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.base.common.api.PageResult;
import com.blog.base.tag.dto.*;
import com.blog.base.tag.entity.Tag;

import java.util.List;

/**
 * Tag service.
 */
public interface TagService extends IService<Tag> {

    PageResult<TagVO> page(TagQueryDTO query);

    List<TagVO> listEnabled();

    TagVO detail(Long id);

    TagVO create(TagCreateDTO dto);

    TagVO update(Long id, TagUpdateDTO dto);

    void updateStatus(Long id, Integer status);

    void delete(Long id);
}
