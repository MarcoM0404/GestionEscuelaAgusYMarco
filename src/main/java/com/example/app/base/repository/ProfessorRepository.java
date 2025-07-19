package com.example.app.base.repository;

import com.example.app.base.domain.Professor;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import org.springframework.stereotype.Repository;

import java.util.List;
import java.util.Optional;

@Repository
public interface ProfessorRepository extends JpaRepository<Professor, Long> {

    /* ------ búsquedas existentes ------ */
    Optional<Professor> findByUserId(Long userId);
    List<Professor> findByNameContainingIgnoreCase(String name);
    List<Professor> findByNameContainingIgnoreCaseOrEmailContainingIgnoreCase(
        String name, String email
    );

    /* ------ NUEVO: cargar el User con JOIN FETCH ------ */
    @Query("""
           SELECT p
           FROM Professor p
           JOIN FETCH p.user u
           WHERE p.id = :id
           """)
    Optional<Professor> findWithUserById(@Param("id") Long id);
}
