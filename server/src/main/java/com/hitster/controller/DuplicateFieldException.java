package com.hitster.controller;

public class DuplicateFieldException extends RuntimeException {

    private final String errorCode;

    public DuplicateFieldException(String errorCode, String message) {
        super(message);
        this.errorCode = errorCode;
    }

    public String getErrorCode() {
        return errorCode;
    }
}