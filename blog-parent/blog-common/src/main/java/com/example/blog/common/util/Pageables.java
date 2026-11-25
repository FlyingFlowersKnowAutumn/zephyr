package com.example.blog.common.util;

import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * 统一的分页参数收敛，避免外部传入任意 size 把数据库打挂。
 */
public final class Pageables {

    public static final int DEFAULT_SIZE = 10;
    /**
     * 上限 100：归档页会一次请求 size=100 拉全年文章，再大就没有合理用途了，
     * 同时避免 size=10000 这类请求把整表读进内存。
     */
    public static final int MAX_SIZE = 100;

    private Pageables() {
    }

    public static Pageable of(int page, int size, Sort sort) {
        int safePage = Math.max(0, page);
        int safeSize = size <= 0 ? DEFAULT_SIZE : Math.min(size, MAX_SIZE);
        return PageRequest.of(safePage, safeSize, sort);
    }
}
