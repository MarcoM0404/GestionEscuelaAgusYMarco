package com.example.app.security;

public enum AppRoles {
    ADMIN,
    PROFESSOR,
    STUDENT;

    public String asRole() {
        return "ROLE_" + name();
    }
}