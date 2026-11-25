package com.example.blog.common.util;

/**
 * 角色判定工具，避免各处手写字符串比较。
 */
public final class Roles {

    public static final String USER = "user";
    public static final String BLOGGER = "blogger";
    public static final String ADMIN = "admin";

    private Roles() {
    }

    /** 是否具备内容管理权限（博主或管理员） */
    public static boolean canManage(String role) {
        return BLOGGER.equals(role) || ADMIN.equals(role);
    }
}
