package com.example.demos;

import com.example.dto.StudentListRow;
import com.example.model.Student;
import com.example.util.DemoData;
import com.example.util.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.SessionFactory;

import java.util.List;

public class Demo5OptimizationChoices {

    public static void main(String[] args) {
        System.out.println("=== DEMO 5: OPTIMIZATION CHOICES ===\n");

        SessionFactory sessionFactory = HibernateUtil.buildSessionFactory("day5-optimization", false, false);
        try {
            DemoData.seed(sessionFactory);
            Long studentId = DemoData.firstStudentId(sessionFactory);

            runNaiveListScreen(sessionFactory);
            runProjectionListScreen(sessionFactory);
            runDetailScreenWithJoinFetch(sessionFactory, studentId);
        } finally {
            sessionFactory.close();
        }
    }

    private static void runNaiveListScreen(SessionFactory sessionFactory) {
        System.out.println("1. Naive list screen: load entities, then walk lazy relationships in a loop.");
        DemoData.resetStatistics(sessionFactory);

        try (Session session = sessionFactory.openSession()) {
            List<Student> students = session.createQuery("select s from Student s order by s.id", Student.class)
                    .getResultList();

            for (Student student : students) {
                System.out.println("   " + student.getName() + " | " +
                        student.getDepartment().getName() + " | courses=" + student.getEnrollments().size());
            }
        }

        DemoData.printStatistics(sessionFactory, "Naive list screen");
    }

    private static void runProjectionListScreen(SessionFactory sessionFactory) {
        System.out.println("2. Better list screen: projection with only the three fields the UI needs.");
        DemoData.resetStatistics(sessionFactory);

        try (Session session = sessionFactory.openSession()) {
            List<StudentListRow> rows = session.createQuery(
                    "select new com.example.dto.StudentListRow(s.name, s.email, d.name) " +
                            "from Student s join s.department d order by s.id",
                    StudentListRow.class
            ).getResultList();

            for (StudentListRow row : rows) {
                System.out.println("   " + row.name() + " | " + row.departmentName() + " | " + row.email());
            }
        }

        DemoData.printStatistics(sessionFactory, "Projection list screen");
    }

    private static void runDetailScreenWithJoinFetch(SessionFactory sessionFactory, Long studentId) {
        System.out.println("3. Detail screen: join fetch the exact graph needed for one page.");
        DemoData.resetStatistics(sessionFactory);

        try (Session session = sessionFactory.openSession()) {
            Student student = session.createQuery(
                            "select distinct s from Student s " +
                                    "left join fetch s.department " +
                                    "left join fetch s.enrollments e " +
                                    "left join fetch e.course " +
                                    "where s.id = :id",
                            Student.class
                    )
                    .setParameter("id", studentId)
                    .getSingleResult();

            System.out.println("   Student: " + student.getName());
            System.out.println("   Department: " + student.getDepartment().getName());
            System.out.println("   Enrollments: " + student.getEnrollments().size());
        }

        DemoData.printStatistics(sessionFactory, "Detail screen with join fetch");
        System.out.println("Takeaway: choose projections for list screens and targeted fetch plans for detail screens.\n");
    }
}