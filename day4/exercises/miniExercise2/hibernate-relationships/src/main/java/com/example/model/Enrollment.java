package com.example.model;

import jakarta.persistence.*;
import java.time.LocalDate;

/**
 * TODO: Maak van deze klasse een Hibernate entity.
 *
 * Velden:
 * - id (Long, auto-gegenereerd)
 * - enrollmentDate (LocalDate, kolom: "enrollment_date")
 * - grade (String, nullable)
 *
 * Relaties:
 * - Een enrollment hoort bij één student (@ManyToOne)
 * - Een enrollment hoort bij één course (@ManyToOne)
 * - Denk na over: @JoinColumn en welke kant de "owning side" is
 */
public class Enrollment {

    private Long id;
    private LocalDate enrollmentDate;
    private String grade;

    // TODO: Voeg een Student-veld toe met de juiste relatie-annotatie

    // TODO: Voeg een Course-veld toe met de juiste relatie-annotatie

    public Enrollment() {}

    public Enrollment(LocalDate enrollmentDate, String grade) {
        this.enrollmentDate = enrollmentDate;
        this.grade = grade;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public LocalDate getEnrollmentDate() { return enrollmentDate; }
    public void setEnrollmentDate(LocalDate enrollmentDate) { this.enrollmentDate = enrollmentDate; }

    public String getGrade() { return grade; }
    public void setGrade(String grade) { this.grade = grade; }

    // TODO: Voeg getters en setters toe voor student en course

    @Override
    public String toString() {
        return "Enrollment{id=" + id + ", enrollmentDate=" + enrollmentDate +
                ", grade='" + grade + "'}";
    }
}
