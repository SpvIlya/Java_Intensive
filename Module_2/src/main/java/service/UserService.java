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
        logger.info("Creating user: name={}, email={}, age={}", name, email, age);

        if (name == null || name.trim().isEmpty()) {
            throw new IllegalArgumentException("Name cannot be empty");
        }
        if (email == null || email.trim().isEmpty()) {
            throw new IllegalArgumentException("Email cannot be empty");
        }
        if (age == null || age < 0 || age > 150) {
            throw new IllegalArgumentException("Age must be between 0 and 150");
        }

        Optional<User> existingUser = userDAO.findByEmail(email);
        if (existingUser.isPresent()) {
            throw new IllegalStateException("User with email " + email + " already exists");
        }

        User user = new User(name.trim(), email.trim(), age);
        return userDAO.save(user);
    }

    public Optional<User> getUserById(Long id) {
        logger.info("Getting user by id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }
        return userDAO.findById(id);
    }

    public List<User> getAllUsers() {
        logger.info("Getting all users");
        return userDAO.findAll();
    }

    public User updateUser(Long id, String name, String email, Integer age) {
        logger.info("Updating user id: {}", id);

        Optional<User> existingUserOpt = userDAO.findById(id);
        if (existingUserOpt.isEmpty()) {
            throw new IllegalStateException("User with id " + id + " not found");
        }

        User user = existingUserOpt.get();

        if (name != null && !name.trim().isEmpty()) {
            user.setName(name.trim());
        }
        if (email != null && !email.trim().isEmpty()) {
            Optional<User> userWithEmail = userDAO.findByEmail(email.trim());
            if (userWithEmail.isPresent() && !userWithEmail.get().getId().equals(id)) {
                throw new IllegalStateException("Email " + email + " is already taken");
            }
            user.setEmail(email.trim());
        }
        if (age != null && age >= 0 && age <= 150) {
            user.setAge(age);
        }

        return userDAO.update(user);
    }

    public boolean deleteUser(Long id) {
        logger.info("Deleting user id: {}", id);
        if (id == null || id <= 0) {
            throw new IllegalArgumentException("Invalid user id");
        }
        return userDAO.deleteById(id);
    }
}
