package com.example.blog.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysUserRoleId implements Serializable {
    private UUID userId;
    private UUID roleId;
}