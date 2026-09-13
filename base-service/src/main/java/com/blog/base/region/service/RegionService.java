package com.blog.base.region.service;

import com.baomidou.mybatisplus.extension.service.IService;
import com.blog.base.region.dto.RegionTreeNode;
import com.blog.base.region.dto.RegionVO;
import com.blog.base.region.entity.Region;

import java.util.List;

/**
 * Region service.
 */
public interface RegionService extends IService<Region> {

    List<RegionVO> listProvinces();

    List<RegionVO> listChildren(Long parentId);

    List<RegionTreeNode> subtree(Long parentId);

    List<RegionTreeNode> fullTree();

    RegionVO getByCode(String code);
}
