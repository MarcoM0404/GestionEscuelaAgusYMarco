package com.example.app.base.service;

import com.example.app.base.domain.User;
import com.example.app.base.repository.UserRepository;
import com.example.app.security.AppRoles;
import org.springframework.security.crypto.password.PasswordEncoder;
import org.springframework.stereotype.Service;

import java.util.List;
import java.util.Optional;

@Service
public class UserService {

    private final UserRepository repo;
    private final PasswordEncoder encoder;

    public UserService(UserRepository repo, PasswordEncoder encoder) {
        this.repo = repo;
        this.encoder = encoder;
    }

    public Optional<User> findByUsername(String username){
        return repo.findByUsername(username);
    }

    public List<User> findAll()                 { return repo.findAll(); }
    public Optional<User> findById(Long id)     { return repo.findById(id); }
    public User save(User user)                 { return repo.save(user); }
    public void deleteById(Long id)             { repo.deleteById(id); }

    public boolean existsByUsername(String username){
        return repo.findByUsername(username).isPresent();
    }

    public User createAdmin(String username, String rawPassword){
        var user = new User(username, encoder.encode(rawPassword), AppRoles.ADMIN);
        return repo.save(user);
    }
}