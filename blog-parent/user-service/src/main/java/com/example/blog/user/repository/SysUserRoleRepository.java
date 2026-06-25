package com.example.blog.user.repository;

import com.example.blog.user.entity.SysUserRole;
import com.example.blog.user.entity.SysUserRoleId;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SysUserRoleRepository extends JpaRepository<SysUserRole, SysUserRoleId> {
    List<SysUserRole> findByUserId(UUID userId);
    List<SysUserRole> findByRoleId(UUID roleId);
    void deleteByUserId(UUID userId);
}