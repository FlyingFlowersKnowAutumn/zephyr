package com.example.blog.user.repository;

import com.example.blog.user.entity.SysRole;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface SysRoleRepository extends JpaRepository<SysRole, UUID> {
    Optional<SysRole> findByName(String name);
    boolean existsByName(String name);
}