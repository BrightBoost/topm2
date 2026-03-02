package nl.topicus.studentdao;

/**
 * Representeert een student in de database.
 *
 * Deze class is hetzelfde als in Mini Exercise 3.
 * Als je die oefening hebt afgerond, kun je je eigen implementatie
 * hiernaartoe kopieren.
 */
public class Student {

    private long id;
    private String name;
    private String email;
    private int age;

    public Student(long id, String name, String email, int age) {
        this.id = id;
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public Student(String name, String email, int age) {
        this.name = name;
        this.email = email;
        this.age = age;
    }

    public long getId() { return id; }
    public void setId(long id) { this.id = id; }

    public String getName() { return name; }
    public void setName(String name) { this.name = name; }

    public String getEmail() { return email; }
    public void setEmail(String email) { this.email = email; }

    public int getAge() { return age; }
    public void setAge(int age) { this.age = age; }

    @Override
    public String toString() {
        return "Student{id=" + id + ", name='" + name + "', email='" + email + "', age=" + age + "}";
    }
}
