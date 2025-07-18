package com.example.app.base.repository;

import com.example.app.base.domain.Professor;
import java.util.List;
import java.util.Optional;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    Optional<Professor> findByUserId(Long userId);

    // 👉 Mantienes el de nombre …
    List<Professor> findByNameContainingIgnoreCase(String name);

    // 👉 … y añades búsqueda combinada por nombre O email
    List<Professor> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String name, String email
    );
}
