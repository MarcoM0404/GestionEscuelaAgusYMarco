package com.example.app.base.repository;

import com.example.app.base.domain.Student;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
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

    @Query("""
           SELECT s
           FROM Student s
           JOIN FETCH s.user u
           WHERE s.id = :id
           """)
    Optional<Student> findWithUserById(@Param("id") Long id);
}
