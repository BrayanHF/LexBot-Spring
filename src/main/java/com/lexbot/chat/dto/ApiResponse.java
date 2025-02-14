package com.lexbot.chat.dto;

import lombok.AllArgsConstructor;
import lombok.Data;

@Data
@AllArgsConstructor
public class ApiResponse<T> {

    private boolean success;
    private boolean stream;
    private T data;
    private String error;

    public static <T> ApiResponse<T> success(T data, boolean isStream) {
        return new ApiResponse<>(true, isStream, data, null);
    }

    public static <T> ApiResponse<T> error(String errorMessage, boolean isStream) {
        return new ApiResponse<>(false, isStream, null, errorMessage);
    }

}
