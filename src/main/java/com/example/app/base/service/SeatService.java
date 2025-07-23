package com.example.app.base.service;

import com.example.app.base.domain.Seat;
import com.example.app.base.repository.SeatRepository;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;
import java.util.List;
import java.util.Optional;
import java.util.UUID;

@Service
public class SeatService {

    private final SeatRepository repo;

    public SeatService(SeatRepository repo) { this.repo = repo; }

    public static class DuplicateEnrollmentException extends RuntimeException {
        public DuplicateEnrollmentException() {
            super("El alumno ya está inscripto en este curso");
        }
    }

    public List<Seat> findAll()             { return repo.findAll(); }
    public Optional<Seat> findById(Long id) { return repo.findById(id); }

    @Transactional
    public Seat save(Seat seat) {

        Long courseId  = seat.getCourse().getId();
        Long studentId = seat.getStudent().getId();

        boolean exists = repo.existsByCourseIdAndStudentId(courseId, studentId);

        if (exists && (seat.getId() == null ||
              !repo.findById(seat.getId())
                   .map(s -> s.getCourse().getId().equals(courseId)
                          && s.getStudent().getId().equals(studentId))
                   .orElse(false))) {
            throw new DuplicateEnrollmentException();
        }
        return repo.save(seat);
    }

    public void deleteById(Long id) { repo.deleteById(id); }

    public List<Seat> findByCourseId(Long id)           { return repo.findByCourseId(id); }
    public List<Seat> findByStudentUserId(Long userId)  { return repo.findByStudentUserId(userId); }
    public List<Seat> findByStudentName(String name)    { return repo.findByStudentNameContainingIgnoreCase(name); }
    public List<Seat> findByStudentNumber(UUID number)  { return repo.findByStudentStudentNumber(number); }
    public long countByCourseId(Long courseId)          { return repo.countByCourseId(courseId); }
    public List<Seat> findAllOrdered() 					{return repo.findAllByOrderByYearDesc(); }
}
