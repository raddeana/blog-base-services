package com.blog.base.region.service.impl;

import com.baomidou.mybatisplus.core.conditions.query.LambdaQueryWrapper;
import com.baomidou.mybatisplus.extension.service.impl.ServiceImpl;
import com.blog.base.common.exception.BizException;
import com.blog.base.region.dto.RegionTreeNode;
import com.blog.base.region.dto.RegionVO;
import com.blog.base.region.entity.Region;
import com.blog.base.region.mapper.RegionMapper;
import com.blog.base.region.service.RegionService;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Map;
import java.util.function.Function;
import java.util.stream.Collectors;

/**
 * Region service implementation.
 * Region data is small (about 3200 rows), so tree building loads all rows once.
 */
@Service
public class RegionServiceImpl extends ServiceImpl<RegionMapper, Region> implements RegionService {

    private volatile List<RegionTreeNode> cachedFullTree;

    @Override
    public List<RegionVO> listProvinces() {
        return this.list(new LambdaQueryWrapper<Region>()
                        .eq(Region::getLevel, Region.LEVEL_PROVINCE)
                        .orderByAsc(Region::getSort))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<RegionVO> listChildren(Long parentId) {
        return this.list(new LambdaQueryWrapper<Region>()
                        .eq(Region::getParentId, parentId)
                        .orderByAsc(Region::getSort))
                .stream().map(this::toVO).toList();
    }

    @Override
    public List<RegionTreeNode> subtree(Long parentId) {
        if (parentId == null || parentId < 0) {
            parentId = 0L;
        }
        return buildChildren(parentId, loadAllAsMap());
    }

    @Override
    public List<RegionTreeNode> fullTree() {
        List<RegionTreeNode> tree = cachedFullTree;
        if (tree == null) {
            synchronized (this) {
                tree = cachedFullTree;
                if (tree == null) {
                    tree = subtree(0L);
                    cachedFullTree = tree;
                }
            }
        }
        return tree;
    }

    @Override
    public RegionVO getByCode(String code) {
        Region region = this.list(new LambdaQueryWrapper<Region>().eq(Region::getCode, code))
                .stream().findFirst().orElseThrow(BizException::notFound);
        return toVO(region);
    }

    private Map<Long, List<Region>> loadAllAsMap() {
        return this.list(new LambdaQueryWrapper<Region>().orderByAsc(Region::getSort))
                .stream()
                .collect(Collectors.groupingBy(Region::getParentId, Collectors.mapping(Function.identity(), Collectors.toList())));
    }

    private List<RegionTreeNode> buildChildren(Long parentId, Map<Long, List<Region>> grouped) {
        List<Region> children = grouped.get(parentId);
        if (children == null || children.isEmpty()) {
            return List.of();
        }
        return children.stream()
                .map(region -> {
                    RegionTreeNode node = new RegionTreeNode();
                    node.setId(region.getId());
                    node.setParentId(region.getParentId());
                    node.setCode(region.getCode());
                    node.setName(region.getName());
                    node.setLevel(region.getLevel());
                    node.setChildren(buildChildren(region.getId(), grouped));
                    return node;
                })
                .toList();
    }

    private RegionVO toVO(Region region) {
        RegionVO vo = new RegionVO();
        vo.setId(region.getId());
        vo.setParentId(region.getParentId());
        vo.setCode(region.getCode());
        vo.setName(region.getName());
        vo.setLevel(region.getLevel());
        return vo;
    }
}
