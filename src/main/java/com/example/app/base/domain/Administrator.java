package com.example.app.base.domain;

import jakarta.persistence.Entity;
import jakarta.persistence.Table;

@Entity
@Table(name = "administrators")
public class Administrator extends Person {
    // sin campos extra por ahora; hereda name, email, phone y address
}
