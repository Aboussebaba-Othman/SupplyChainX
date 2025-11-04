package com.supplychainx.common.constant;


public final class SecurityConstants {

    private SecurityConstants() {}

    public static final String HEADER_EMAIL = "X-User-Email";
    public static final String HEADER_PASSWORD = "X-User-Password";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";
    
    public static final int MIN_PASSWORD_LENGTH = 8;
    public static final int MAX_PASSWORD_LENGTH = 100;
}
