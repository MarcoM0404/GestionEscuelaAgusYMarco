package com.example.app.base.domain;


import jakarta.persistence.*;
import java.time.LocalDate;

@Entity
@Table(
    name = "seats",
    uniqueConstraints = @UniqueConstraint(columnNames = { "student_id", "course_id" })
)
public class Seat extends AbstractEntity {

    @Column(name = "exam_date", nullable = false)
    private LocalDate year;

    @Column(name = "mark")
    private Double mark;

    private LocalDate evaluationDate;

    @ManyToOne(optional = false)
    @JoinColumn(name = "student_id", nullable = false)
    private Student student;

    @ManyToOne(optional = false)
    @JoinColumn(name = "course_id", nullable = false)
    private Course course;

    public LocalDate getYear() { return year; }
    public void setYear(LocalDate year) { this.year = year; }

    public Double getMark() { return mark; }
    public void setMark(Double mark) { this.mark = mark; }

    public LocalDate getEvaluationDate() { return evaluationDate; }
    public void setEvaluationDate(LocalDate evaluationDate) { this.evaluationDate = evaluationDate; }

    public Student getStudent() { return student; }
    public void setStudent(Student student) { this.student = student; }

    public Course getCourse() { return course; }
    public void setCourse(Course course) { this.course = course; }
}