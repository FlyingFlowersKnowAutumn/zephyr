package com.example.blog.article.service;

import com.example.blog.article.entity.Tag;
import com.example.blog.article.repository.ArticleTagMapRepository;
import com.example.blog.article.repository.TagRepository;
import com.example.blog.common.exception.BusinessException;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.List;

@Slf4j
@Service
@RequiredArgsConstructor
public class TagService {

    private final TagRepository tagRepository;
    private final ArticleTagMapRepository articleTagMapRepository;
    private final TagVisitService tagVisitService;

    @Transactional
    public Tag createTag(String name) {
        if (tagRepository.existsByName(name)) {
            throw BusinessException.badRequest("标签已存在");
        }

        Tag tag = Tag.builder()
                .name(name)
                .articleCount(0)
                .build();

        Tag saved = tagRepository.save(tag);
        log.info("Tag created: id={}, name={}", saved.getId(), saved.getName());
        return saved;
    }

    @Transactional(readOnly = true)
    public List<Tag> getAllTags() {
        return tagRepository.findAll();
    }

    @Transactional(readOnly = true)
    public List<Tag> getHotTags() {
        return tagRepository.findAllOrderByVisitCountDesc();
    }

    @Transactional(readOnly = true)
    public Tag getTagById(String id) {
        return tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));
    }

    @Transactional(readOnly = true)
    public Tag getTagByName(String name) {
        return tagRepository.findByName(name)
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));
    }

    @Transactional
    public Tag updateTag(String id, String name) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));

        if (!tag.getName().equals(name) && tagRepository.existsByName(name)) {
            throw BusinessException.badRequest("标签名称已存在");
        }

        tag.setName(name);

        Tag updated = tagRepository.save(tag);
        log.info("Tag updated: id={}, name={}", updated.getId(), updated.getName());
        return updated;
    }

    @Transactional
    public void deleteTag(String id) {
        Tag tag = tagRepository.findById(id)
                .orElseThrow(() -> BusinessException.notFound("标签不存在"));

        if (tag.getArticleCount() > 0) {
            throw BusinessException.badRequest("标签下还有文章，无法删除");
        }

        articleTagMapRepository.deleteByTagId(id);
        tagRepository.delete(tag);
        log.info("Tag deleted: id={}, name={}", id, tag.getName());
    }

    @Transactional
    public void incrementArticleCount(String tagId) {
        if (tagId != null && !tagId.isBlank()) {
            tagRepository.incrementArticleCount(tagId);
        }
    }

    @Transactional
    public void decrementArticleCount(String tagId) {
        if (tagId != null && !tagId.isBlank()) {
            tagRepository.decrementArticleCount(tagId);
        }
    }

    @Transactional
    public void incrementVisitCount(String tagId) {
        incrementVisitCount(tagId, null);
    }

    @Transactional
    public void incrementVisitCount(String tagId, String userId) {
        if (tagId == null || tagId.isBlank()) {
            return;
        }

        try {
            boolean recorded = tagVisitService.recordVisit(tagId, userId != null ? userId : "anonymous");
            if (recorded) {
                tagRepository.incrementVisitCount(tagId);
                log.info("Tag visit count incremented: id={}, userId={}", tagId, userId);
            }
        } catch (Exception e) {
            log.warn("Redis visit recording failed, falling back to direct DB increment: tagId={}", tagId, e);
            tagRepository.incrementVisitCount(tagId);
        }
    }

    @Transactional
    public void incrementVisitCountByDelta(String tagId, int delta) {
        if (tagId != null && !tagId.isBlank() && delta > 0) {
            tagRepository.incrementVisitCountByDelta(tagId, delta);
            log.info("Tag visit count incremented by delta: id={}, delta={}", tagId, delta);
        }
    }
}