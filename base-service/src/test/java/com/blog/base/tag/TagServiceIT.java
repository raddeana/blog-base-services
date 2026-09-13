package com.blog.base.tag;

import com.blog.base.common.api.ErrorCode;
import com.blog.base.common.api.PageResult;
import com.blog.base.common.exception.BizException;
import com.blog.base.it.IntegrationTest;
import com.blog.base.tag.dto.TagCreateDTO;
import com.blog.base.tag.dto.TagQueryDTO;
import com.blog.base.tag.dto.TagVO;
import com.blog.base.tag.service.TagService;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.jdbc.core.JdbcTemplate;

import java.util.Map;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

/**
 * Integration tests against embedded H2: verifies real MyBatis-Plus behaviour —
 * auto-fill of timestamps, logical delete SQL rewriting and the pagination
 * interceptor — which mocked mapper unit tests cannot cover.
 */
@IntegrationTest
class TagServiceIT {

    @Autowired
    private TagService tagService;

    @Autowired
    private JdbcTemplate jdbcTemplate;

    @Test
    void create_shouldAutoFillTimestampsAndDeletedFlag() {
        TagVO vo = tagService.create(createDTO("IT-Java", 1));

        Map<String, Object> row = jdbcTemplate.queryForMap(
                "SELECT create_time, update_time, deleted, status FROM base_tag WHERE id = ?", vo.getId());

        assertThat(row.get("CREATE_TIME")).as("createTime auto-filled").isNotNull();
        assertThat(row.get("UPDATE_TIME")).as("updateTime auto-filled").isNotNull();
        assertThat(((Number) row.get("DELETED")).intValue()).isZero();
        assertThat(((Number) row.get("STATUS")).intValue()).isEqualTo(1);
    }

    @Test
    void delete_shouldBeLogicalDelete() {
        TagVO vo = tagService.create(createDTO("IT-ToDelete", 1));

        tagService.delete(vo.getId());

        // invisible through MP (logic-delete filter active)
        assertThat(tagService.getById(vo.getId())).isNull();
        // row still physically present, flagged deleted=1
        Integer deleted = jdbcTemplate.queryForObject(
                "SELECT deleted FROM base_tag WHERE id = ?", Integer.class, vo.getId());
        assertThat(deleted).isEqualTo(1);
    }

    @Test
    void page_shouldUseRealPaginationInterceptor() {
        tagService.create(createDTO("IT-A", 3));
        tagService.create(createDTO("IT-B", 1));
        tagService.create(createDTO("IT-C", 2));

        TagQueryDTO query = new TagQueryDTO();
        query.setPage(1);
        query.setSize(2);

        PageResult<TagVO> page = tagService.page(query);

        assertThat(page.getTotal()).isEqualTo(3);
        assertThat(page.getRecords()).hasSize(2);
        // ordered by sort asc: B(1), C(2)
        assertThat(page.getRecords()).extracting(TagVO::getName).containsExactly("IT-B", "IT-C");
    }

    @Test
    void create_duplicateName_shouldBeRejectedByRealCountQuery() {
        tagService.create(createDTO("IT-Dup", 1));

        assertThatThrownBy(() -> tagService.create(createDTO("IT-Dup", 1)))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.DUPLICATE_NAME);
    }

    @Test
    void listEnabled_shouldExcludeDisabledTags() {
        TagVO enabled = tagService.create(createDTO("IT-Enabled", 1));
        TagVO disabled = tagService.create(createDTO("IT-Disabled", 1));
        tagService.updateStatus(disabled.getId(), 0);

        assertThat(tagService.listEnabled())
                .extracting(TagVO::getId)
                .contains(enabled.getId())
                .doesNotContain(disabled.getId());
    }

    private TagCreateDTO createDTO(String name, int sort) {
        TagCreateDTO dto = new TagCreateDTO();
        dto.setName(name);
        dto.setSort(sort);
        return dto;
    }
}
