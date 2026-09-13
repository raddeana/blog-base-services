package com.blog.base.api.client;

import com.blog.base.api.dto.ApiResult;
import com.blog.base.api.dto.TagDTO;
import org.springframework.cloud.openfeign.FeignClient;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.PathVariable;
import org.springframework.web.bind.annotation.PostMapping;
import org.springframework.web.bind.annotation.RequestBody;

import java.util.List;

/**
 * Tag service contract. Consumers: register with @EnableFeignClients and
 * bring spring-cloud-starter-openfeign + spring-cloud-starter-loadbalancer.
 */
@FeignClient(name = "blog-base-services", path = "/api/v1", contextId = "tagClient")
public interface TagClient {

    @GetMapping("/tags/all")
    ApiResult<List<TagDTO>> listAll();

    @GetMapping("/tags/{id}")
    ApiResult<TagDTO> detail(@PathVariable("id") Long id);

    @PostMapping("/tags")
    ApiResult<TagDTO> create(@RequestBody TagDTO dto);
}
