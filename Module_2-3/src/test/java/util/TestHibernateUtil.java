package util;

import entity.User;
import org.hibernate.SessionFactory;
import org.hibernate.cfg.Configuration;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Testcontainers;

@Testcontainers
public class TestHibernateUtil {

    private static PostgreSQLContainer<?> postgresContainer;
    private static SessionFactory sessionFactory;

    public static synchronized PostgreSQLContainer<?> getPostgresContainer() {
        if (postgresContainer == null) {
            postgresContainer = new PostgreSQLContainer<>("postgres:15")
                    .withDatabaseName("testdb")
                    .withUsername("test")
                    .withPassword("test");
            postgresContainer.start();
        }
        return postgresContainer;
    }

    public static synchronized SessionFactory getTestSessionFactory() {
        if (sessionFactory == null) {
            PostgreSQLContainer<?> container = getPostgresContainer();

            Configuration configuration = new Configuration();
            configuration.configure("test-hibernate.cfg.xml");
            configuration.setProperty("hibernate.connection.url", container.getJdbcUrl());
            configuration.setProperty("hibernate.connection.username", container.getUsername());
            configuration.setProperty("hibernate.connection.password", container.getPassword());
            configuration.addAnnotatedClass(User.class);

            sessionFactory = configuration.buildSessionFactory();
        }
        return sessionFactory;
    }

    public static void close() {
        if (sessionFactory != null) {
            sessionFactory.close();
            sessionFactory = null;
        }
        if (postgresContainer != null && postgresContainer.isRunning()) {
            postgresContainer.stop();
            postgresContainer = null;
        }
    }
}
