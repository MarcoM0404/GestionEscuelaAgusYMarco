package com.example.app.security.domain;



public record UserId(String value) {

    public static UserId of(String value) {
        return new UserId(value);
    }

    @Override public String toString() {
        return value;
    }
}