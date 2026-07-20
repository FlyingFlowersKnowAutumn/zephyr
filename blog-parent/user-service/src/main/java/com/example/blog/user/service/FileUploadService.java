package com.example.blog.user.service;

import lombok.extern.slf4j.Slf4j;
import org.springframework.beans.factory.annotation.Value;
import org.springframework.stereotype.Service;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.UUID;

@Slf4j
@Service
public class FileUploadService {

    @Value("${file.upload.path:uploads}")
    private String uploadPath;

    @Value("${file.upload.base-url:/uploads}")
    private String baseUrl;

    public String uploadAvatar(MultipartFile file) throws IOException {
        validateFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + extension;
        
        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, "avatars", dateDir);
        Files.createDirectories(uploadDir);
        
        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);
        
        String url = baseUrl + "/avatars/" + dateDir + "/" + fileName;
        log.info("Avatar uploaded: {}", url);
        
        return url;
    }

    public String uploadImage(MultipartFile file) throws IOException {
        validateFile(file);

        String extension = getFileExtension(file.getOriginalFilename());
        String fileName = UUID.randomUUID().toString() + extension;

        String dateDir = LocalDateTime.now().format(DateTimeFormatter.ofPattern("yyyy/MM/dd"));
        Path uploadDir = Paths.get(uploadPath, "images", dateDir);
        Files.createDirectories(uploadDir);

        Path filePath = uploadDir.resolve(fileName);
        Files.copy(file.getInputStream(), filePath);

        String url = baseUrl + "/images/" + dateDir + "/" + fileName;
        log.info("Image uploaded: {}", url);

        return url;
    }

    public void deleteOldAvatar(String avatarUrl) {
        if (avatarUrl == null || !avatarUrl.startsWith(baseUrl)) {
            return;
        }
        try {
            String relativePath = avatarUrl.substring(baseUrl.length());
            Path filePath = Paths.get(uploadPath, relativePath);
            boolean deleted = Files.deleteIfExists(filePath);
            if (deleted) {
                log.info("Old avatar deleted: {}", avatarUrl);
            }
        } catch (IOException e) {
            log.warn("Failed to delete old avatar: {}", avatarUrl, e);
        }
    }

    private void validateFile(MultipartFile file) {
        if (file == null || file.isEmpty()) {
            throw new IllegalArgumentException("文件不能为空");
        }
        
        String originalFilename = file.getOriginalFilename();
        if (originalFilename == null || !originalFilename.contains(".")) {
            throw new IllegalArgumentException("无效的文件名");
        }
        
        String extension = getFileExtension(originalFilename).toLowerCase();
        if (!extension.matches("\\.(jpg|jpeg|png|gif|webp)")) {
            throw new IllegalArgumentException("只支持 jpg、jpeg、png、gif、webp 格式的图片");
        }
        
        if (file.getSize() > 5 * 1024 * 1024) {
            throw new IllegalArgumentException("图片大小不能超过 5MB");
        }
    }

    private String getFileExtension(String filename) {
        if (filename == null) {
            return "";
        }
        int lastDotIndex = filename.lastIndexOf('.');
        if (lastDotIndex == -1) {
            return "";
        }
        return filename.substring(lastDotIndex);
    }
}
