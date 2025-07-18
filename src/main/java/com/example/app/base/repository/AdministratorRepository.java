package com.example.app.base.repository;

import com.example.app.base.domain.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.query.Param;
import java.util.List;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {

    boolean existsByUserId(Long userId);

    @Query("SELECT a FROM Administrator a LEFT JOIN FETCH a.user")
    List<Administrator> findAllWithUser();

    @Query("""
        SELECT a FROM Administrator a
        LEFT JOIN FETCH a.user u
        WHERE LOWER(u.username) LIKE LOWER(CONCAT('%', :term, '%'))
           OR LOWER(a.name)     LIKE LOWER(CONCAT('%', :term, '%'))
    """)
    List<Administrator> searchWithUser(@Param("term") String term);
}
