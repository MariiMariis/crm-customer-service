package com.pb.crm.customer;

import jakarta.persistence.*;

import java.time.Instant;


@Entity
@Table(name = "customers")
public class Customer {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(length = 20)
    private String phone;

    @Column(length = 20)
    private String document;

    @Column(name = "created_at", nullable = false, updatable = false)
    private Instant createdAt;

    protected Customer() {
        // exigido pelo JPA
    }

    public Customer(String name, String email, String phone, String document) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
    }

    @PrePersist
    void onCreate() {
        this.createdAt = Instant.now();
    }

    public void update(String name, String email, String phone, String document) {
        this.name = name;
        this.email = email;
        this.phone = phone;
        this.document = document;
    }

    public Long getId() {
        return id;
    }

    public String getName() {
        return name;
    }

    public String getEmail() {
        return email;
    }

    public String getPhone() {
        return phone;
    }

    public String getDocument() {
        return document;
    }

    public Instant getCreatedAt() {
        return createdAt;
    }
}
