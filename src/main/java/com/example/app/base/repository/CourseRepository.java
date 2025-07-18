package com.example.app.base.repository;

import com.example.app.base.domain.Course;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface CourseRepository extends JpaRepository<Course, Long> {

    List<Course> findByProfessorId(Long professorId);

    // 👉 Nuevo: búsqueda por nombre (case-insensitive)
    List<Course> findByNameContainingIgnoreCase(String name);
}
