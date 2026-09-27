package com.pb.crm.commons.web;

import com.pb.crm.commons.domain.PageResult;
import org.springframework.data.domain.Page;

import java.util.List;
import java.util.function.Function;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages,
        boolean first,
        boolean last
) {
    public static <E, T> PageResponse<T> from(Page<E> page, Function<E, T> mapper) {
        return new PageResponse<>(
                page.getContent().stream().map(mapper).toList(),
                page.getNumber(),
                page.getSize(),
                page.getTotalElements(),
                page.getTotalPages(),
                page.isFirst(),
                page.isLast()
        );
    }

    public static <T> PageResponse<T> from(PageResult<T> result) {
        int totalPages = result.totalPages();
        return new PageResponse<>(
                result.content(),
                result.page(),
                result.size(),
                result.totalElements(),
                totalPages,
                result.page() == 0,
                totalPages == 0 || result.page() >= totalPages - 1
        );
    }
}
