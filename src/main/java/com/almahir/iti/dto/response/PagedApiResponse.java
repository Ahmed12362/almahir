package com.almahir.iti.dto.response;

import com.almahir.iti.model.PageMeta;

import java.time.LocalDateTime;
import java.util.List;

public record PagedApiResponse<T>(
        boolean success,
        String message,
        T data,
        PageMeta pagination,
        LocalDateTime timestamp
) {
    public static <T> PagedApiResponse<T> success(String message, T data, PageMeta pagination) {
        return new PagedApiResponse<>(true, message, data, pagination, LocalDateTime.now());
    }
}