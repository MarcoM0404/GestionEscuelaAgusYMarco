package com.example.app.base.service;

import com.example.app.base.domain.Student;

import com.example.app.base.repository.StudentRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {
    private final StudentRepository repo;

    @Autowired
    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public List<Student> findAll() {
        return repo.findAll();
    }

    public Optional<Student> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public Optional<Student> findByUserId(Long userId) {
        return repo.findByUserId(userId);
    }
    
    public List<Student> findByNameContainingIgnoreCase(String name) {
        return repo.findByNameContainingIgnoreCase(name);
    }
    
    public Student save(Student student) {
        return repo.save(student);
    }

}