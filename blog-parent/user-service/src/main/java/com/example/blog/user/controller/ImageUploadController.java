package com.example.blog.user.controller;

import com.example.blog.common.Result;
import com.example.blog.user.service.FileUploadService;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.web.bind.annotation.*;
import org.springframework.web.multipart.MultipartFile;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

@Slf4j
@RestController
@RequestMapping("/api/v1/upload")
@RequiredArgsConstructor
public class ImageUploadController {

    private final FileUploadService fileUploadService;

    @PostMapping("/image")
    public Result<Map<String, String>> uploadImage(
            @RequestParam("file") MultipartFile file,
            @RequestHeader("X-User-Role") String role) throws IOException {

        if (!"blogger".equals(role) && !"admin".equals(role)) {
            return Result.fail(403, "仅博主可上传图片");
        }

        String url = fileUploadService.uploadImage(file);

        Map<String, String> result = new HashMap<>();
        result.put("url", url);
        return Result.success(result);
    }
}
