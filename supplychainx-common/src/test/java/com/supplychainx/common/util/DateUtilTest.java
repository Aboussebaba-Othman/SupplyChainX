package com.supplychainx.common.util;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalDateTime;

import static org.junit.jupiter.api.Assertions.*;

@DisplayName("DateUtil Unit Tests")
class DateUtilTest {

    // ========== Format Date Tests ==========
    
    @Test
    @DisplayName("Should format date correctly")
    void shouldFormatDateCorrectly() {
        LocalDate date = LocalDate.of(2025, 11, 15);
        String formatted = DateUtil.formatDate(date);
        assertEquals("2025-11-15", formatted);
    }

    @Test
    @DisplayName("Should return null for null date")
    void shouldReturnNullForNullDate() {
        String formatted = DateUtil.formatDate(null);
        assertNull(formatted);
    }

    // ========== Format DateTime Tests ==========
    
    @Test
    @DisplayName("Should format datetime correctly")
    void shouldFormatDateTimeCorrectly() {
        LocalDateTime dateTime = LocalDateTime.of(2025, 11, 15, 14, 30, 45);
        String formatted = DateUtil.formatDateTime(dateTime);
        assertEquals("2025-11-15 14:30:45", formatted);
    }

    @Test
    @DisplayName("Should return null for null datetime")
    void shouldReturnNullForNullDateTime() {
        String formatted = DateUtil.formatDateTime(null);
        assertNull(formatted);
    }

    // ========== Parse Date Tests ==========
    
    @Test
    @DisplayName("Should parse date string correctly")
    void shouldParseDateStringCorrectly() {
        LocalDate date = DateUtil.parseDate("2025-11-15");
        assertNotNull(date);
        assertEquals(2025, date.getYear());
        assertEquals(11, date.getMonthValue());
        assertEquals(15, date.getDayOfMonth());
    }

    @Test
    @DisplayName("Should return null for null date string")
    void shouldReturnNullForNullDateString() {
        LocalDate date = DateUtil.parseDate(null);
        assertNull(date);
    }

    @Test
    @DisplayName("Should return null for blank date string")
    void shouldReturnNullForBlankDateString() {
        LocalDate date = DateUtil.parseDate("");
        assertNull(date);
        
        date = DateUtil.parseDate("   ");
        assertNull(date);
    }

    // ========== Parse DateTime Tests ==========
    
    @Test
    @DisplayName("Should parse datetime string correctly")
    void shouldParseDateTimeStringCorrectly() {
        LocalDateTime dateTime = DateUtil.parseDateTime("2025-11-15 14:30:45");
        assertNotNull(dateTime);
        assertEquals(2025, dateTime.getYear());
        assertEquals(11, dateTime.getMonthValue());
        assertEquals(15, dateTime.getDayOfMonth());
        assertEquals(14, dateTime.getHour());
        assertEquals(30, dateTime.getMinute());
        assertEquals(45, dateTime.getSecond());
    }

    @Test
    @DisplayName("Should return null for null datetime string")
    void shouldReturnNullForNullDateTimeString() {
        LocalDateTime dateTime = DateUtil.parseDateTime(null);
        assertNull(dateTime);
    }

    @Test
    @DisplayName("Should return null for blank datetime string")
    void shouldReturnNullForBlankDateTimeString() {
        LocalDateTime dateTime = DateUtil.parseDateTime("");
        assertNull(dateTime);
    }

    // ========== Is Date In Past Tests ==========
    
    @Test
    @DisplayName("Should return true for past date")
    void shouldReturnTrueForPastDate() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertTrue(DateUtil.isDateInPast(pastDate));
        
        LocalDate longPast = LocalDate.of(2020, 1, 1);
        assertTrue(DateUtil.isDateInPast(longPast));
    }

    @Test
    @DisplayName("Should return false for today")
    void shouldReturnFalseForToday() {
        LocalDate today = LocalDate.now();
        assertFalse(DateUtil.isDateInPast(today));
    }

    @Test
    @DisplayName("Should return false for future date")
    void shouldReturnFalseForFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertFalse(DateUtil.isDateInPast(futureDate));
    }

    @Test
    @DisplayName("Should return false for null date in past check")
    void shouldReturnFalseForNullDateInPastCheck() {
        assertFalse(DateUtil.isDateInPast(null));
    }

    // ========== Is Date In Future Tests ==========
    
    @Test
    @DisplayName("Should return true for future date")
    void shouldReturnTrueForFutureDate() {
        LocalDate futureDate = LocalDate.now().plusDays(1);
        assertTrue(DateUtil.isDateInFuture(futureDate));
        
        LocalDate longFuture = LocalDate.of(2030, 12, 31);
        assertTrue(DateUtil.isDateInFuture(longFuture));
    }

    @Test
    @DisplayName("Should return false for today in future check")
    void shouldReturnFalseForTodayInFutureCheck() {
        LocalDate today = LocalDate.now();
        assertFalse(DateUtil.isDateInFuture(today));
    }

    @Test
    @DisplayName("Should return false for past date in future check")
    void shouldReturnFalseForPastDateInFutureCheck() {
        LocalDate pastDate = LocalDate.now().minusDays(1);
        assertFalse(DateUtil.isDateInFuture(pastDate));
    }

    @Test
    @DisplayName("Should return false for null date in future check")
    void shouldReturnFalseForNullDateInFutureCheck() {
        assertFalse(DateUtil.isDateInFuture(null));
    }

    // ========== Days Between Tests ==========
    
    @Test
    @DisplayName("Should calculate days between dates")
    void shouldCalculateDaysBetweenDates() {
        LocalDate start = LocalDate.of(2025, 11, 1);
        LocalDate end = LocalDate.of(2025, 11, 15);
        
        int days = DateUtil.daysBetween(start, end);
        assertEquals(14, days);
    }

    @Test
    @DisplayName("Should return zero for same dates")
    void shouldReturnZeroForSameDates() {
        LocalDate date = LocalDate.of(2025, 11, 15);
        int days = DateUtil.daysBetween(date, date);
        assertEquals(0, days);
    }

    @Test
    @DisplayName("Should return negative for end before start")
    void shouldReturnNegativeForEndBeforeStart() {
        LocalDate start = LocalDate.of(2025, 11, 15);
        LocalDate end = LocalDate.of(2025, 11, 1);
        
        int days = DateUtil.daysBetween(start, end);
        assertEquals(-14, days);
    }

    @Test
    @DisplayName("Should return zero for null start date")
    void shouldReturnZeroForNullStartDate() {
        LocalDate end = LocalDate.of(2025, 11, 15);
        int days = DateUtil.daysBetween(null, end);
        assertEquals(0, days);
    }

    @Test
    @DisplayName("Should return zero for null end date")
    void shouldReturnZeroForNullEndDate() {
        LocalDate start = LocalDate.of(2025, 11, 1);
        int days = DateUtil.daysBetween(start, null);
        assertEquals(0, days);
    }

    @Test
    @DisplayName("Should return zero for both null dates")
    void shouldReturnZeroForBothNullDates() {
        int days = DateUtil.daysBetween(null, null);
        assertEquals(0, days);
    }

    // ========== Format and Parse Round Trip Tests ==========
    
    @Test
    @DisplayName("Should preserve date through format and parse cycle")
    void shouldPreserveDateThroughFormatAndParseCycle() {
        LocalDate original = LocalDate.of(2025, 11, 15);
        String formatted = DateUtil.formatDate(original);
        LocalDate parsed = DateUtil.parseDate(formatted);
        
        assertEquals(original, parsed);
    }

    @Test
    @DisplayName("Should preserve datetime through format and parse cycle")
    void shouldPreserveDateTimeThroughFormatAndParseCycle() {
        LocalDateTime original = LocalDateTime.of(2025, 11, 15, 14, 30, 45);
        String formatted = DateUtil.formatDateTime(original);
        LocalDateTime parsed = DateUtil.parseDateTime(formatted);
        
        assertEquals(original, parsed);
    }
}
