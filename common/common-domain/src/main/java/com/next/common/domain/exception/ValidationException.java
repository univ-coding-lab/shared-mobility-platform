package com.next.common.domain.exception;

import java.util.Map;

public class ValidationException extends BusinessException {

    private Map<String, String> fieldErrors;

    public ValidationException(String message) {
        super("VALIDATION_ERROR", message);
    }

    public ValidationException(String message, Map<String, String> fieldErrors) {
        super("VALIDATION_ERROR", message);
        this.fieldErrors = fieldErrors;
    }

    public Map<String, String> getFieldErrors() {
        return fieldErrors;
    }
}
