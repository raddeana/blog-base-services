package com.blog.base.common.api;

import lombok.Getter;

import java.util.List;

/**
 * Pagination response.
 */
@Getter
public class PageResult<T> {

    private final long total;
    private final long page;
    private final long size;
    private final List<T> records;

    private PageResult(long total, long page, long size, List<T> records) {
        this.total = total;
        this.page = page;
        this.size = size;
        this.records = records;
    }

    public static <T> PageResult<T> of(long total, long page, long size, List<T> records) {
        return new PageResult<>(total, page, size, records);
    }
}
