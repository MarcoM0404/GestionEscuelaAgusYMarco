package com.example.app.base.domain;

import jakarta.persistence.*;

@Entity
@Table(name = "addresses")
public class Address extends AbstractEntity {

    @Column(nullable = false, length = 100)
    private String street;

    @Column(nullable = false, length = 50)
    private String city;

    @Column(nullable = false, length = 50)
    private String state;

    @Column(nullable = false, length = 50)
    private String country;


    public String getStreet() { return street; }
    public void setStreet(String street) { this.street = street; }


    public String getCity() { return city; }
    public void setCity(String city) { this.city = city; }

    public String getState() { return state; }
    public void setState(String state) { this.state = state; }

    public String getCountry() { return country; }
    public void setCountry(String country) { this.country = country; }
}