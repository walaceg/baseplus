package com.baseplus.shared.dto;

import java.util.Collections;
import java.util.List;

public record ApiResponse<T>(
        boolean success,
        T data,
        String message,
        List<String> errors
) {

    public static <T> ApiResponse<T> success(T data, String message) {
        return new ApiResponse<>(true, data, message, Collections.emptyList());
    }

    public static <T> ApiResponse<T> failure(String message, List<String> errors) {
        return new ApiResponse<>(false, null, message, errors == null ? Collections.emptyList() : errors);
    }

    public static <T> ApiResponse<T> failure(String message, String error) {
        return failure(message, error == null ? Collections.emptyList() : List.of(error));
    }
}
