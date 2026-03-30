package com.example.model;

import jakarta.persistence.DiscriminatorValue;
import jakarta.persistence.Entity;
import jakarta.persistence.JoinColumn;
import jakarta.persistence.ManyToOne;

@Entity
@DiscriminatorValue("TEACHER")
public class Teacher extends Person {

    private String departmentName;

    @ManyToOne
    @JoinColumn(name = "dept_id")
    private Department department;

    public Teacher() {}

    public Teacher(String name, int age, String departmentName) {
        super(name, age);
        this.departmentName = departmentName;
    }

    public String getDepartmentName() { return departmentName; }
    public void setDepartmentName(String departmentName) { this.departmentName = departmentName; }

    public Department getDepartment() { return department; }
    public void setDepartment(Department department) { this.department = department; }

    @Override
    public String toString() {
        return "Teacher{" +
                "id=" + getId() +
                ", name='" + getName() + '\'' +
                ", age=" + getAge() +
                ", departmentName='" + departmentName + '\'' +
                '}';
    }
}
