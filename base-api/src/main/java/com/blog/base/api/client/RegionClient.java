package com.blog.base.api.client;

import com.blog.base.api.dto.ApiResult;
import com.blog.base.api.dto.RegionDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;

import java.util.List;

@FeignClient(name = "blog-base-services", path = "/api/v1", contextId = "regionClient")
public interface RegionClient {

    @GetMapping("/regions/provinces")
    ApiResult<List<RegionDTO>> listProvinces();

    @GetMapping("/regions/code/{code}")
    ApiResult<RegionDTO> byCode(@PathVariable("code") String code);
}
