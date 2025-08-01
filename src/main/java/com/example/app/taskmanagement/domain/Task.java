package com.example.app.taskmanagement.domain;

import com.example.app.base.domain.AbstractEntity;
import jakarta.persistence.*;
import jakarta.validation.constraints.Size;
import org.jspecify.annotations.Nullable;

import java.time.Instant;
import java.time.LocalDate;

@Entity
@Table(name = "task")
public class Task extends AbstractEntity {   // ← sin <Long>

    public static final int DESCRIPTION_MAX_LENGTH = 255;

    /* ---------- columnas propias ---------- */

    @Column(name = "description", nullable = false, length = DESCRIPTION_MAX_LENGTH)
    @Size(max = DESCRIPTION_MAX_LENGTH)
    private String description;

    @Column(name = "creation_date", nullable = false)
    private Instant creationDate;

    @Column(name = "due_date")
    @Nullable
    private LocalDate dueDate;

    /* ---------- getters / setters ---------- */

    public String getDescription() {
        return description;
    }
    public void setDescription(String description) {
        this.description = description;
    }

    public Instant getCreationDate() {
        return creationDate;
    }
    public void setCreationDate(Instant creationDate) {
        this.creationDate = creationDate;
    }

    public @Nullable LocalDate getDueDate() {
        return dueDate;
    }
    public void setDueDate(@Nullable LocalDate dueDate) {
        this.dueDate = dueDate;
    }
}