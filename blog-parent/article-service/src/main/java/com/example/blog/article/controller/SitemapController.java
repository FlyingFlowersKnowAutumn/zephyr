package com.example.blog.article.controller;

import com.example.blog.article.entity.Article;
import com.example.blog.article.entity.Article.ArticleStatus;
import com.example.blog.article.repository.ArticleRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.data.domain.PageRequest;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RestController;

import java.time.format.DateTimeFormatter;
import java.util.List;

@RestController
@RequiredArgsConstructor
public class SitemapController {

    private final ArticleRepository articleRepository;

    @Value("${blog.base-url:http://localhost:5173}")
    private String baseUrl;

    @GetMapping(value = "/api/v1/sitemap.xml", produces = "application/xml")
    public String getSitemap() {
        List<Article> articles = articleRepository.findPublishedArticles(
                ArticleStatus.PUBLISHED,
                PageRequest.of(0, 5000)
        ).getContent();

        StringBuilder sb = new StringBuilder();
        sb.append("<?xml version=\"1.0\" encoding=\"UTF-8\"?>\n");
        sb.append("<urlset xmlns=\"http://www.sitemaps.org/schemas/sitemap/0.9\">\n");

        sb.append("  <url>\n");
        sb.append("    <loc>").append(baseUrl).append("/</loc>\n");
        sb.append("    <changefreq>daily</changefreq>\n");
        sb.append("    <priority>1.0</priority>\n");
        sb.append("  </url>\n");

        sb.append("  <url>\n");
        sb.append("    <loc>").append(baseUrl).append("/articles</loc>\n");
        sb.append("    <changefreq>daily</changefreq>\n");
        sb.append("    <priority>0.9</priority>\n");
        sb.append("  </url>\n");

        DateTimeFormatter fmt = DateTimeFormatter.ofPattern("yyyy-MM-dd");

        for (Article article : articles) {
            String slug = article.getSlug() != null ? article.getSlug() : article.getId();
            sb.append("  <url>\n");
            sb.append("    <loc>").append(baseUrl).append("/articles/").append(slug).append("</loc>\n");
            sb.append("    <lastmod>").append(article.getUpdatedAt().format(fmt)).append("</lastmod>\n");
            sb.append("    <changefreq>weekly</changefreq>\n");
            sb.append("    <priority>0.8</priority>\n");
            sb.append("  </url>\n");
        }

        sb.append("</urlset>\n");
        return sb.toString();
    }
}
