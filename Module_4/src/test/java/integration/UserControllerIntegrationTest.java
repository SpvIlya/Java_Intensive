package integration;

import app.UserServiceApplication;
import app.dto.CreateUserRequest;
import app.dto.UpdateUserRequest;
import app.entity.User;
import app.repository.UserRepository;
import com.fasterxml.jackson.databind.ObjectMapper;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.autoconfigure.web.servlet.AutoConfigureMockMvc;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.http.MediaType;
import org.springframework.test.web.servlet.MockMvc;

import static org.springframework.test.web.servlet.request.MockMvcRequestBuilders.*;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.jsonPath;
import static org.springframework.test.web.servlet.result.MockMvcResultMatchers.status;

@SpringBootTest(classes = UserServiceApplication.class)
@AutoConfigureMockMvc
@DisplayName("Интеграционные тесты User API")
class UserControllerIntegrationTest {

    @Autowired
    private MockMvc mockMvc;

    @Autowired
    private UserRepository userRepository;

    @Autowired
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        userRepository.deleteAll();
    }

    @Test
    void createUser_ShouldReturnCreatedUser() throws Exception {
        CreateUserRequest request = new CreateUserRequest();
        request.setName("Иван Петров");
        request.setEmail("ivan@example.com");
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isCreated())
                .andExpect(jsonPath("$.name").value("Иван Петров"))
                .andExpect(jsonPath("$.email").value("ivan@example.com"))
                .andExpect(jsonPath("$.age").value(25));
    }

    @Test
    void getAllUsers_ShouldReturnListOfUsers() throws Exception {
        User user = new User("Мария Сидорова", "maria@example.com", 30);
        userRepository.save(user);

        mockMvc.perform(get("/api/users"))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$[0].name").value("Мария Сидорова"));
    }

    @Test
    void getUserById_ShouldReturnUser() throws Exception {
        User user = new User("Алексей Смирнов", "alexey@example.com", 40);
        User savedUser = userRepository.save(user);

        mockMvc.perform(get("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Алексей Смирнов"));
    }

    @Test
    void updateUser_ShouldReturnUpdatedUser() throws Exception {
        User user = new User("Старое Имя", "old@example.com", 20);
        User savedUser = userRepository.save(user);

        UpdateUserRequest updateRequest = new UpdateUserRequest();
        updateRequest.setName("Новое Имя");
        updateRequest.setEmail("new@example.com");
        updateRequest.setAge(25);

        mockMvc.perform(put("/api/users/{id}", savedUser.getId())
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(updateRequest)))
                .andExpect(status().isOk())
                .andExpect(jsonPath("$.name").value("Новое Имя"))
                .andExpect(jsonPath("$.email").value("new@example.com"));
    }

    @Test
    void deleteUser_ShouldReturnNoContent() throws Exception {
        User user = new User("Для Удаления", "delete@example.com", 99);
        User savedUser = userRepository.save(user);

        mockMvc.perform(delete("/api/users/{id}", savedUser.getId()))
                .andExpect(status().isNoContent());
    }

    @Test
    void createUser_WithDuplicateEmail_ShouldReturnError() throws Exception {
        User existingUser = new User("Существующий", "duplicate@example.com", 30);
        userRepository.save(existingUser);

        CreateUserRequest request = new CreateUserRequest();
        request.setName("Новый Пользователь");
        request.setEmail("duplicate@example.com");
        request.setAge(25);

        mockMvc.perform(post("/api/users")
                        .contentType(MediaType.APPLICATION_JSON)
                        .content(objectMapper.writeValueAsString(request)))
                .andExpect(status().isBadRequest())
                .andExpect(jsonPath("$.ошибка").value("Пользователь с email duplicate@example.com уже существует"));
    }
}
