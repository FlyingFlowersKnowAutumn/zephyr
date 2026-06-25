package com.example.blog.user.repository;

import com.example.blog.user.entity.SysLoginLog;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.time.LocalDateTime;
import java.util.List;
import java.util.UUID;

@Repository
public interface SysLoginLogRepository extends JpaRepository<SysLoginLog, UUID> {
    List<SysLoginLog> findByUserId(UUID userId);
    List<SysLoginLog> findByStatus(String status);
    List<SysLoginLog> findByCreatedAtBetween(LocalDateTime start, LocalDateTime end);
}