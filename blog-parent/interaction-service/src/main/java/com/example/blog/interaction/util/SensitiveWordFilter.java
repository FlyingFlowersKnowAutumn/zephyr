package com.example.blog.interaction.util;

import jakarta.annotation.PostConstruct;
import lombok.extern.slf4j.Slf4j;
import org.springframework.core.io.ClassPathResource;
import org.springframework.stereotype.Component;

import java.io.BufferedReader;
import java.io.InputStreamReader;
import java.nio.charset.StandardCharsets;
import java.util.*;

@Slf4j
@Component
public class SensitiveWordFilter {

    private final Map<Character, Object> root = new HashMap<>();

    @PostConstruct
    public void init() {
        try {
            ClassPathResource resource = new ClassPathResource("sensitive-words.txt");
            BufferedReader reader = new BufferedReader(
                    new InputStreamReader(resource.getInputStream(), StandardCharsets.UTF_8));
            String line;
            int count = 0;
            while ((line = reader.readLine()) != null) {
                line = line.trim();
                if (line.isEmpty() || line.startsWith("#")) {
                    continue;
                }
                addWord(line);
                count++;
            }
            reader.close();
            log.info("Loaded {} sensitive words", count);
        } catch (Exception e) {
            log.warn("Failed to load sensitive words: {}", e.getMessage());
        }
    }

    @SuppressWarnings("unchecked")
    private void addWord(String word) {
        Map<Character, Object> current = root;
        for (int i = 0; i < word.length(); i++) {
            char c = word.charAt(i);
            Map<Character, Object> next = (Map<Character, Object>) current.get(c);
            if (next == null) {
                next = new HashMap<>();
                current.put(c, next);
            }
            if (i == word.length() - 1) {
                next.put('\0', Boolean.TRUE);
            }
            current = next;
        }
    }

    @SuppressWarnings("unchecked")
    public String filter(String text) {
        if (text == null || text.isEmpty()) {
            return text;
        }

        StringBuilder result = new StringBuilder();
        int i = 0;
        while (i < text.length()) {
            Map<Character, Object> current = root;
            int matchEnd = -1;
            int j = i;

            while (j < text.length()) {
                char c = text.charAt(j);
                Map<Character, Object> next = (Map<Character, Object>) current.get(c);
                if (next == null) {
                    break;
                }
                if (next.containsKey('\0')) {
                    matchEnd = j;
                }
                current = next;
                j++;
            }

            if (matchEnd >= 0) {
                for (int k = i; k <= matchEnd; k++) {
                    result.append('*');
                }
                i = matchEnd + 1;
            } else {
                result.append(text.charAt(i));
                i++;
            }
        }

        return result.toString();
    }
}
