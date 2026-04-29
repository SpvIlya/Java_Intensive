package dao;

import com.example.userservice.entity.User;
import com.example.userservice.util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;

import javax.persistence.NoResultException;
import javax.persistence.criteria.CriteriaBuilder;
import javax.persistence.criteria.CriteriaQuery;
import javax.persistence.criteria.Root;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private static final Logger logger = LogManager.getLogger(UserDAO.class);

    // Create
    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.info("User saved successfully: {}", user);
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error saving user: {}", e.getMessage());
            throw new RuntimeException("Failed to save user", e);
        }
    }

    // Read by ID
    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            logger.debug("Finding user by id {}: {}", id, user);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Error finding user by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to find user", e);
        }
    }

    // Read all
    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cr = cb.createQuery(User.class);
            Root<User> root = cr.from(User.class);
            cr.select(root);

            Query<User> query = session.createQuery(cr);
            List<User> users = query.getResultList();
            logger.info("Found {} users", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Error finding all users: {}", e.getMessage());
            throw new RuntimeException("Failed to find all users", e);
        }
    }

    // Update
    public User update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User mergedUser = session.merge(user);
            transaction.commit();
            logger.info("User updated successfully: {}", mergedUser);
            return mergedUser;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error updating user: {}", e.getMessage());
            throw new RuntimeException("Failed to update user", e);
        }
    }

    // Delete by entity
    public void delete(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(user) ? user : session.merge(user));
            transaction.commit();
            logger.info("User deleted successfully: {}", user);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user: {}", e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    // Delete by ID
    public boolean deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                transaction.commit();
                logger.info("User with id {} deleted successfully", id);
                return true;
            }
            transaction.commit();
            logger.warn("User with id {} not found for deletion", id);
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Error deleting user by id {}: {}", id, e.getMessage());
            throw new RuntimeException("Failed to delete user", e);
        }
    }

    // Find by email (unique constraint)
    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cr = cb.createQuery(User.class);
            Root<User> root = cr.from(User.class);
            cr.select(root).where(cb.equal(root.get("email"), email));

            Query<User> query = session.createQuery(cr);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            logger.debug("No user found with email: {}", email);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Error finding user by email: {}", e.getMessage());
            throw new RuntimeException("Failed to find user by email", e);
        }
    }
}
