package com.next.common.domain.exception;

/**
 * Exception thrown when user is not authorized to perform an action
 */
public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Unauthorized access");
    }
}
