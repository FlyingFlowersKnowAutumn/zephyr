package com.example.blog.common.exception;

import lombok.Getter;

@Getter
public class BusinessException extends RuntimeException {
    private final Integer code;

    public BusinessException(Integer code, String message) {
        super(message);
        this.code = code;
    }

    public static BusinessException of(Integer code, String message) {
        return new BusinessException(code, message);
    }

    public static BusinessException badRequest(String message) {
        return new BusinessException(1001, message);
    }

    public static BusinessException unauthorized(String message) {
        return new BusinessException(1002, message);
    }

    public static BusinessException forbidden(String message) {
        return new BusinessException(1003, message);
    }

    public static BusinessException notFound(String message) {
        return new BusinessException(2001, message);
    }

    public static BusinessException conflict(String message) {
        return new BusinessException(2002, message);
    }

    public static BusinessException serverError(String message) {
        return new BusinessException(3001, message);
    }

    public static BusinessException tooManyRequests(String message) {
        return new BusinessException(4001, message);
    }
}
