package com.example.app.base.service;

import com.example.app.base.domain.Professor;
import com.example.app.base.repository.ProfessorRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class ProfessorService {
    private final ProfessorRepository repo;

    @Autowired
    public ProfessorService(ProfessorRepository repo) {
        this.repo = repo;
    }

    public List<Professor> findAll() {
        return repo.findAll();
    }

    public Optional<Professor> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
    

    public List<Professor> findByNameContainingIgnoreCase(String txt) {
        return repo.findByNameContainingIgnoreCase(txt);
    }
    
    public Optional<Professor> findByUserId(Long userId) {
        return repo.findByUserId(userId);
    }

    public Professor save(Professor professor) {
        return repo.save(professor);
    }
}

