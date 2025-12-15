package com.next.common.domain.exception;

public class UnauthorizedException extends BusinessException {

    public UnauthorizedException(String message) {
        super("UNAUTHORIZED", message);
    }

    public UnauthorizedException() {
        super("UNAUTHORIZED", "Unauthorized access");
    }
}
