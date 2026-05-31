package com.example.blog.common.enums;

public enum RoleEnum {
    USER("user"),
    BLOGGER("blogger"),
    GUEST("guest");

    private final String value;

    RoleEnum(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }
}
