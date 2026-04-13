package com.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

/**
 * Versie van Student ZONDER mappedBy — om te demonsteren wat er misgaat.
 * Hibernate maakt een join table aan in plaats van de foreign key te gebruiken.
 * Gebruik deze klasse ALLEEN in Demo1BrokenMappedBy.
 */
@Entity
@Table(name = "students_broken")
public class StudentBroken {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    // FOUT: geen mappedBy! Hibernate maakt een join table aan.
    @OneToMany(cascade = CascadeType.PERSIST)
    private List<Enrollment> enrollments = new ArrayList<>();

    public StudentBroken() {}

    public StudentBroken(String name) {
        this.name = name;
    }

    public Long getId() { return id; }
    public String getName() { return name; }
    public List<Enrollment> getEnrollments() { return enrollments; }

    @Override
    public String toString() {
        return "StudentBroken{id=" + id + ", name='" + name + "'}";
    }
}
