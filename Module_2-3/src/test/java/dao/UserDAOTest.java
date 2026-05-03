package dao;

import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;
import org.junit.jupiter.api.*;
import org.testcontainers.containers.PostgreSQLContainer;
import org.testcontainers.junit.jupiter.Container;
import org.testcontainers.junit.jupiter.Testcontainers;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@Testcontainers
@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    @Container
    static PostgreSQLContainer<?> postgres = new PostgreSQLContainer<>("postgres:15")
            .withDatabaseName("testdb")
            .withUsername("test")
            .withPassword("test");

    private static SessionFactory sessionFactory;
    private UserDAO userDAO;
    private Session session;
    private Transaction transaction;

    @BeforeAll
    static void setUpAll() {
        Configuration configuration = new Configuration();

        configuration.setProperty("hibernate.connection.driver_class", "org.postgresql.Driver");
        configuration.setProperty("hibernate.connection.url", postgres.getJdbcUrl());
        configuration.setProperty("hibernate.connection.username", postgres.getUsername());
        configuration.setProperty("hibernate.connection.password", postgres.getPassword());
        configuration.setProperty("hibernate.dialect", "org.hibernate.dialect.PostgreSQLDialect");
        configuration.setProperty("hibernate.hbm2ddl.auto", "create-drop");
        configuration.setProperty("hibernate.show_sql", "true");
        configuration.setProperty("hibernate.format_sql", "true");
        configuration.setProperty("hibernate.connection.pool_size", "3");

        configuration.addAnnotatedClass(User.class);

        sessionFactory = configuration.buildSessionFactory();
    }

    @AfterAll
    static void tearDownAll() {
        if (sessionFactory != null) {
            sessionFactory.close();
        }
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();

        session.createMutationQuery("DELETE FROM User").executeUpdate();
        transaction.commit();
    }

    @AfterEach
    void tearDown() {
        if (session != null && session.isOpen()) {
            session.close();
        }
    }

    @Test
    @Order(1)
    @DisplayName("Сохранение пользователя (успешное создание)")
    void testSaveUser() {
        User user = new User("Иван Петров", "ivan@example.com", 25);

        User savedUser = userDAO.save(user);

        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Иван Петров");
        assertThat(savedUser.getEmail()).isEqualTo("ivan@example.com");
        assertThat(savedUser.getAge()).isEqualTo(25);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Поиск пользователя по ID (успешное нахождение)")
    void testFindById() {
        User user = new User("Мария Сидорова", "maria@example.com", 30);
        User savedUser = userDAO.save(user);

        Optional<User> foundUser = userDAO.findById(savedUser.getId());

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Мария Сидорова");
        assertThat(foundUser.get().getEmail()).isEqualTo("maria@example.com");
    }

    @Test
    @Order(3)
    @DisplayName("Поиск пользователя по ID (пользователь не найден)")
    void testFindByIdNotFound() {
        Optional<User> foundUser = userDAO.findById(999L);

        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("Поиск всех пользователей (получение списка)")
    void testFindAll() {
        User user1 = new User("Алексей Иванов", "alexey@example.com", 28);
        User user2 = new User("Елена Петрова", "elena@example.com", 35);
        userDAO.save(user1);
        userDAO.save(user2);

        List<User> users = userDAO.findAll();

        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("Алексей Иванов", "Елена Петрова");
    }

    @Test
    @Order(5)
    @DisplayName("Поиск всех пользователей (пустой список)")
    void testFindAllEmpty() {
        List<User> users = userDAO.findAll();

        assertThat(users).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("Обновление пользователя (успешное обновление)")
    void testUpdateUser() {
        User user = new User("Оригинальное имя", "original@example.com", 20);
        User savedUser = userDAO.save(user);

        savedUser.setName("Обновленное имя");
        savedUser.setAge(25);
        User updatedUser = userDAO.update(savedUser);

        assertThat(updatedUser.getName()).isEqualTo("Обновленное имя");
        assertThat(updatedUser.getAge()).isEqualTo(25);

        Optional<User> verified = userDAO.findById(savedUser.getId());
        assertThat(verified).isPresent();
        assertThat(verified.get().getName()).isEqualTo("Обновленное имя");
    }

    @Test
    @Order(7)
    @DisplayName("Удаление пользователя по ID (успешное удаление)")
    void testDeleteById() {
        User user = new User("Для удаления", "delete@example.com", 40);
        User savedUser = userDAO.save(user);

        boolean deleted = userDAO.deleteById(savedUser.getId());

        assertThat(deleted).isTrue();
        Optional<User> foundUser = userDAO.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("Удаление пользователя по ID (пользователь не найден)")
    void testDeleteByIdNotFound() {
        boolean deleted = userDAO.deleteById(999L);

        assertThat(deleted).isFalse();
    }

    @Test
    @Order(9)
    @DisplayName("Удаление пользователя по объекту (успешное удаление)")
    void testDeleteByEntity() {
        User user = new User("Удаление по объекту", "deleteEntity@example.com", 35);
        User savedUser = userDAO.save(user);

        userDAO.delete(savedUser);

        Optional<User> foundUser = userDAO.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("Поиск пользователя по email (успешное нахождение)")
    void testFindByEmail() {
        User user = new User("Email тест", "uniquetest@example.com", 22);
        userDAO.save(user);

        Optional<User> foundUser = userDAO.findByEmail("uniquetest@example.com");

        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Email тест");
    }

    @Test
    @Order(11)
    @DisplayName("Поиск пользователя по email (email не найден)")
    void testFindByEmailNotFound() {
        Optional<User> foundUser = userDAO.findByEmail("nonexistent@example.com");

        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("Сохранение пользователя с null полями (должно выбросить исключение)")
    void testSaveUserWithNullFields() {
        User user = new User(null, null, null);

        assertThatThrownBy(() -> userDAO.save(user))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Order(13)
    @DisplayName("Сохранение пользователя с дублирующим email (должно выбросить исключение)")
    void testSaveUserWithDuplicateEmail() {
        User user1 = new User("Первый", "duplicate@example.com", 25);
        User user2 = new User("Второй", "duplicate@example.com", 30);
        userDAO.save(user1);

        assertThatThrownBy(() -> userDAO.save(user2))
                .isInstanceOf(RuntimeException.class);
    }
}
