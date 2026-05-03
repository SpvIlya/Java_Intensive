package service;

import dao.UserDAO;
import entity.User;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;

public class UserService {

    private static final Logger logger = LogManager.getLogger(UserService.class);
    private final UserDAO userDAO;

    public UserService() {
        this.userDAO = new UserDAO();
    }

    public User createUser(String name, String email, Integer age) {
        logger.info("Создание пользователя: имя={}, email={}, возраст={}", name, email, age);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Имя не может быть пустым");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email не может быть пустым");
        }
        if (age == null || age < 0 || age > 100) {
            throw new IllegalArgumentException("Возраст должен быть между 0 and 100");
        }

        Optional<User> existingUser = userDAO.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalStateException("Пользователь с email " + email + " уже существует");
        }

        User user = new User(name.trim(), email.trim(), age);
        return userDAO.save(user);
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Получение пользователя по id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Неверный id пользователя");
        }
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() {
        logger.info("Получение всех пользователей");
        return userDAO.findAll();
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        logger.info("Обновления пользователя по id: {}", id);

        Optional<User> existingUserOpt = userDAO.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalStateException("Пользователь с id " + id + " не найден");
        }

        User user = existingUserOpt.get();

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            Optional<User> userWithEmail = userDAO.findByEmail(email.trim());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                throw new IllegalStateException("Email " + email + " уже занят");
            }
            user.setEmail(email.trim());
        }
        if (age != null && age >= 0 && age <= 100) {
            user.setAge(age);
        }

        return userDAO.update(user);
    }

    public boolean deleteUser(Long id) {
        logger.info("Удаление пользователя по id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Неверный id пользователя");
        }
        return userDAO.deleteById(id);
    }
}
