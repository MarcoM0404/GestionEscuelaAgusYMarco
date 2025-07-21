package com.example.app.base.repository;

import com.example.app.base.domain.Seat;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.UUID;

@Repository
public interface SeatRepository extends JpaRepository<Seat, Long> {

    List<Seat> findByStudentUserId(Long userId);
    List<Seat> findByStudentNameContainingIgnoreCase(String name);
    List<Seat> findByStudentStudentNumber(UUID studentNumber);
    List<Seat> findByCourseId(Long courseId);

    long countByCourseId(Long courseId);

    boolean existsByCourseIdAndStudentId(Long courseId, Long studentId);
}
