package com.example.app.security.domain;


/**
 * Value object simple para representar la identidad de un usuario
 * (sólo se usa en el perfil de desarrollo).
 */
public record UserId(String value) {

    public static UserId of(String value) {
        return new UserId(value);
    }

    @Override public String toString() {
        return value;
    }
}