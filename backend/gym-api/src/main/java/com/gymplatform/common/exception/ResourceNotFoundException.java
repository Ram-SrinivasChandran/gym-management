package com.gymplatform.common.exception;

import org.springframework.http.HttpStatus;

public class ResourceNotFoundException extends ApiException {

    public ResourceNotFoundException(String entityName, Object identifier) {
        super(HttpStatus.NOT_FOUND, entityName + " not found: " + identifier);
    }
}
