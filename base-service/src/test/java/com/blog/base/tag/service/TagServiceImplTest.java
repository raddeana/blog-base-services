package com.blog.base.tag.service;

import com.baomidou.mybatisplus.extension.plugins.pagination.Page;
import com.blog.base.common.api.ErrorCode;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.tag.dto.TagCreateDTO;
import com.blog.base.tag.dto.TagQueryDTO;
import com.blog.base.tag.dto.TagUpdateDTO;
import com.blog.base.tag.dto.TagVO;
import com.blog.base.tag.entity.Tag;
import com.blog.base.tag.mapper.TagMapper;
import com.blog.base.tag.service.impl.TagServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.never;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link TagServiceImpl}. MyBatis-Plus ServiceImpl terminal
 * operations all delegate to the injected BaseMapper, which is mocked.
 */
@ExtendWith(MockitoExtension.class)
class TagServiceImplTest {

    @Mock
    private TagMapper tagMapper;

    @InjectMocks
    private TagServiceImpl tagService;

    @BeforeEach
    void injectBaseMapper() {
        // ServiceImpl#baseMapper is a generic superclass field that @InjectMocks
        // does not populate reliably; inject the mock explicitly.
        ReflectionTestUtils.setField(tagService, "baseMapper", tagMapper);
    }

    @Test
    void create_shouldSaveEnabledTag() {
        // duplicate-name check returns 0, insert backfills id
        when(tagMapper.selectCount(any())).thenReturn(0L);
        when(tagMapper.insert(any(Tag.class))).thenAnswer(invocation -> {
            invocation.getArgument(0, Tag.class).setId(1L);
            return 1;
        });

        TagCreateDTO dto = new TagCreateDTO();
        dto.setName("Java");
        dto.setSort(2);
        dto.setRemark("backend");

        TagVO vo = tagService.create(dto);

        assertThat(vo.getId()).isEqualTo(1L);
        assertThat(vo.getName()).isEqualTo("Java");
        assertThat(vo.getStatus()).isEqualTo(Tag.STATUS_ENABLED);
        assertThat(vo.getSort()).isEqualTo(2);

        ArgumentCaptor<Tag> captor = ArgumentCaptor.forClass(Tag.class);
        verify(tagMapper).insert(captor.capture());
        assertThat(captor.getValue().getStatus()).isEqualTo(Tag.STATUS_ENABLED);
    }

    @Test
    void create_duplicateName_shouldThrow() {
        when(tagMapper.selectCount(any())).thenReturn(1L);

        TagCreateDTO dto = new TagCreateDTO();
        dto.setName("Java");

        assertThatThrownBy(() -> tagService.create(dto))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_NAME);

        verify(tagMapper, never()).insert(any(Tag.class));
    }

    @Test
    void page_shouldReturnMappedRecords() {
        Tag tag = tag(1L, "Java", Tag.STATUS_ENABLED);
        when(tagMapper.selectPage(any(Page.class), any())).thenAnswer(invocation -> {
            Page<Tag> page = invocation.getArgument(0);
            page.setRecords(List.of(tag));
            page.setTotal(1);
            return page;
        });

        TagQueryDTO query = new TagQueryDTO();
        query.setPage(1);
        query.setSize(10);

        PageResult<TagVO> result = tagService.page(query);

        assertThat(result.getTotal()).isEqualTo(1);
        assertThat(result.getRecords()).hasSize(1);
        assertThat(result.getRecords().get(0).getName()).isEqualTo("Java");
    }

    @Test
    void listEnabled_shouldMapResults() {
        when(tagMapper.selectList(any())).thenReturn(List.of(
                tag(1L, "Java", Tag.STATUS_ENABLED),
                tag(2L, "Spring", Tag.STATUS_ENABLED)));

        List<TagVO> result = tagService.listEnabled();

        assertThat(result).extracting(TagVO::getName).containsExactly("Java", "Spring");
    }

    @Test
    void detail_notFound_shouldThrow() {
        when(tagMapper.selectById(99L)).thenReturn(null);

        assertThatThrownBy(() -> tagService.detail(99L))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }

    @Test
    void update_shouldExcludeSelfWhenCheckingName() {
        when(tagMapper.selectById(1L)).thenReturn(tag(1L, "Old", Tag.STATUS_ENABLED));
        when(tagMapper.selectCount(any())).thenReturn(0L);
        when(tagMapper.updateById(any(Tag.class))).thenReturn(1);

        TagUpdateDTO dto = new TagUpdateDTO();
        dto.setName("New");
        dto.setSort(5);

        TagVO vo = tagService.update(1L, dto);

        assertThat(vo.getName()).isEqualTo("New");
        assertThat(vo.getSort()).isEqualTo(5);
        verify(tagMapper).updateById(any(Tag.class));
    }

    @Test
    void updateStatus_invalidStatus_shouldThrow() {
        when(tagMapper.selectById(1L)).thenReturn(tag(1L, "Java", Tag.STATUS_ENABLED));

        assertThatThrownBy(() -> tagService.updateStatus(1L, 9))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.PARAM_ERROR);

        verify(tagMapper, never()).updateById(any(Tag.class));
    }

    @Test
    void delete_shouldRemoveExistingTag() {
        when(tagMapper.selectById(1L)).thenReturn(tag(1L, "Java", Tag.STATUS_ENABLED));
        when(tagMapper.deleteById(1L)).thenReturn(1);

        tagService.delete(1L);

        verify(tagMapper, times(1)).deleteById(1L);
    }

    private Tag tag(Long id, String name, Integer status) {
        Tag tag = new Tag();
        tag.setId(id);
        tag.setName(name);
        tag.setStatus(status);
        tag.setSort(0);
        return tag;
    }
}
