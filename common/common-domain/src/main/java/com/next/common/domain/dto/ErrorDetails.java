package com.next.common.domain.dto;

import com.fasterxml.jackson.annotation.JsonInclude;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.util.Map;

/**
 * Error details for API responses
 */
@Data
@Builder
@NoArgsConstructor
@AllArgsConstructor
@JsonInclude(JsonInclude.Include.NON_NULL)
public class ErrorDetails {

    private String code;

    private String message;

    private Map<String, String> fieldErrors;

    private String trace;

    /**
     * Create error details with code and message
     */
    public static ErrorDetails of(String code, String message) {
        return ErrorDetails.builder()
                .code(code)
                .message(message)
                .build();
    }

    /**
     * Create error details with field validation errors
     */
    public static ErrorDetails withFieldErrors(String code, String message, Map<String, String> fieldErrors) {
        return ErrorDetails.builder()
                .code(code)
                .message(message)
                .fieldErrors(fieldErrors)
                .build();
    }
}
