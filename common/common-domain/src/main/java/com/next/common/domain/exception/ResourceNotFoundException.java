package com.next.common.domain.exception;

/**
 * Exception thrown when a requested resource is not found
 */
public class ResourceNotFoundException extends BusinessException {

    public ResourceNotFoundException(String resourceName, String resourceId) {
        super("RESOURCE_NOT_FOUND", String.format("%s with id '%s' not found", resourceName, resourceId));
    }

    public ResourceNotFoundException(String message) {
        super("RESOURCE_NOT_FOUND", message);
    }
}
