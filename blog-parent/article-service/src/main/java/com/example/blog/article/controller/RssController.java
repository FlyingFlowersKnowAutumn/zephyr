package com.example.blog.article.controller;

import com.example.blog.article.entity.Article;
import com.example.blog.article.entity.Article.ArticleStatus;
import com.example.blog.article.repository.ArticleRepository;
import com.rometools.rome.feed.synd.SyndContent;
import com.rometools.rome.feed.synd.SyndContentImpl;
import com.rometools.rome.feed.synd.SyndEntry;
import com.rometools.rome.feed.synd.SyndEntryImpl;
import com.rometools.rome.feed.synd.SyndFeed;
import com.rometools.rome.feed.synd.SyndFeedImpl;
import com.rometools.rome.io.SyndFeedOutput;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.util.ArrayList;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class RssController {

    private final ArticleRepository articleRepository;

    @Value("${blog.base-url:http://localhost:5173}")
    private String baseUrl;

    @GetMapping(value = "/api/v1/rss.xml", produces = "application/rss+xml")
    public String getRss() {
        List<Article> articles = articleRepository.findPublishedArticles(
                ArticleStatus.PUBLISHED,
                PageRequest.of(0, 20)
        ).getContent();

        SyndFeed feed = new SyndFeedImpl();
        feed.setFeedType("rss_2.0");
        feed.setTitle("个人技术博客");
        feed.setLink(baseUrl);
        feed.setDescription("记录技术成长，分享编程心得");
        feed.setLanguage("zh-CN");

        List<SyndEntry> entries = new ArrayList<>();
        for (Article article : articles) {
            SyndEntry entry = new SyndEntryImpl();
            String slug = article.getSlug() != null ? article.getSlug() : article.getId();
            entry.setTitle(article.getTitle());
            entry.setLink(baseUrl + "/articles/" + slug);
            entry.setPublishedDate(java.util.Date.from(article.getPublishedAt() != null
                    ? article.getPublishedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()
                    : article.getCreatedAt().atZone(java.time.ZoneId.systemDefault()).toInstant()));
            entry.setAuthor(article.getAuthorName());

            SyndContent description = new SyndContentImpl();
            description.setType("text/html");
            description.setValue(article.getExcerpt() != null ? article.getExcerpt() : article.getContent());
            entry.setDescription(description);

            entries.add(entry);
        }
        feed.setEntries(entries);

        try {
            return new SyndFeedOutput().outputString(feed);
        } catch (Exception e) {
            throw new RuntimeException("生成 RSS 失败", e);
        }
    }
}
