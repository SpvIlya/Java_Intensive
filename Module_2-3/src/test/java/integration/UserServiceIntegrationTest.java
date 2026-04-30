package integration;

import entity.User;
import org.junit.jupiter.api.*;
import service.UserService;
import util.TestHibernateUtil;

import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;

@TestMethodOrder(MethodOrderer.OrderAnnotation.class)
class UserServiceIntegrationTest {

    private static UserService userService;

    @BeforeAll
    static void setUpAll() {
        TestHibernateUtil.getTestSessionFactory();
        userService = new UserService();
    }

    @AfterAll
    static void tearDownAll() {
        TestHibernateUtil.close();
    }

    @BeforeEach
    void setUp() {
        userService.getAllUsers().forEach(user -> userService.deleteUser(user.getId()));
    }

    @Test
    @Order(1)
    @DisplayName("Полный жизненный цикл пользователя")
    void testCompleteUserLifecycle() {
        User created = userService.createUser("Тестовый Пользователь", "test@example.com", 25);
        assertThat(created.getId()).isNotNull();
        assertThat(created.getName()).isEqualTo("Тестовый Пользователь");

        Optional<User> found = userService.getUserById(created.getId());
        assertThat(found).isPresent();
        assertThat(found.get().getEmail()).isEqualTo("test@example.com");

        User updated = userService.updateUser(created.getId(), "Обновленное Имя", null, 30);
        assertThat(updated.getName()).isEqualTo("Обновленное Имя");
        assertThat(updated.getAge()).isEqualTo(30);

        boolean deleted = userService.deleteUser(created.getId());
        assertThat(deleted).isTrue();

        Optional<User> afterDelete = userService.getUserById(created.getId());
        assertThat(afterDelete).isEmpty();
    }

    @Test
    @Order(2)
    @DisplayName("Проверка уникальности email")
    void testEmailUniqueness() {
        userService.createUser("Первый", "unique@example.com", 20);

        assertThatThrownBy(() -> userService.createUser("Второй", "unique@example.com", 25))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже существует");
    }

    @Test
    @Order(3)
    @DisplayName("Получение всех пользователей")
    void testGetAllUsers() {
        userService.createUser("Пользователь 1", "user1@example.com", 20);
        userService.createUser("Пользователь 2", "user2@example.com", 25);
        userService.createUser("Пользователь 3", "user3@example.com", 30);

        List<User> users = userService.getAllUsers();

        assertThat(users).hasSize(3);
        assertThat(users).extracting(User::getName)
                .containsExactlyInAnyOrder("Пользователь 1", "Пользователь 2", "Пользователь 3");
    }

    @Test
    @Order(4)
    @DisplayName("Обновление email на существующий - должно выбросить исключение")
    void testUpdateToExistingEmail() {
        User user1 = userService.createUser("Первый", "first@example.com", 20);
        User user2 = userService.createUser("Второй", "second@example.com", 25);

        assertThatThrownBy(() -> userService.updateUser(user2.getId(), null, "first@example.com", null))
                .isInstanceOf(IllegalStateException.class)
                .hasMessageContaining("уже занят");
    }

    @Test
    @Order(5)
    @DisplayName("Обновление email на свой же email - должно работать")
    void testUpdateToSameEmail() {
        User user = userService.createUser("Тест", "test@example.com", 20);

        User updated = userService.updateUser(user.getId(), null, "test@example.com", null);

        assertThat(updated.getEmail()).isEqualTo("test@example.com");
    }
}
