package com.supplychainx.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("StringUtil Unit Tests")
class StringUtilTest {

    // ========== Generate Code Tests ==========
    
    @Test
    @DisplayName("Should generate code with prefix and ID")
    void shouldGenerateCodeWithPrefixAndId() {
        String code = StringUtil.generateCode("SUP", 1L);
        assertEquals("SUP-000001", code);
        
        code = StringUtil.generateCode("PROD", 123L);
        assertEquals("PROD-000123", code);
        
        code = StringUtil.generateCode("ORDER", 999999L);
        assertEquals("ORDER-999999", code);
    }

    @Test
    @DisplayName("Should generate code with zero padding")
    void shouldGenerateCodeWithZeroPadding() {
        String code = StringUtil.generateCode("RM", 5L);
        assertEquals("RM-000005", code);
        
        code = StringUtil.generateCode("INV", 42L);
        assertEquals("INV-000042", code);
    }

    // ========== Truncate Tests ==========
    
    @Test
    @DisplayName("Should truncate long string")
    void shouldTruncateLongString() {
        String result = StringUtil.truncate("This is a very long string that needs to be truncated", 20);
        assertEquals("This is a very lo...", result);
        assertEquals(20, result.length());
    }

    @Test
    @DisplayName("Should not truncate short string")
    void shouldNotTruncateShortString() {
        String shortString = "Short";
        String result = StringUtil.truncate(shortString, 20);
        assertEquals("Short", result);
    }

    @Test
    @DisplayName("Should not truncate string equal to max length")
    void shouldNotTruncateStringEqualToMaxLength() {
        String exact = "12345";
        String result = StringUtil.truncate(exact, 5);
        assertEquals("12345", result);
    }

    @Test
    @DisplayName("Should handle null string in truncate")
    void shouldHandleNullStringInTruncate() {
        String result = StringUtil.truncate(null, 10);
        assertNull(result);
    }

    @Test
    @DisplayName("Should truncate at exact max length including ellipsis")
    void shouldTruncateAtExactMaxLengthIncludingEllipsis() {
        String result = StringUtil.truncate("123456789", 6);
        assertEquals("123...", result);
        assertEquals(6, result.length());
    }

    // ========== Is Null Or Blank Tests ==========
    
    @Test
    @DisplayName("Should return true for null string")
    void shouldReturnTrueForNullString() {
        assertTrue(StringUtil.isNullOrBlank(null));
    }

    @Test
    @DisplayName("Should return true for blank string")
    void shouldReturnTrueForBlankString() {
        assertTrue(StringUtil.isNullOrBlank(""));
        assertTrue(StringUtil.isNullOrBlank("   "));
        assertTrue(StringUtil.isNullOrBlank("\t\n"));
    }

    @Test
    @DisplayName("Should return false for non-blank string")
    void shouldReturnFalseForNonBlankString() {
        assertFalse(StringUtil.isNullOrBlank("test"));
        assertFalse(StringUtil.isNullOrBlank("  a  "));
        assertFalse(StringUtil.isNullOrBlank("123"));
    }

    // ========== Capitalize First Tests ==========
    
    @Test
    @DisplayName("Should capitalize first letter")
    void shouldCapitalizeFirstLetter() {
        String result = StringUtil.capitalizeFirst("hello");
        assertEquals("Hello", result);
        
        result = StringUtil.capitalizeFirst("world");
        assertEquals("World", result);
    }

    @Test
    @DisplayName("Should lowercase remaining letters")
    void shouldLowercaseRemainingLetters() {
        String result = StringUtil.capitalizeFirst("HELLO");
        assertEquals("Hello", result);
        
        result = StringUtil.capitalizeFirst("WoRlD");
        assertEquals("World", result);
    }

    @Test
    @DisplayName("Should handle single character string")
    void shouldHandleSingleCharacterString() {
        String result = StringUtil.capitalizeFirst("a");
        assertEquals("A", result);
        
        result = StringUtil.capitalizeFirst("Z");
        assertEquals("Z", result);
    }

    @Test
    @DisplayName("Should return null for null string in capitalize")
    void shouldReturnNullForNullStringInCapitalize() {
        String result = StringUtil.capitalizeFirst(null);
        assertNull(result);
    }

    @Test
    @DisplayName("Should return blank for blank string in capitalize")
    void shouldReturnBlankForBlankStringInCapitalize() {
        String result = StringUtil.capitalizeFirst("");
        assertEquals("", result);
        
        result = StringUtil.capitalizeFirst("   ");
        assertEquals("   ", result);
    }

    @Test
    @DisplayName("Should handle string with numbers")
    void shouldHandleStringWithNumbers() {
        String result = StringUtil.capitalizeFirst("test123");
        assertEquals("Test123", result);
    }
}
