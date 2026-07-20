package com.example.blog.interaction.util;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

class SensitiveWordFilterTest {

    private SensitiveWordFilter filter;

    @BeforeEach
    void setUp() {
        filter = new SensitiveWordFilter();
        filter.init();
    }

    @Test
    void shouldFilterSingleSensitiveWord() {
        String result = filter.filter("这是个傻逼");
        assertEquals("这是个**", result);
    }

    @Test
    void shouldFilterMultipleSensitiveWords() {
        String result = filter.filter("fuck you 傻逼");
        assertEquals("**** you **", result);
    }

    @Test
    void shouldReturnOriginalWhenNoSensitiveWords() {
        String result = filter.filter("这是正常文本");
        assertEquals("这是正常文本", result);
    }

    @Test
    void shouldHandleNullInput() {
        assertNull(filter.filter(null));
    }

    @Test
    void shouldHandleEmptyInput() {
        assertEquals("", filter.filter(""));
    }
}
