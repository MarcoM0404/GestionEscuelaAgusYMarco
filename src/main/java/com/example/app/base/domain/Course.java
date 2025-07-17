package com.example.app.base.domain;

import java.util.*;
import jakarta.persistence.*;

@Entity
@Table(name = "courses")
public class Course {
	
	@Id
	@GeneratedValue(strategy = GenerationType.IDENTITY)
	private Long id;
	
	public Long getId() {
		return id;
	}
	
	@Column(name = "name", nullable = false)
	private String name;
	
	@ManyToOne(optional = false)
	@JoinColumn(name = "professor_id", nullable = false)
	private Professor professor;
	
	@OneToMany(mappedBy = "course", cascade = CascadeType.ALL, orphanRemoval = true)
	private List<Seat> seats = new ArrayList<>();

	public String getName() {
		return name;
	}

	public void setName(String name) {
		this.name = name;
	}

	public Professor getProfessor() {
		return professor;
	}

	public void setProfessor(Professor professor) {
		this.professor = professor;
	}

	public List<Seat> getSeats() {
		return seats;
	}

	public void setSeats(List<Seat> seats) {
		this.seats = seats;
	}
}