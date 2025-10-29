package com.supplychainx.common.util;

import lombok.experimental.UtilityClass;


@UtilityClass
public class StringUtil {

    public static String generateCode(String prefix, Long id) {
        return String.format("%s-%06d", prefix, id);
    }

    public static String truncate(String str, int maxLength) {
        if (str == null || str.length() <= maxLength) {
            return str;
        }
        return str.substring(0, maxLength - 3) + "...";
    }

    public static boolean isNullOrBlank(String str) {
        return str == null || str.isBlank();
    }

    public static String capitalizeFirst(String str) {
        if (isNullOrBlank(str)) {
            return str;
        }
        return str.substring(0, 1).toUpperCase() + str.substring(1).toLowerCase();
    }
}
