package com.blog.base.common.api;

import jakarta.validation.constraints.Max;
import jakarta.validation.constraints.Min;
import lombok.Data;

/**
 * Pagination request.
 */
@Data
public class PageQuery {

    @Min(value = 1, message = "页码最小为1")
    private int page = 1;

    @Min(value = 1, message = "每页条数最小为1")
    @Max(value = 200, message = "每页条数最大为200")
    private int size = 10;
}
