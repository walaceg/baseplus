package com.baseplus.core.exception;

import java.util.Collections;
import java.util.List;

import org.springframework.http.HttpStatus;

public class BusinessException extends RuntimeException {

    private final HttpStatus status;
    private final List<String> errors;

    public BusinessException(String message) {
        this(message, HttpStatus.BAD_REQUEST, Collections.emptyList());
    }

    public BusinessException(String message, String error) {
        this(message, HttpStatus.BAD_REQUEST, List.of(error));
    }

    public BusinessException(String message, List<String> errors) {
        this(message, HttpStatus.BAD_REQUEST, errors);
    }

    public BusinessException(String message, HttpStatus status, List<String> errors) {
        super(message);
        this.status = status == null ? HttpStatus.BAD_REQUEST : status;
        this.errors = errors == null ? Collections.emptyList() : errors;
    }

    public HttpStatus getStatus() {
        return status;
    }

    public List<String> getErrors() {
        return errors;
    }
}
