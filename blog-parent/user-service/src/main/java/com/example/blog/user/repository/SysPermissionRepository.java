package com.example.blog.user.repository;

import com.example.blog.user.entity.SysPermission;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Repository
public interface SysPermissionRepository extends JpaRepository<SysPermission, UUID> {
    Optional<SysPermission> findByCode(String code);
    boolean existsByCode(String code);
    List<SysPermission> findByParentId(UUID parentId);
    List<SysPermission> findByType(String type);
    List<SysPermission> findByStatus(String status);
}