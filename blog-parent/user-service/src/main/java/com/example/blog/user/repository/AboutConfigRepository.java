package com.example.blog.user.repository;

import com.example.blog.user.entity.AboutConfig;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.Optional;
import java.util.UUID;

@Repository
public interface AboutConfigRepository extends JpaRepository<AboutConfig, Integer> {
    Optional<AboutConfig> findByUserId(UUID userId);
}
