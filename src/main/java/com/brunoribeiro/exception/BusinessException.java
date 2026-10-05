package com.brunoribeiro.exception;

import org.springframework.http.HttpStatus;

public class BusinessException extends ApplicationException {

    public BusinessException(String message) {
        super(message, HttpStatus.BAD_REQUEST);
    }

    public BusinessException(String message, Throwable cause) {
        super(message, cause, HttpStatus.BAD_REQUEST);
    }
}
