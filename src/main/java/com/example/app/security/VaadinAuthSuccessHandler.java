package com.example.app.security;

import jakarta.servlet.ServletException;
import jakarta.servlet.http.HttpServletRequest;
import jakarta.servlet.http.HttpServletResponse;
import org.springframework.security.core.Authentication;
import org.springframework.security.web.authentication.AuthenticationSuccessHandler;
import org.springframework.stereotype.Component;

import java.io.IOException;

@Component
public class VaadinAuthSuccessHandler implements AuthenticationSuccessHandler {

    @Override
    public void onAuthenticationSuccess(HttpServletRequest request,
                                        HttpServletResponse response,
                                        Authentication authentication)
            throws IOException, ServletException {


        String role = authentication.getAuthorities().stream()
                .findFirst()
                .map(a -> a.getAuthority())
                .orElse("");

        if ("ROLE_ADMIN".equals(role)) {
            response.sendRedirect("/admin");
        } else if ("ROLE_PROFESSOR".equals(role)) {
            response.sendRedirect("/professor");
        } else if ("ROLE_STUDENT".equals(role)) {
            response.sendRedirect("/student");
        } else {
            response.sendRedirect("/");
        }
    }
}