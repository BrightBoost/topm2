package nl.topicus.jdbc.part4mapping;

public record Student(long id, String name, String email, int age) {
    @Override
    public String toString() {
        return "Student{" +
                "id=" + id +
                ", name='" + name + '\'' +
                ", email='" + email + '\'' +
                ", age=" + age +
                '}';
    }
}
