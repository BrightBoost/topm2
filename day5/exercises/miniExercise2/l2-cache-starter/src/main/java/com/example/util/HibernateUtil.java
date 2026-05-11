package com.example.util;

import com.example.model.Course;
import com.example.model.Department;
import org.hibernate.SessionFactory;
import org.hibernate.boot.registry.StandardServiceRegistryBuilder;
import org.hibernate.cfg.Configuration;
import org.hibernate.cfg.Environment;
import org.hibernate.service.ServiceRegistry;

import java.util.Properties;

public final class HibernateUtil {

    private HibernateUtil() {
    }

    public static SessionFactory buildSessionFactory(String databaseName, boolean showSql, boolean enableL2Cache) {
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

        if (enableL2Cache) {
            // TODO: Enable second-level cache
            // settings.put(Environment.USE_SECOND_LEVEL_CACHE, "true");
            // settings.put("hibernate.cache.region.factory_class", "org.hibernate.cache.jcache.internal.JCacheRegionFactory");
            // settings.put("hibernate.javax.cache.provider", "com.github.benmanes.caffeine.jcache.spi.CaffeineCachingProvider");
            // settings.put("hibernate.javax.cache.missing_cache_strategy", "create");
        }

        configuration.setProperties(settings);
        // TODO: Add entity classes and @Cacheable annotation
        // configuration.addAnnotatedClass(Department.class);
        // configuration.addAnnotatedClass(Course.class);

        ServiceRegistry serviceRegistry = new StandardServiceRegistryBuilder()
                .applySettings(configuration.getProperties())
                .build();

        return configuration.buildSessionFactory(serviceRegistry);
    }
}
