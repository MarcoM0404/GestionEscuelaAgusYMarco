package com.example.app.base.repository;

import com.example.app.base.domain.Address;
import java.util.List;
import org.springframework.data.jpa.repository.JpaRepository;
import org.springframework.stereotype.Repository;

@Repository
public interface AddressRepository extends JpaRepository<Address, Long> {

    List<Address> findByCity(String city);
}
