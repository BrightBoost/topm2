package com.example.model;

import jakarta.persistence.*;

/**
 * TODO: Maak van deze klasse een Hibernate entity.
 *
 * Velden:
 * - id (Long, auto-gegenereerd)
 * - name (String)
 * - studentNumber (String, kolom: "student_number")
 * - email (String)
 *
 * Relatie:
 * - Een student heeft meerdere enrollments (@OneToMany)
 * - Denk na over: mappedBy, cascade, en het initialiseren van de lijst
 */
public class Student {

    private Long id;
    private String name;
    private String studentNumber;
    private String email;

    // TODO: Voeg een List<Enrollment> toe met de juiste relatie-annotatie

    public Student() {}

    public Student(String name, String studentNumber, String email) {
        this.name = name;
        this.studentNumber = studentNumber;
        this.email = email;
    }

    public Long getId() { return id; }
    public void setId(Long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getStudentNumber() { return studentNumber; }
    public void setStudentNumber(String studentNumber) { this.studentNumber = studentNumber; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    // TODO: Voeg getter en setter toe voor enrollments

    // TODO (Bonus): Maak een helper-methode enroll(Course course) die:
    //  - Een nieuwe Enrollment aanmaakt met LocalDate.now()
    //  - De student op de enrollment zet (owning side)
    //  - De enrollment aan deze student's lijst toevoegt (inverse side)
    //  - De enrollment retourneert

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', studentNumber='" + studentNumber +
                "', email='" + email + "'}";
    }
}
