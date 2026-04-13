package com.example.model;

import jakarta.persistence.*;
import java.util.ArrayList;
import java.util.List;

@Entity
@Table(name = "students")
public class Student {

    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;

    @Column(nullable = false)
    private String name;

    @Column(name = "student_number")
    private String studentNumber;

    private String email;

    @OneToMany(mappedBy = "student", cascade = CascadeType.PERSIST)
    private List<Enrollment> enrollments = new ArrayList<>();

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

    public List<Enrollment> getEnrollments() { return enrollments; }
    public void setEnrollments(List<Enrollment> enrollments) { this.enrollments = enrollments; }

    /**
     * Helper: schrijft de student in voor een cursus.
     * Houdt beide kanten van de bidirectionele relatie in sync.
     */
    public Enrollment enroll(Course course) {
        Enrollment enrollment = new Enrollment(java.time.LocalDate.now(), null);
        enrollment.setStudent(this);
        enrollment.setCourse(course);
        this.enrollments.add(enrollment);
        return enrollment;
    }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', studentNumber='" + studentNumber + "'}";
    }
}
