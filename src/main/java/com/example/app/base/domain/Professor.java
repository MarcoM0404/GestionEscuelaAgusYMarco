package com.example.app.base.domain;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "professors")
@PrimaryKeyJoinColumn(name = "id")
public class Professor extends Person {

    @Column(nullable = false)
    private Double salary;

    @OneToMany(mappedBy = "professor", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Course> courses = new ArrayList<>();

    public Professor() {}

    public Professor(String name, String email, Double salary) {
        setName(name);
        setEmail(email);
        this.salary = salary;
    }

    public Double getSalary() { return salary; }
    public void setSalary(Double salary) { this.salary = salary; }

    public List<Course> getCourses() { return courses; }
    public void setCourses(List<Course> courses) { this.courses = courses; }
}