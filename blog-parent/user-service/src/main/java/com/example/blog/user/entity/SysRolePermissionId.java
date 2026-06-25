package com.example.blog.user.entity;

import lombok.AllArgsConstructor;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.io.Serializable;
import java.util.UUID;

@Data
@NoArgsConstructor
@AllArgsConstructor
public class SysRolePermissionId implements Serializable {
    private UUID roleId;
    private UUID permissionId;
}