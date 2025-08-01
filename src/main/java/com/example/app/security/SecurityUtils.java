package com.example.app.security;

import org.springframework.security.authentication.AnonymousAuthenticationToken;
import org.springframework.security.core.GrantedAuthority;
import org.springframework.security.core.context.SecurityContextHolder;

public class SecurityUtils {

    public static boolean isUserLoggedIn() {
        var auth = SecurityContextHolder.getContext().getAuthentication();
        return auth != null
            && auth.isAuthenticated()
            && !(auth instanceof AnonymousAuthenticationToken);
    }

    public static String getUsername() {
        return SecurityContextHolder.getContext()
                                    .getAuthentication()
                                    .getName();
    }
    public static String getUserRole() {
        return SecurityContextHolder.getContext().getAuthentication()
                   .getAuthorities()
                   .stream()
                   .findFirst()
                   .map(GrantedAuthority::getAuthority)
                   .orElse("");
        }
}