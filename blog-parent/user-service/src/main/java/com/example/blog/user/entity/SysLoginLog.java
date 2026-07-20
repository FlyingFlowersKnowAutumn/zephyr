package com.example.blog.user.entity;

import jakarta.persistence.*;
import lombok.AllArgsConstructor;
import lombok.Builder;
import lombok.Data;
import lombok.NoArgsConstructor;

import java.time.LocalDateTime;
import java.util.UUID;

@Data
@Entity
@Builder
@NoArgsConstructor
@AllArgsConstructor
@Table(name = "sys_login_log")
public class SysLoginLog {

    @Id
    @GeneratedValue(strategy = GenerationType.UUID)
    private UUID id;

    @Column(name = "user_id")
    private UUID userId;

    @Column(length = 50)
    private String username;

    @Column(length = 50)
    private String ip;

    @Column(length = 200)
    private String location;

    @Column(length = 100)
    private String device;

    @Column(nullable = false, length = 20)
    private String status;

    @Column(length = 200)
    private String message;

    @Column(name = "created_at")
    @Builder.Default
    private LocalDateTime createdAt = LocalDateTime.now();
}