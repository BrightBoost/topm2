package com.example.model;

import jakarta.persistence.*;

/**
 * TODO: Maak van deze klasse een Hibernate entity.
 *
 * Velden:
 * - id (Long, auto-gegenereerd)
 * - name (String)
 * - credits (int)
 * - description (String, kolom: "course_description")
 *
 * Relatie:
 * - Een cursus heeft meerdere enrollments (@OneToMany)
 * - Denk na over: mappedBy en of je hier cascade nodig hebt
 */
public class Course {

    private Long id;
    private String name;
    private int credits;
    private String description;

    // TODO: Voeg een List<Enrollment> toe met de juiste relatie-annotatie

    public Course() {}

    public Course(String name, int credits, String description) {
        this.name = name;
        this.credits = credits;
        this.description = description;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public int getCredits() { return credits; }
    public void setCredits(int credits) { this.credits = credits; }

    public String getDescription() { return description; }
    public void setDescription(String description) { this.description = description; }

    // TODO: Voeg getter en setter toe voor enrollments

    @Override
    public String toString() {
        return "Course{id=" + id + ", name='" + name + "', credits=" + credits +
                ", description='" + description + "'}";
    }
}
