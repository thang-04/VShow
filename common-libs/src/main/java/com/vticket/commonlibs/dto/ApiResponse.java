package com.vticket.commonlibs.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ApiResponse<T> {
    private Integer code;
    private String codeName;
    private String desc;
    private T result;

    public static <T> ApiResponse<T> success(T data) {
        return ApiResponse.<T>builder()
                .code(1000)
                .codeName("SUCCESS")
                .desc("Success")
                .result(data)
                .build();
    }

    public static <T> ApiResponse<T> success(String message, T data) {
        return ApiResponse.<T>builder()
                .code(1000)
                .codeName("SUCCESS")
                .desc(message)
                .result(data)
                .build();
    }

    public static <T> ApiResponse<T> error(Integer code, String codeName, String message) {
        return ApiResponse.<T>builder()
                .code(code)
                .codeName(codeName)
                .desc(message)
                .build();
    }
}

