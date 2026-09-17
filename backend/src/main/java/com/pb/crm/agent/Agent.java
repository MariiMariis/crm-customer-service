package com.pb.crm.agent;

import jakarta.persistence.*;


@Entity
@Table(name = "agents")
public class Agent {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false, length = 120)
    private String name;

    @Column(nullable = false, unique = true, length = 160)
    private String email;

    @Column(length = 80)
    private String department;

    @Column(nullable = false)
    private boolean active = true;

    protected Agent() {
        // exigido pelo JPA
    }

    public Agent(String name, String email, String department) {
        this.name = name;
        this.email = email;
        this.department = department;
    }

    public void update(String name, String email, String department, boolean active) {
        this.name = name;
        this.email = email;
        this.department = department;
        this.active = active;
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

    public String getDepartment() {
        return department;
    }

    public boolean isActive() {
        return active;
    }
}
