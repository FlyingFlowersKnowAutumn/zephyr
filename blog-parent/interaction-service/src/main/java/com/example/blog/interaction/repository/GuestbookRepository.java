package com.example.blog.interaction.repository;

import com.example.blog.interaction.entity.Guestbook;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.Pageable;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface GuestbookRepository extends JpaRepository<Guestbook, String> {

    @Query("SELECT g FROM Guestbook g WHERE g.deletedAt IS NULL AND g.status = 'approved' ORDER BY g.createdAt DESC")
    Page<Guestbook> findAllApproved(Pageable pageable);

    @Query("SELECT g FROM Guestbook g WHERE g.deletedAt IS NULL ORDER BY g.createdAt DESC")
    Page<Guestbook> findAllNotDeleted(Pageable pageable);

    long countByDeletedAtIsNullAndStatus(String status);
}