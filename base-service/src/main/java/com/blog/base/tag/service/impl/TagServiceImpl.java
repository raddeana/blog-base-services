package com.blog.base.tag.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.base.common.api.ErrorCode;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.tag.dto.*;
import com.blog.base.tag.entity.Tag;
import com.blog.base.tag.mapper.TagMapper;
import com.blog.base.tag.service.TagService;
import org.springframework.stereotype.Service;
import org.springframework.util.StringUtils;

import java.util.List;

/**
 * Tag service implementation.
 */
@Service
public class TagServiceImpl extends ServiceImpl<TagMapper, Tag> implements TagService {

    @Override
    public PageResult<TagVO> page(TagQueryDTO query) {
        LambdaQueryWrapper<Tag> wrapper = new LambdaQueryWrapper<Tag>()
                .like(StringUtils.hasText(query.getKeyword()), Tag::getName, query.getKeyword())
                .eq(query.getStatus() != null, Tag::getStatus, query.getStatus())
                .orderByAsc(Tag::getSort)
                .orderByDesc(Tag::getId);
        Page<Tag> result = this.page(new Page<>(query.getPage(), query.getSize()), wrapper);
        List<TagVO> records = result.getRecords().stream().map(this::toVO).toList();
        return PageResult.of(result.getTotal(), query.getPage(), query.getSize(), records);
    }

    @Override
    public List<TagVO> listEnabled() {
        return this.list(new LambdaQueryWrapper<Tag>()
                        .eq(Tag::getStatus, Tag.STATUS_ENABLED)
                        .orderByAsc(Tag::getSort)
                        .orderByDesc(Tag::getId))
                .stream().map(this::toVO).toList();
    }

    @Override
    public TagVO detail(Long id) {
        return toVO(getByIdOrThrow(id));
    }

    @Override
    public TagVO create(TagCreateDTO dto) {
        checkNameDuplicate(dto.getName(), null);
        Tag tag = new Tag();
        tag.setName(dto.getName());
        tag.setStatus(Tag.STATUS_ENABLED);
        tag.setSort(dto.getSort() == null ? 0 : dto.getSort());
        tag.setRemark(dto.getRemark());
        this.save(tag);
        return toVO(tag);
    }

    @Override
    public TagVO update(Long id, TagUpdateDTO dto) {
        Tag tag = getByIdOrThrow(id);
        checkNameDuplicate(dto.getName(), id);
        tag.setName(dto.getName());
        tag.setSort(dto.getSort() == null ? 0 : dto.getSort());
        tag.setRemark(dto.getRemark());
        this.updateById(tag);
        return toVO(tag);
    }

    @Override
    public void updateStatus(Long id, Integer status) {
        Tag tag = getByIdOrThrow(id);
        if (status == null || (status != Tag.STATUS_ENABLED && status != Tag.STATUS_DISABLED)) {
            throw new BizException(ErrorCode.PARAM_ERROR, "status 只能为 0 或 1");
        }
        tag.setStatus(status);
        this.updateById(tag);
    }

    @Override
    public void delete(Long id) {
        getByIdOrThrow(id);
        this.removeById(id);
    }

    private Tag getByIdOrThrow(Long id) {
        Tag tag = this.getById(id);
        if (tag == null) {
            throw BizException.notFound();
        }
        return tag;
    }

    private void checkNameDuplicate(String name, Long excludeId) {
        long count = this.count(new LambdaQueryWrapper<Tag>()
                .eq(Tag::getName, name)
                .ne(excludeId != null, Tag::getId, excludeId));
        if (count > 0) {
            throw new BizException(ErrorCode.DUPLICATE_NAME, "标签名已存在: " + name);
        }
    }

    private TagVO toVO(Tag tag) {
        TagVO vo = new TagVO();
        vo.setId(tag.getId());
        vo.setName(tag.getName());
        vo.setStatus(tag.getStatus());
        vo.setSort(tag.getSort());
        vo.setRemark(tag.getRemark());
        vo.setCreateTime(tag.getCreateTime());
        vo.setUpdateTime(tag.getUpdateTime());
        return vo;
    }
}
