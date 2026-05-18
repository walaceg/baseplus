package com.baseplus.shared.dto;

import java.util.List;

import org.springframework.data.domain.Page;

public record PageResponse<T>(
        List<T> content,
        int page,
        int size,
        long totalElements,
        int totalPages
) {

    public static <T> PageResponse<T> from(Page<?> page, List<T> content) {
        return new PageResponse<>(
                content == null ? List.of() : content,
                page == null ? 0 : page.getNumber(),
                page == null ? 0 : page.getSize(),
                page == null ? 0L : page.getTotalElements(),
                page == null ? 0 : page.getTotalPages()
        );
    }
}
