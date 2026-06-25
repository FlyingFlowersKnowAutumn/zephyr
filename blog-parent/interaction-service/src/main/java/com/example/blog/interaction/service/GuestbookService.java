package com.example.blog.interaction.service;

import com.example.blog.common.dto.GuestbookCreateDTO;
import com.example.blog.common.dto.GuestbookDTO;
import com.example.blog.common.exception.BusinessException;
import com.example.blog.interaction.entity.Guestbook;
import com.example.blog.interaction.repository.GuestbookRepository;
import com.example.blog.interaction.util.SensitiveWordFilter;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.data.domain.Page;
import org.springframework.data.domain.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.time.LocalDateTime;

@Slf4j
@Service
@RequiredArgsConstructor
public class GuestbookService {

    private final GuestbookRepository guestbookRepository;
    private final SensitiveWordFilter sensitiveWordFilter;

    @Transactional
    public GuestbookDTO createGuestbook(GuestbookCreateDTO dto, String userId, String userName) {
        String filteredContent = sensitiveWordFilter.filter(dto.getContent());

        Guestbook guestbook = Guestbook.builder()
                .userId(userId)
                .content(filteredContent)
                .build();

        Guestbook saved = guestbookRepository.save(guestbook);
        log.info("Guestbook created: id={}, userId={}", saved.getId(), userId);
        return convertToDTO(saved, userName);
    }

    @Transactional(readOnly = true)
    public Page<GuestbookDTO> getGuestbookList(int page, int size, boolean isAdmin) {
        Pageable pageable = PageRequest.of(page, size, Sort.by(Sort.Direction.DESC, "createdAt"));
        Page<Guestbook> guestbookPage = isAdmin 
                ? guestbookRepository.findAllNotDeleted(pageable)
                : guestbookRepository.findAllApproved(pageable);
        return guestbookPage.map(g -> convertToDTO(g, null));
    }

    @Transactional
    public void deleteGuestbook(String id, String userId, boolean isAdmin) {
        Guestbook guestbook = guestbookRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("留言不存在"));

        if (!isAdmin && !guestbook.getUserId().equals(userId)) {
            throw BusinessException.forbidden("无权删除此留言");
        }

        guestbook.setDeletedAt(LocalDateTime.now());
        guestbookRepository.save(guestbook);
        log.info("Guestbook deleted: id={}", id);
    }

    private GuestbookDTO convertToDTO(Guestbook guestbook, String userName) {
        return GuestbookDTO.builder()
                .id(guestbook.getId())
                .userId(guestbook.getUserId())
                .userName(userName)
                .content(guestbook.getContent())
                .status(guestbook.getStatus())
                .createdAt(guestbook.getCreatedAt())
                .build();
    }
}