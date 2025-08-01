package com.example.app.base.domain;

import jakarta.persistence.GeneratedValue;
import jakarta.persistence.GenerationType;
import jakarta.persistence.Id;
import jakarta.persistence.MappedSuperclass;
import org.springframework.data.util.ProxyUtils;

@MappedSuperclass
public abstract class AbstractEntity {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    public Long getId() {
        return id;
    }

    public void setId(Long id) {
        this.id = id;
    }

    @Override
    public String toString() {
        return "%s{id=%s}".formatted(getClass().getSimpleName(), getId());
    }

    @Override
    public int hashCode() {
        return ProxyUtils.getUserClass(getClass()).hashCode();
    }

    @Override
    public boolean equals(Object obj) {
        if (obj == null) return false;
        if (obj == this) return true;

        var thisUserClass = ProxyUtils.getUserClass(getClass());
        var otherUserClass = ProxyUtils.getUserClass(obj);
        if (!thisUserClass.equals(otherUserClass)) return false;

        return getId() != null && getId().equals(((AbstractEntity) obj).getId());
    }
}
