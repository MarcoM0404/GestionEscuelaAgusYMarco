package com.example.app.base.service;

import com.example.app.base.domain.Seat;
import com.example.app.base.repository.SeatRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeatService {
    private final SeatRepository repo;

    @Autowired
    public SeatService(SeatRepository repo) {
        this.repo = repo;
    }
    
    public List<Seat> findAll() {
        return repo.findAll();
    }

    public Optional<Seat> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }

    public List<Seat> findByCourseId(Long courseId) {
        return repo.findByCourseId(courseId);
    }
    
    public List<Seat> findByStudentUserId(Long userId) {
        return repo.findByStudentUserId(userId);
    }
    
    public List<Seat> findByStudentName(String name) {
        return repo.findByStudentNameContainingIgnoreCase(name);
    }
    
    public List<Seat> findByStudentNumber(UUID studentNumber) {
        return repo.findByStudentStudentNumber(studentNumber);
    }
    
    public Seat save(Seat seat) {
        return repo.save(seat);
    }

}