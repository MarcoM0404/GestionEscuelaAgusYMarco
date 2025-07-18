package com.example.app.base.service;

import com.example.app.base.domain.Course;
import com.example.app.base.repository.CourseRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class CourseService {

    private final CourseRepository repo;

    public CourseService(CourseRepository repo) {
        this.repo = repo;
    }

    public List<Course> findAll()                 { return repo.findAll(); }
    public Optional<Course> findById(Long id)     { return repo.findById(id); }
    public void deleteById(Long id)               { repo.deleteById(id); }
    public Course save(Course course)             { return repo.save(course); }

    public List<Course> findByProfessorId(Long id){ return repo.findByProfessorId(id); }

    public List<Course> search(String term) {
        return term == null || term.isBlank()
               ? repo.findAll()
               : repo.findByNameContainingIgnoreCase(term);
    }
}
