package service;

import dao.UserDAO;
import entity.User;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.CsvSource;
import org.junit.jupiter.params.provider.ValueSource;
import org.mockito.ArgumentCaptor;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import java.time.LocalDateTime;
import java.util.List;
import java.util.Optional;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.*;

@ExtendWith(MockitoExtension.class)
@DisplayName("UserService Unit Tests")
class UserServiceTest {

    @Mock
    private UserDAO userDAO;

    @InjectMocks
    private UserService userService;

    private User testUser;

    @BeforeEach
    void setUp() {
        testUser = new User("Тестовый Пользователь", "test@example.com", 30);
        testUser.setId(1L);
        testUser.setCreatedAt(LocalDateTime.now());
    }

    @Nested
    @DisplayName("Create User Tests - Создание пользователя")
    class CreateUserTests {

        @Test
        @DisplayName("Должен успешно создать пользователя с валидными данными")
        void shouldCreateUserSuccessfully() {
            when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenReturn(testUser);

            User createdUser = userService.createUser("Тестовый Пользователь", "test@example.com", 30);

            assertThat(createdUser).isNotNull();
            assertThat(createdUser.getName()).isEqualTo("Тестовый Пользователь");
            assertThat(createdUser.getEmail()).isEqualTo("test@example.com");
            assertThat(createdUser.getAge()).isEqualTo(30);

            verify(userDAO, times(1)).findByEmail("test@example.com");
            verify(userDAO, times(1)).save(any(User.class));
        }

        @Test
        @DisplayName("Должен выбросить исключение, когда email уже существует")
        void shouldThrowExceptionWhenEmailExists() {
            when(userDAO.findByEmail("existing@example.com")).thenReturn(Optional.of(testUser));

            assertThatThrownBy(() -> userService.createUser("Новый Пользователь", "existing@example.com", 25))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("уже существует");

            verify(userDAO, never()).save(any(User.class));
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t", "\n"})
        @DisplayName("Должен выбросить исключение, когда имя пустое")
        void shouldThrowExceptionWhenNameIsBlank(String invalidName) {
            assertThatThrownBy(() -> userService.createUser(invalidName, "email@example.com", 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Имя не может быть пустым");
        }

        @Test
        @DisplayName("Должен выбросить исключение, когда имя null")
        void shouldThrowExceptionWhenNameIsNull() {
            assertThatThrownBy(() -> userService.createUser(null, "email@example.com", 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Имя не может быть пустым");
        }

        @ParameterizedTest
        @ValueSource(strings = {"", "   ", "\t"})
        @DisplayName("Должен выбросить исключение, когда email пустой")
        void shouldThrowExceptionWhenEmailIsBlank(String invalidEmail) {
            assertThatThrownBy(() -> userService.createUser("Иван", invalidEmail, 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Email не может быть пустым");
        }

        @Test
        @DisplayName("Должен выбросить исключение, когда email null")
        void shouldThrowExceptionWhenEmailIsNull() {
            assertThatThrownBy(() -> userService.createUser("Иван", null, 25))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Email не может быть пустым");
        }

        @ParameterizedTest
        @CsvSource({"-1", "-5", "101", "150", "200", "1000"})
        @DisplayName("Должен выбросить исключение, когда возраст не в диапазоне 0-100")
        void shouldThrowExceptionWhenAgeIsInvalid(int invalidAge) {
            assertThatThrownBy(() -> userService.createUser("Иван", "ivan@example.com", invalidAge))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Возраст должен быть между 0 and 100");
        }

        @Test
        @DisplayName("Должен выбросить исключение, когда возраст null")
        void shouldThrowExceptionWhenAgeIsNull() {
            assertThatThrownBy(() -> userService.createUser("Иван", "ivan@example.com", null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessageContaining("Возраст должен быть между 0 and 100");
        }

        @Test
        @DisplayName("Должен обрезать пробелы в имени и email")
        void shouldTrimWhitespaceFromNameAndEmail() {
            when(userDAO.findByEmail("test@example.com")).thenReturn(Optional.empty());
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userDAO.save(userCaptor.capture())).thenAnswer(invocation -> invocation.getArgument(0));

            userService.createUser("  Иван Петров  ", "  test@example.com  ", 25);

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getName()).isEqualTo("Иван Петров");
            assertThat(capturedUser.getEmail()).isEqualTo("test@example.com");
        }
    }

    @Nested
    @DisplayName("Get User Tests - Получение пользователя")
    class GetUserTests {

        @Test
        @DisplayName("Должен вернуть пользователя при поиске по ID")
        void shouldReturnUserWhenFoundById() {
            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));

            Optional<User> foundUser = userService.getUserById(1L);

            assertThat(foundUser).isPresent();
            assertThat(foundUser.get().getName()).isEqualTo("Тестовый Пользователь");
            verify(userDAO, times(1)).findById(1L);
        }

        @Test
        @DisplayName("Должен вернуть пустой Optional, когда пользователь не найден")
        void shouldReturnEmptyWhenUserNotFoundById() {
            when(userDAO.findById(999L)).thenReturn(Optional.empty());

            Optional<User> foundUser = userService.getUserById(999L);

            assertThat(foundUser).isEmpty();
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -5})
        @DisplayName("Должен выбросить исключение при неверном ID")
        void shouldThrowExceptionWhenIdIsInvalid(Long invalidId) {
            assertThatThrownBy(() -> userService.getUserById(invalidId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Неверный id пользователя");
        }

        @Test
        @DisplayName("Должен вернуть всех пользователей")
        void shouldReturnAllUsers() {
            User user2 = new User("Пользователь 2", "user2@example.com", 40);
            user2.setId(2L);
            List<User> users = List.of(testUser, user2);
            when(userDAO.findAll()).thenReturn(users);

            List<User> allUsers = userService.getAllUsers();

            assertThat(allUsers).hasSize(2);
            assertThat(allUsers).extracting(User::getName)
                    .containsExactlyInAnyOrder("Тестовый Пользователь", "Пользователь 2");
            verify(userDAO, times(1)).findAll();
        }

        @Test
        @DisplayName("Должен вернуть пустой список, когда пользователей нет")
        void shouldReturnEmptyListWhenNoUsers() {
            when(userDAO.findAll()).thenReturn(List.of());

            List<User> allUsers = userService.getAllUsers();

            assertThat(allUsers).isEmpty();
        }
    }

    @Nested
    @DisplayName("Update User Tests - Обновление пользователя")
    class UpdateUserTests {

        @Test
        @DisplayName("Должен успешно обновить пользователя с валидными данными")
        void shouldUpdateUserSuccessfully() {
            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
            when(userDAO.update(any(User.class))).thenReturn(testUser);

            User updatedUser = userService.updateUser(1L, "Обновленное Имя", null, 35);

            assertThat(updatedUser.getName()).isEqualTo("Обновленное Имя");
            assertThat(updatedUser.getAge()).isEqualTo(35);
            assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
        }

        @Test
        @DisplayName("Должен выбросить исключение, когда пользователь не найден для обновления")
        void shouldThrowExceptionWhenUserNotFoundForUpdate() {
            when(userDAO.findById(999L)).thenReturn(Optional.empty());

            assertThatThrownBy(() -> userService.updateUser(999L, "Новое Имя", null, 30))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("не найден");
        }

        @Test
        @DisplayName("Должен выбросить исключение при обновлении на существующий email")
        void shouldThrowExceptionWhenUpdatingToExistingEmail() {
            User existingUser = new User("Существующий", "existing@example.com", 25);
            existingUser.setId(2L);

            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
            when(userDAO.findByEmail("existing@example.com")).thenReturn(Optional.of(existingUser));

            assertThatThrownBy(() -> userService.updateUser(1L, null, "existing@example.com", null))
                    .isInstanceOf(IllegalStateException.class)
                    .hasMessageContaining("уже занят");
        }

        @Test
        @DisplayName("Должен сохранить существующие значения, когда переданы null")
        void shouldKeepExistingValuesWhenNullsProvided() {
            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
            when(userDAO.update(any(User.class))).thenReturn(testUser);

            User updatedUser = userService.updateUser(1L, null, null, null);

            assertThat(updatedUser.getName()).isEqualTo("Тестовый Пользователь");
            assertThat(updatedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(updatedUser.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("Должен обновить только указанные поля")
        void shouldOnlyUpdateProvidedFields() {
            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userDAO.update(userCaptor.capture())).thenReturn(testUser);

            userService.updateUser(1L, "Только Имя", null, null);

            User capturedUser = userCaptor.getValue();
            assertThat(capturedUser.getName()).isEqualTo("Только Имя");
            assertThat(capturedUser.getEmail()).isEqualTo("test@example.com");
            assertThat(capturedUser.getAge()).isEqualTo(30);
        }

        @Test
        @DisplayName("Должен обновить возраст, если передан валидный возраст")
        void shouldUpdateAgeIfValid() {
            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userDAO.update(userCaptor.capture())).thenReturn(testUser);

            userService.updateUser(1L, null, null, 45);

            assertThat(userCaptor.getValue().getAge()).isEqualTo(45);
        }
    }

    @Nested
    @DisplayName("Delete User Tests - Удаление пользователя")
    class DeleteUserTests {

        @Test
        @DisplayName("Должен успешно удалить пользователя")
        void shouldDeleteUserSuccessfully() {
            when(userDAO.deleteById(1L)).thenReturn(true);

            boolean deleted = userService.deleteUser(1L);

            assertThat(deleted).isTrue();
            verify(userDAO, times(1)).deleteById(1L);
        }

        @Test
        @DisplayName("Должен вернуть false, когда пользователь не найден для удаления")
        void shouldReturnFalseWhenUserNotFoundForDeletion() {
            when(userDAO.deleteById(999L)).thenReturn(false);

            boolean deleted = userService.deleteUser(999L);

            assertThat(deleted).isFalse();
        }

        @ParameterizedTest
        @ValueSource(longs = {0, -1, -10})
        @DisplayName("Должен выбросить исключение при удалении с неверным ID")
        void shouldThrowExceptionWhenDeletingWithInvalidId(Long invalidId) {
            assertThatThrownBy(() -> userService.deleteUser(invalidId))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Неверный id пользователя");
        }

        @Test
        @DisplayName("Должен выбросить исключение при удалении с null ID")
        void shouldThrowExceptionWhenDeletingWithNullId() {
            assertThatThrownBy(() -> userService.deleteUser(null))
                    .isInstanceOf(IllegalArgumentException.class)
                    .hasMessage("Неверный id пользователя");
        }
    }

    @Nested
    @DisplayName("Edge Cases Tests - Граничные случаи")
    class EdgeCasesTests {

        @Test
        @DisplayName("Должен обрабатывать очень длинные имена")
        void shouldHandleVeryLongNames() {
            String longName = "А".repeat(255);
            when(userDAO.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User createdUser = userService.createUser(longName, "long@example.com", 25);

            assertThat(createdUser.getName()).isEqualTo(longName);
        }

        @Test
        @DisplayName("Должен обрабатывать граничные значения возраста (0 и 100)")
        void shouldHandleAgeBoundaries() {
            when(userDAO.findByEmail(anyString())).thenReturn(Optional.empty());
            when(userDAO.save(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User userMinAge = userService.createUser("Мин возраст", "min@example.com", 0);
            User userMaxAge = userService.createUser("Макс возраст", "max@example.com", 100);

            assertThat(userMinAge.getAge()).isEqualTo(0);
            assertThat(userMaxAge.getAge()).isEqualTo(100);
        }

        @Test
        @DisplayName("Должен сохранять оригинальную дату создания при обновлении")
        void shouldPreserveCreatedAtOnUpdate() {
            LocalDateTime originalCreatedAt = LocalDateTime.now().minusDays(5);
            testUser.setCreatedAt(originalCreatedAt);

            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
            when(userDAO.update(any(User.class))).thenAnswer(invocation -> invocation.getArgument(0));

            User updatedUser = userService.updateUser(1L, "Обновленный", null, 40);

            assertThat(updatedUser.getCreatedAt()).isEqualTo(originalCreatedAt);
        }

        @Test
        @DisplayName("Должен нормализовать email (обрезать пробелы) при обновлении")
        void shouldTrimEmailOnUpdate() {
            when(userDAO.findById(1L)).thenReturn(Optional.of(testUser));
            when(userDAO.findByEmail("trimmed@example.com")).thenReturn(Optional.empty());
            ArgumentCaptor<User> userCaptor = ArgumentCaptor.forClass(User.class);
            when(userDAO.update(userCaptor.capture())).thenReturn(testUser);

            userService.updateUser(1L, null, "  trimmed@example.com  ", null);

            assertThat(userCaptor.getValue().getEmail()).isEqualTo("trimmed@example.com");
        }
    }
}
