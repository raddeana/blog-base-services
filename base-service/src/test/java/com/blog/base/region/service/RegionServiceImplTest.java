package com.blog.base.region.service;

import com.blog.base.common.api.ErrorCode;
import com.blog.base.common.exception.BizException;
import com.blog.base.region.dto.RegionTreeNode;
import com.blog.base.region.dto.RegionVO;
import com.blog.base.region.entity.Region;
import com.blog.base.region.mapper.RegionMapper;
import com.blog.base.region.service.impl.RegionServiceImpl;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;
import org.springframework.test.util.ReflectionTestUtils;

import java.util.List;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.times;
import static org.mockito.Mockito.verify;
import static org.mockito.Mockito.when;

/**
 * Unit tests for {@link RegionServiceImpl}: tree assembly, code lookup
 * and the in-process full-tree cache.
 */
@ExtendWith(MockitoExtension.class)
class RegionServiceImplTest {

    @Mock
    private RegionMapper regionMapper;

    @InjectMocks
    private RegionServiceImpl regionService;

    @BeforeEach
    void injectBaseMapper() {
        ReflectionTestUtils.setField(regionService, "baseMapper", regionMapper);
    }

    @Test
    void listProvinces_shouldReturnLevel1Sorted() {
        when(regionMapper.selectList(any())).thenReturn(List.of(
                region(1L, 0L, "110000", "北京市", Region.LEVEL_PROVINCE),
                region(4L, 0L, "310000", "上海市", Region.LEVEL_PROVINCE)));

        List<RegionVO> result = regionService.listProvinces();

        assertThat(result).hasSize(2);
        assertThat(result).extracting(RegionVO::getName).containsExactly("北京市", "上海市");
    }

    @Test
    void fullTree_shouldBuildThreeLevelTree() {
        when(regionMapper.selectList(any())).thenReturn(sampleRegions());

        List<RegionTreeNode> tree = regionService.fullTree();

        // two provinces
        assertThat(tree).hasSize(2);
        RegionTreeNode beijing = tree.get(0);
        assertThat(beijing.getName()).isEqualTo("北京市");
        assertThat(beijing.getChildren()).hasSize(1);
        RegionTreeNode city = beijing.getChildren().get(0);
        assertThat(city.getLevel()).isEqualTo(Region.LEVEL_CITY);
        assertThat(city.getChildren()).hasSize(1);
        assertThat(city.getChildren().get(0).getName()).isEqualTo("东城区");
    }

    @Test
    void fullTree_secondCallShouldUseCache() {
        when(regionMapper.selectList(any())).thenReturn(sampleRegions());

        regionService.fullTree();
        regionService.fullTree();

        verify(regionMapper, times(1)).selectList(any());
    }

    @Test
    void subtree_byCityId_shouldReturnDistricts() {
        when(regionMapper.selectList(any())).thenReturn(sampleRegions());

        List<RegionTreeNode> children = regionService.subtree(2L);

        assertThat(children).hasSize(1);
        assertThat(children.get(0).getName()).isEqualTo("东城区");
        assertThat(children.get(0).getLevel()).isEqualTo(Region.LEVEL_DISTRICT);
    }

    @Test
    void getByCode_found_shouldReturnRegion() {
        Region dongcheng = region(3L, 2L, "110101", "东城区", Region.LEVEL_DISTRICT);
        when(regionMapper.selectList(any())).thenReturn(List.of(dongcheng));

        RegionVO vo = regionService.getByCode("110101");

        assertThat(vo.getName()).isEqualTo("东城区");
        assertThat(vo.getCode()).isEqualTo("110101");
    }

    @Test
    void getByCode_missing_shouldThrowNotFound() {
        when(regionMapper.selectList(any())).thenReturn(List.of());

        assertThatThrownBy(() -> regionService.getByCode("999999"))
                .isInstanceOf(BizException.class)
                .extracting(e -> ((BizException) e).getErrorCode())
                .isEqualTo(ErrorCode.DATA_NOT_FOUND);
    }

    private List<Region> sampleRegions() {
        return List.of(
                region(1L, 0L, "110000", "北京市", Region.LEVEL_PROVINCE),
                region(2L, 1L, "110100", "市辖区", Region.LEVEL_CITY),
                region(3L, 2L, "110101", "东城区", Region.LEVEL_DISTRICT),
                region(4L, 0L, "310000", "上海市", Region.LEVEL_PROVINCE));
    }

    private Region region(Long id, Long parentId, String code, String name, int level) {
        Region r = new Region();
        r.setId(id);
        r.setParentId(parentId);
        r.setCode(code);
        r.setName(name);
        r.setLevel(level);
        r.setSort(1);
        return r;
    }
}
