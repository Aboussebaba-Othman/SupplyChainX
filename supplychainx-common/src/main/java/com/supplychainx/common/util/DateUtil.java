package com.supplychainx.common.util;

import lombok.experimental.UtilityClass;

import java.time.LocalDate;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;


@UtilityClass
public class DateUtil {

    public static final DateTimeFormatter DATE_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd");
    public static final DateTimeFormatter DATETIME_FORMATTER = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

    public static String formatDate(LocalDate date) {
        return date != null ? date.format(DATE_FORMATTER) : null;
    }

    public static String formatDateTime(LocalDateTime dateTime) {
        return dateTime != null ? dateTime.format(DATETIME_FORMATTER) : null;
    }

    public static LocalDate parseDate(String dateStr) {
        return dateStr != null && !dateStr.isBlank() ? LocalDate.parse(dateStr, DATE_FORMATTER) : null;
    }

    public static LocalDateTime parseDateTime(String dateTimeStr) {
        return dateTimeStr != null && !dateTimeStr.isBlank() ? 
                LocalDateTime.parse(dateTimeStr, DATETIME_FORMATTER) : null;
    }

    public static boolean isDateInPast(LocalDate date) {
        return date != null && date.isBefore(LocalDate.now());
    }

    public static boolean isDateInFuture(LocalDate date) {
        return date != null && date.isAfter(LocalDate.now());
    }

    public static int daysBetween(LocalDate start, LocalDate end) {
        return start != null && end != null ? 
                (int) java.time.temporal.ChronoUnit.DAYS.between(start, end) : 0;
    }
}
