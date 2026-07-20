package com.example.blog.user.repository;

import com.example.blog.user.entity.SysRolePermission;
import com.example.blog.user.entity.SysRolePermissionId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SysRolePermissionRepository extends JpaRepository<SysRolePermission, SysRolePermissionId> {
    List<SysRolePermission> findByRoleId(UUID roleId);
    List<SysRolePermission> findByPermissionId(UUID permissionId);
    void deleteByRoleId(UUID roleId);
}