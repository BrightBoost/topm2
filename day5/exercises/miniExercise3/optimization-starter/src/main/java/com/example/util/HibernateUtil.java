package com.example.util;

import com.example.model.Course;
import com.example.model.Department;
import com.example.model.Enrollment;
import com.example.model.Student;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public final class HibernateUtil {

    private HibernateUtil() {
    }

    public static SessionFactory buildSessionFactory(String databaseName, boolean showSql) {
        Configuration configuration = new Configuration();

        Properties settings = new Properties();
        settings.put(Environment.DRIVER, "org.h2.Driver");
        settings.put(Environment.URL, "jdbc:h2:mem:" + databaseName + ";DB_CLOSE_DELAY=-1");
        settings.put(Environment.USER, "sa");
        settings.put(Environment.PASS, "");
        settings.put(Environment.DIALECT, "org.hibernate.dialect.H2Dialect");
        settings.put(Environment.HBM2DDL_AUTO, "create-drop");
        settings.put(Environment.SHOW_SQL, Boolean.toString(showSql));
        settings.put(Environment.FORMAT_SQL, "true");
        settings.put(Environment.HIGHLIGHT_SQL, "true");
        settings.put(Environment.GENERATE_STATISTICS, "true");

        configuration.setProperties(settings);
        configuration.addAnnotatedClass(Department.class);
        configuration.addAnnotatedClass(Student.class);
        configuration.addAnnotatedClass(Course.class);
        configuration.addAnnotatedClass(Enrollment.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        return configuration.buildSessionFactory(serviceRegistry);
    }
}
