package com.example.app.base.domain;


import com.example.app.security.AppRoles;

import jakarta.persistence.*;

@Entity
@Table(name = "users")
public class User extends AbstractEntity {

    @Column(nullable = false, unique = true, length = 50)
    private String username;

    @Column(nullable = false)
    private String password;

    @Enumerated(EnumType.STRING)
    @Column(nullable = false, length = 20)
    private AppRoles role;

    @OneToOne(mappedBy = "user", cascade = CascadeType.ALL)
    private Person person;

    public User() {}

    public User(String username, String password, AppRoles role) {
        this.username = username;
        this.password = password;
        this.role     = role;
    }

    public String getUsername() { return username; }
    public void setUsername(String username) { this.username = username; }

    public String getPassword() { return password; }
    public void setPassword(String password) { this.password = password; }

    public AppRoles getRole() { return role; }
    public void setRole(AppRoles role) { this.role = role; }

    public Person getPerson() { return person; }
    public void setPerson(Person person) { this.person = person; }
}