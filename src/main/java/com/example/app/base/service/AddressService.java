package com.example.app.base.service;

import com.example.app.base.domain.Address;
import com.example.app.base.repository.AddressRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Service;
import java.util.List;
import java.util.Optional;

@Service
public class AddressService {
    private final AddressRepository repo;

    @Autowired
    public AddressService(AddressRepository repo) {
        this.repo = repo;
    }

    public List<Address> findAll() {
        return repo.findAll();
    }

    public Optional<Address> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }


    public List<Address> findByCity(String city) {
        return repo.findByCity(city);
    }
    
    public Address save(Address address) {
        return repo.save(address);
    }
}