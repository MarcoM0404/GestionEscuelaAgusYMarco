package com.example.app.base.repository;

import com.example.app.base.domain.Administrator;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.data.jpa.repository.Query;

import java.util.List;

public interface AdministratorRepository extends JpaRepository<Administrator, Long> {

    boolean existsByUserId(Long userId);

    // Carga cada Administrator con su User para evitar LazyInitializationException
    @Query("SELECT a FROM Administrator a LEFT JOIN FETCH a.user")
    List<Administrator> findAllWithUser();
}

