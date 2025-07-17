package com.example.app.base.service;

import com.example.app.base.domain.Course;
import com.example.app.base.repository.CourseRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository repo;

    @Autowired
    public CourseService(CourseRepository repo) {
        this.repo = repo;
    }

    public List<Course> findAll() {
        return repo.findAll();
    }

    public Optional<Course> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public List<Course> findByProfessorId(Long professorId) {
        return repo.findByProfessorId(professorId);
    }
    
    public Course save(Course course) {
        return repo.save(course);
    }

}