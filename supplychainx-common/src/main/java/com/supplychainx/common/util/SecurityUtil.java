package com.supplychainx.common.util;

import com.supplychainx.common.exception.UnauthorizedException;
import jakarta.servlet.http.HttpServletRequest;
import lombok.experimental.UtilityClass;
import org.springframework.web.context.request.RequestContextHolder;
import org.springframework.web.context.request.ServletRequestAttributes;


@UtilityClass
public class SecurityUtil {

    public static final String HEADER_EMAIL = "X-User-Email";
    public static final String HEADER_PASSWORD = "X-User-Password";
    public static final String HEADER_USER_ID = "X-User-Id";
    public static final String HEADER_USER_ROLE = "X-User-Role";


    public static HttpServletRequest getCurrentRequest() {
        ServletRequestAttributes attributes = 
            (ServletRequestAttributes) RequestContextHolder.getRequestAttributes();
        if (attributes == null) {
            throw new UnauthorizedException("No request context found");
        }
        return attributes.getRequest();
    }


//      Get user email from request header

    public static String getUserEmail() {
        HttpServletRequest request = getCurrentRequest();
        String email = request.getHeader(HEADER_EMAIL);
        if (email == null || email.isBlank()) {
            throw new UnauthorizedException("User email not found in headers");
        }
        return email;
    }


//      Get user password from request header (for authentication)

    public static String getUserPassword() {
        HttpServletRequest request = getCurrentRequest();
        String password = request.getHeader(HEADER_PASSWORD);
        if (password == null || password.isBlank()) {
            throw new UnauthorizedException("User password not found in headers");
        }
        return password;
    }


//      Get user ID from request header (set by SecurityAspect after authentication)

    public static Long getUserId() {
        HttpServletRequest request = getCurrentRequest();
        String userId = request.getHeader(HEADER_USER_ID);
        if (userId == null || userId.isBlank()) {
            throw new UnauthorizedException("User ID not found in headers");
        }
        return Long.parseLong(userId);
    }


//      Get user role from request header (set by SecurityAspect after authentication)

    public static String getUserRole() {
        HttpServletRequest request = getCurrentRequest();
        String role = request.getHeader(HEADER_USER_ROLE);
        if (role == null || role.isBlank()) {
            throw new UnauthorizedException("User role not found in headers");
        }
        return role;
    }


//      Check if current user has specific role

    public static boolean hasRole(String role) {
        try {
            return getUserRole().equalsIgnoreCase(role);
        } catch (Exception e) {
            return false;
        }
    }
}
