package com.example.app.base.service;

import com.example.app.base.domain.Role;
import com.example.app.base.domain.User;
import com.example.app.base.repository.UserRepository;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {
	
    @Autowired
    private UserRepository repo;

    public User findByUsername(String username) {
        return repo.findByUsername(username);
    }
    
    public UserService(UserRepository repo) {
        this.repo = repo;
    }

    public List<User> findAll() {
        return repo.findAll();
    }

    public Optional<User> findById(Long id) {
        return repo.findById(id);
    }

    public void deleteById(Long id) {
        repo.deleteById(id);
    }
    
    public User save(User user) {
        return repo.save(user);
    }
    
    public boolean existsByUsername(String username){
        return repo.findByUsername(username) != null;
    }

    public User createAdmin(String username, String rawPassword, PasswordEncoder encoder){
        User u = new User(username, encoder.encode(rawPassword), Role.ADMIN);
        return repo.save(u);
    }
}
