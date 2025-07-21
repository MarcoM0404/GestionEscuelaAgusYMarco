package com.example.app.base.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "persons")
@Inheritance(strategy = InheritanceType.JOINED)
public abstract class Person {

    @Id @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)            private String name;
    @Column(nullable = false, unique = true) private String email;
    private String phone;

    /* Address (sin cambios) */
    @OneToOne(fetch = FetchType.EAGER,
              cascade = CascadeType.ALL,
              optional = true)
    @JoinColumn(name = "address_id")
    private Address address;

    /* User — solo propagamos REMOVE */
    @OneToOne(fetch = FetchType.LAZY,
              cascade = CascadeType.REMOVE,   // ← ya NO incluye PERSIST
              orphanRemoval = true)           // ← sigue eliminando huérfanos
    @JoinColumn(name = "user_id")
    private User user;

    /* getters & setters … */
    // ...;

    /* ---------- getters / setters ---------- */

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public String getPhone() { return phone; }
    public void setPhone(String phone) { this.phone = phone; }

    public Address getAddress() { return address; }
    public void setAddress(Address address) { this.address = address; }

    public User getUser() { return user; }
    public void setUser(User user) { this.user = user; }
}
