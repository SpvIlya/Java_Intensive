package dao;

import entity.User;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.junit.jupiter.api.*;
import util.TestHibernateUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserDAOTest {

    private static SessionFactory sessionFactory;
    private UserDAO userDAO;
    private Session session;
    private Transaction transaction;

    @BeforeAll
    static void setUpAll() {
        sessionFactory = TestHibernateUtil.getTestSessionFactory();
    }

    @AfterAll
    static void tearDownAll() {
        TestHibernateUtil.close();
    }

    @BeforeEach
    void setUp() {
        userDAO = new UserDAO();
        session = sessionFactory.openSession();
        transaction = session.beginTransaction();

        // Clean up tables before each test
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
    @DisplayName("Сохранение пользователя - успешное создание")
    void testSaveUser() {
        // Given
        User user = new User("Иван Петров", "ivan@example.com", 25);

        // When
        User savedUser = userDAO.save(user);

        // Then
        assertThat(savedUser).isNotNull();
        assertThat(savedUser.getId()).isNotNull();
        assertThat(savedUser.getName()).isEqualTo("Иван Петров");
        assertThat(savedUser.getEmail()).isEqualTo("ivan@example.com");
        assertThat(savedUser.getAge()).isEqualTo(25);
        assertThat(savedUser.getCreatedAt()).isNotNull();
    }

    @Test
    @Order(2)
    @DisplayName("Поиск пользователя по ID - успешное нахождение")
    void testFindById() {
        // Given
        User user = new User("Мария Сидорова", "maria@example.com", 30);
        User savedUser = userDAO.save(user);

        // When
        Optional<User> foundUser = userDAO.findById(savedUser.getId());

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Мария Сидорова");
        assertThat(foundUser.get().getEmail()).isEqualTo("maria@example.com");
        assertThat(foundUser.get().getAge()).isEqualTo(30);
    }

    @Test
    @Order(3)
    @DisplayName("Поиск пользователя по ID - пользователь не найден")
    void testFindByIdNotFound() {
        // When
        Optional<User> foundUser = userDAO.findById(999L);

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(4)
    @DisplayName("Поиск всех пользователей - получение списка")
    void testFindAll() {
        // Given
        User user1 = new User("Алексей Иванов", "alexey@example.com", 28);
        User user2 = new User("Елена Петрова", "elena@example.com", 35);
        userDAO.save(user1);
        userDAO.save(user2);

        // When
        List<User> users = userDAO.findAll();

        // Then
        assertThat(users).hasSize(2);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("Алексей Иванов", "Елена Петрова");
    }

    @Test
    @Order(5)
    @DisplayName("Поиск всех пользователей - пустой список")
    void testFindAllEmpty() {
        // When
        List<User> users = userDAO.findAll();

        // Then
        assertThat(users).isEmpty();
    }

    @Test
    @Order(6)
    @DisplayName("Обновление пользователя - успешное обновление")
    void testUpdateUser() {
        // Given
        User user = new User("Оригинальное имя", "original@example.com", 20);
        User savedUser = userDAO.save(user);

        // When
        savedUser.setName("Обновленное имя");
        savedUser.setAge(25);
        User updatedUser = userDAO.update(savedUser);

        // Then
        assertThat(updatedUser.getName()).isEqualTo("Обновленное имя");
        assertThat(updatedUser.getAge()).isEqualTo(25);
        assertThat(updatedUser.getEmail()).isEqualTo("original@example.com");

        // Verify in database
        Optional<User> verified = userDAO.findById(savedUser.getId());
        assertThat(verified).isPresent();
        assertThat(verified.get().getName()).isEqualTo("Обновленное имя");
    }

    @Test
    @Order(7)
    @DisplayName("Удаление пользователя по ID - успешное удаление")
    void testDeleteById() {
        // Given
        User user = new User("Для удаления", "delete@example.com", 40);
        User savedUser = userDAO.save(user);

        // When
        boolean deleted = userDAO.deleteById(savedUser.getId());

        // Then
        assertThat(deleted).isTrue();
        Optional<User> foundUser = userDAO.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(8)
    @DisplayName("Удаление пользователя по ID - пользователь не найден")
    void testDeleteByIdNotFound() {
        // When
        boolean deleted = userDAO.deleteById(999L);

        // Then
        assertThat(deleted).isFalse();
    }

    @Test
    @Order(9)
    @DisplayName("Удаление пользователя по объекту - успешное удаление")
    void testDeleteByEntity() {
        // Given
        User user = new User("Удаление по объекту", "deleteEntity@example.com", 35);
        User savedUser = userDAO.save(user);

        // When
        userDAO.delete(savedUser);

        // Then
        Optional<User> foundUser = userDAO.findById(savedUser.getId());
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(10)
    @DisplayName("Поиск пользователя по email - успешное нахождение")
    void testFindByEmail() {
        // Given
        User user = new User("Email тест", "uniquetest@example.com", 22);
        userDAO.save(user);

        // When
        Optional<User> foundUser = userDAO.findByEmail("uniquetest@example.com");

        // Then
        assertThat(foundUser).isPresent();
        assertThat(foundUser.get().getName()).isEqualTo("Email тест");
        assertThat(foundUser.get().getEmail()).isEqualTo("uniquetest@example.com");
    }

    @Test
    @Order(11)
    @DisplayName("Поиск пользователя по email - email не найден")
    void testFindByEmailNotFound() {
        // When
        Optional<User> foundUser = userDAO.findByEmail("nonexistent@example.com");

        // Then
        assertThat(foundUser).isEmpty();
    }

    @Test
    @Order(12)
    @DisplayName("Сохранение пользователя с null полями - должно выбросить исключение")
    void testSaveUserWithNullFields() {
        // Given
        User user = new User(null, null, null);

        // When/Then
        assertThatThrownBy(() -> userDAO.save(user))
                .isInstanceOf(RuntimeException.class);
    }

    @Test
    @Order(13)
    @DisplayName("Сохранение пользователя с дублирующим email - должно выбросить исключение")
    void testSaveUserWithDuplicateEmail() {
        // Given
        User user1 = new User("Первый", "duplicate@example.com", 25);
        User user2 = new User("Второй", "duplicate@example.com", 30);
        userDAO.save(user1);

        // When/Then
        assertThatThrownBy(() -> userDAO.save(user2))
                .isInstanceOf(RuntimeException.class);
    }
}
