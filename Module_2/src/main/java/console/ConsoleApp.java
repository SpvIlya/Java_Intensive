package console;

import entity.User;
import service.UserService;
import util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.List;
import java.util.Optional;
import java.util.Scanner;

public class ConsoleApp {

    private static final Logger logger = LogManager.getLogger(ConsoleApp.class);
    private static final UserService userService = new UserService();
    private static final Scanner scanner = new Scanner(System.in);

    static void main(String[] args) {
        logger.info("Запуск консольного приложения обслуживания пользователей");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Закрытие приложения...");
            HibernateUtil.shutdown();
            scanner.close();
        }));

        showMainMenu();
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n========== СИСТЕМА УПРАВЛЕНИЯ ПОЛЬЗОВАТЕЛЯМИ ==========");
            System.out.println("1. Создать нового пользователя");
            System.out.println("2. Найти пользователя по ID");
            System.out.println("3. Показать всех пользователей");
            System.out.println("4. Обновить пользователя");
            System.out.println("5. Удалить пользователя");
            System.out.println("6. Выход");
            System.out.print("Выберите действие (1-6): ");

            String choice = scanner.nextLine().trim();

            switch (choice) {
                case "1":
                    createUser();
                    break;
                case "2":
                    findUserById();
                    break;
                case "3":
                    showAllUsers();
                    break;
                case "4":
                    updateUser();
                    break;
                case "5":
                    deleteUser();
                    break;
                case "6":
                    System.out.println("До свидания!");
                    logger.info("Приложение закрыто пользователем");
                    return;
                default:
                    System.out.println("Неверное действие. Пожалуйста, попробуйте еще раз.");
            }
        }
    }

    private static void createUser() {
        System.out.println("\n--- Создать нового пользователя ---");

        System.out.print("Введите имя: ");
        String name = scanner.nextLine().trim();

        System.out.print("Введите email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Введите возраст: ");
        String ageStr = scanner.nextLine().trim();

        try {
            int age = Integer.parseInt(ageStr);
            User user = userService.createUser(name, email, age);
            System.out.println("Пользователь успешно создан!");
            System.out.println("Сведения о пользователе: " + user);
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: недопустимый формат возраста. Пожалуйста, введите цифру.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка, пожалуйста, проверьте данные.", e);
            System.out.println("Непредвиденная ошибка, пожалуйста, проверьте данные.");
        }
    }

    private static void findUserById() {
        System.out.println("\n--- Найти пользователя по ID ---");
        System.out.print("Введите ID пользователя: ");
        String idStr = scanner.nextLine().trim();

        try {
            Long id = Long.parseLong(idStr);
            Optional<User> userOpt = userService.getUserById(id);

            if (userOpt.isPresent()) {
                System.out.println("Пользователь найден:");
                System.out.println("  " + userOpt.get());
            } else {
                System.out.println("Пользователь с ID " + id + " не найден.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Ошибка: недопустимый формат ID. Пожалуйста, введите цифру.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка при поиске пользователя.", e);
            System.out.println("Непредвиденная ошибка при поиске пользователя.");
        }
    }

    private static void showAllUsers() {
        System.out.println("\n--- Все пользователи ---");

        try {
            List<User> users = userService.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("В базе данных не найдено пользователей.");
            } else {
                System.out.println("Найденные " + users.size() + " пользователи:");
                System.out.println("----------------------------------------");
                for (User user : users) {
                    System.out.printf("ID: %-5d | Имя: %-20s | Email: %-30s | Возраст: %d | Создан: %s%n",
                            user.getId(), user.getName(), user.getEmail(),
                            user.getAge(), user.getCreatedAt());
                }
                System.out.println("----------------------------------------");
            }
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка во время просмотра всех пользователей.", e);
            System.out.println("Непредвиденная ошибка во время просмотра всех пользователей.");
        }
    }

    private static void updateUser() {
        System.out.println("\n--- Обновить пользователя ---");
        System.out.print("Введите ID пользователя для обновления: ");
        String idStr = scanner.nextLine().trim();

        try {
            Long id = Long.parseLong(idStr);

            // Check if user exists
            Optional<User> existingUser = userService.getUserById(id);
            if (existingUser.isEmpty()) {
                System.out.println("Пользователь с ID " + id + " не найден.");
                return;
            }

            System.out.println("Текущие данные пользователя:");
            System.out.println("  " + existingUser.get());
            System.out.println("\nОставьте поле пустым, чтобы сохранить текущее значение.");

            System.out.print("Введите новое имя (текущий: " + existingUser.get().getName() + "): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = null;

            System.out.print("Введите новый email (текущий: " + existingUser.get().getEmail() + "): ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = null;

            System.out.print("Введите новый возраст (текущий: " + existingUser.get().getAge() + "): ");
            String ageStr = scanner.nextLine().trim();
            Integer age = null;
            if (!ageStr.isEmpty()) {
                age = Integer.parseInt(ageStr);
            }

            User updatedUser = userService.updateUser(id, name, email, age);
            System.out.println("Пользователь успешно обновлен!");
            System.out.println("Внесенный изменения: " + updatedUser);

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: недопустимый формат ID или возраста. Пожалуйста, введите цифру.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка во время обновления пользователя.", e);
            System.out.println("Непредвиденная ошибка во время обновления пользователя.");
        }
    }

    private static void deleteUser() {
        System.out.println("\n--- Удалить пользователя ---");
        System.out.print("Введите ID пользователя для удаления: ");
        String idStr = scanner.nextLine().trim();

        try {
            Long id = Long.parseLong(idStr);

            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                System.out.println("Пользователь с ID " + id + " не найден.");
                return;
            }

            System.out.println("Пользователь для удаления:");
            System.out.println("  " + userOpt.get());
            System.out.print("Вы уверенны, что хотите удалить данного пользователя? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("yes") || confirm.equals("y")) {
                boolean deleted = userService.deleteUser(id);
                if (deleted) {
                    System.out.println("Пользователь с ID " + id + " был удален.");
                } else {
                    System.out.println("Не удалось удалить пользователя.");
                }
            } else {
                System.out.println("Удаление отменено.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Ошибка: недопустимый формат ID. Пожалуйста, введите цифру.");
        } catch (IllegalArgumentException e) {
            System.out.println("Ошибка: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Непредвиденная ошибка во время удаления пользователя.", e);
            System.out.println("Непредвиденная ошибка во время удаления пользователя.");
        }
    }
}
