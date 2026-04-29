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
        logger.info("Starting User Service Console Application");

        Runtime.getRuntime().addShutdownHook(new Thread(() -> {
            logger.info("Shutting down application...");
            HibernateUtil.shutdown();
            scanner.close();
        }));

        showMainMenu();
    }

    private static void showMainMenu() {
        while (true) {
            System.out.println("\n========== USER MANAGEMENT SYSTEM ==========");
            System.out.println("1. Create new user");
            System.out.println("2. Find user by ID");
            System.out.println("3. Show all users");
            System.out.println("4. Update user");
            System.out.println("5. Delete user");
            System.out.println("6. Exit");
            System.out.print("Choose option (1-6): ");

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
                    System.out.println("Goodbye!");
                    logger.info("Application exited by user");
                    return;
                default:
                    System.out.println("Invalid option. Please try again.");
            }
        }
    }

    private static void createUser() {
        System.out.println("\n--- CREATE NEW USER ---");

        System.out.print("Enter name: ");
        String name = scanner.nextLine().trim();

        System.out.print("Enter email: ");
        String email = scanner.nextLine().trim();

        System.out.print("Enter age: ");
        String ageStr = scanner.nextLine().trim();

        try {
            int age = Integer.parseInt(ageStr);
            User user = userService.createUser(name, email, age);
            System.out.println("User created successfully!");
            System.out.println("User details: " + user);
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid age format. Please enter a number.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during user creation", e);
            System.out.println("Unexpected error occurred. Check logs for details.");
        }
    }

    private static void findUserById() {
        System.out.println("\n--- FIND USER BY ID ---");
        System.out.print("Enter user ID: ");
        String idStr = scanner.nextLine().trim();

        try {
            Long id = Long.parseLong(idStr);
            Optional<User> userOpt = userService.getUserById(id);

            if (userOpt.isPresent()) {
                System.out.println("User found:");
                System.out.println("  " + userOpt.get());
            } else {
                System.out.println("User with ID " + id + " not found.");
            }
        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID format. Please enter a number.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during find user", e);
            System.out.println("Unexpected error occurred. Check logs for details.");
        }
    }

    private static void showAllUsers() {
        System.out.println("\n--- ALL USERS ---");

        try {
            List<User> users = userService.getAllUsers();

            if (users.isEmpty()) {
                System.out.println("No users found in the database.");
            } else {
                System.out.println("Found " + users.size() + " user(s):");
                System.out.println("----------------------------------------");
                for (User user : users) {
                    System.out.printf("ID: %-5d | Name: %-20s | Email: %-30s | Age: %d | Created: %s%n",
                            user.getId(), user.getName(), user.getEmail(),
                            user.getAge(), user.getCreatedAt());
                }
                System.out.println("----------------------------------------");
            }
        } catch (Exception e) {
            logger.error("Unexpected error during show all users", e);
            System.out.println("Unexpected error occurred. Check logs for details.");
        }
    }

    private static void updateUser() {
        System.out.println("\n--- UPDATE USER ---");
        System.out.print("Enter user ID to update: ");
        String idStr = scanner.nextLine().trim();

        try {
            Long id = Long.parseLong(idStr);

            // Check if user exists
            Optional<User> existingUser = userService.getUserById(id);
            if (existingUser.isEmpty()) {
                System.out.println("User with ID " + id + " not found.");
                return;
            }

            System.out.println("Current user details:");
            System.out.println("  " + existingUser.get());
            System.out.println("\nLeave field empty to keep current value.");

            System.out.print("Enter new name (current: " + existingUser.get().getName() + "): ");
            String name = scanner.nextLine().trim();
            if (name.isEmpty()) name = null;

            System.out.print("Enter new email (current: " + existingUser.get().getEmail() + "): ");
            String email = scanner.nextLine().trim();
            if (email.isEmpty()) email = null;

            System.out.print("Enter new age (current: " + existingUser.get().getAge() + "): ");
            String ageStr = scanner.nextLine().trim();
            Integer age = null;
            if (!ageStr.isEmpty()) {
                age = Integer.parseInt(ageStr);
            }

            User updatedUser = userService.updateUser(id, name, email, age);
            System.out.println("User updated successfully!");
            System.out.println("Updated details: " + updatedUser);

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID or age format.");
        } catch (IllegalArgumentException | IllegalStateException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during update user", e);
            System.out.println("Unexpected error occurred. Check logs for details.");
        }
    }

    private static void deleteUser() {
        System.out.println("\n--- DELETE USER ---");
        System.out.print("Enter user ID to delete: ");
        String idStr = scanner.nextLine().trim();

        try {
            Long id = Long.parseLong(idStr);

            Optional<User> userOpt = userService.getUserById(id);
            if (userOpt.isEmpty()) {
                System.out.println("User with ID " + id + " not found.");
                return;
            }

            System.out.println("User to delete:");
            System.out.println("  " + userOpt.get());
            System.out.print("Are you sure you want to delete this user? (yes/no): ");
            String confirm = scanner.nextLine().trim().toLowerCase();

            if (confirm.equals("yes") || confirm.equals("y")) {
                boolean deleted = userService.deleteUser(id);
                if (deleted) {
                    System.out.println("User with ID " + id + " has been deleted.");
                } else {
                    System.out.println("Failed to delete user.");
                }
            } else {
                System.out.println("Deletion cancelled.");
            }

        } catch (NumberFormatException e) {
            System.out.println("Error: Invalid ID format.");
        } catch (IllegalArgumentException e) {
            System.out.println("Error: " + e.getMessage());
        } catch (Exception e) {
            logger.error("Unexpected error during delete user", e);
            System.out.println(" Unexpected error occurred. Check logs for details.");
        }
    }
}
