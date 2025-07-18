package com.example.app.base.service;

import com.example.app.base.domain.Student;
import com.example.app.base.repository.StudentRepository;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class StudentService {

    private final StudentRepository repo;

    public StudentService(StudentRepository repo) {
        this.repo = repo;
    }

    public List<Student> findAll()                   { return repo.findAll(); }
    public Optional<Student> findById(Long id)       { return repo.findById(id); }
    public void deleteById(Long id)                  { repo.deleteById(id); }
    public Student save(Student student)             { return repo.save(student); }

    public Optional<Student> findByUserId(Long id)   { return repo.findByUserId(id); }

    public List<Student> search(String term) {
        return term == null || term.isBlank()
               ? repo.findAll()
               : repo.findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(term, term);
    }
}
