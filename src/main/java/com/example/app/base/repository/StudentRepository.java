package com.example.app.base.repository;

import com.example.app.base.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;
import java.util.List;
import java.util.Optional;

@Repository
public interface StudentRepository extends JpaRepository<Student, Long> {

    Optional<Student> findByUserId(Long userId);

    List<Student> findByNameContainingIgnoreCase(String name);

    List<Student> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String name, String email
    );
}
