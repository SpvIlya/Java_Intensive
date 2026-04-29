package dao;

import entity.User;
import jakarta.persistence.*;
import jakarta.persistence.criteria.CriteriaBuilder;
import jakarta.persistence.criteria.CriteriaQuery;
import jakarta.persistence.criteria.Root;
import util.HibernateUtil;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.query.Query;
import java.util.List;
import java.util.Optional;

public class UserDAO {

    private static final Logger logger = LogManager.getLogger(UserDAO.class);

    public User save(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.persist(user);
            transaction.commit();
            logger.info("Пользователь успешно сохранен: {}", user);
            return user;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка сохранения пользователя: {}", e.getMessage());
            throw new RuntimeException("Не удалось сохранить пользователя", e);
        }
    }

    public Optional<User> findById(Long id) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            User user = session.get(User.class, id);
            logger.debug("Поиск пользователя по id {}: {}", id, user);
            return Optional.ofNullable(user);
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по id {}: {}", id, e.getMessage());
            throw new RuntimeException("Не удалось найти пользователя", e);
        }
    }

    public List<User> findAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cr = cb.createQuery(User.class);
            Root<User> root = cr.from(User.class);
            cr.select(root);

            Query<User> query = session.createQuery(cr);
            List<User> users = query.getResultList();
            logger.info("Найденные {} пользователи", users.size());
            return users;
        } catch (Exception e) {
            logger.error("Ошибка поиска всех пользователей: {}", e.getMessage());
            throw new RuntimeException("Не удалось найти всех пользователей", e);
        }
    }

    public User update(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User mergedUser = session.merge(user);
            transaction.commit();
            logger.info("Пользователь успешно обновлен: {}", mergedUser);
            return mergedUser;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка обновления пользователя: {}", e.getMessage());
            throw new RuntimeException("Не удалось обновить пользователя", e);
        }
    }

    public void delete(User user) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            session.remove(session.contains(user) ? user : session.merge(user));
            transaction.commit();
            logger.info("Пользователь успешно удален: {}", user);
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка удаления пользователя: {}", e.getMessage());
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }

    public boolean deleteById(Long id) {
        Transaction transaction = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            User user = session.get(User.class, id);
            if (user != null) {
                session.remove(user);
                transaction.commit();
                logger.info("Пользователь с id {} успешно удален", id);
                return true;
            }
            transaction.commit();
            logger.warn("Пользователь с id {} не найден для удаления", id);
            return false;
        } catch (Exception e) {
            if (transaction != null) {
                transaction.rollback();
            }
            logger.error("Ошибка удаления пользователя по id {}: {}", id, e.getMessage());
            throw new RuntimeException("Не удалось удалить пользователя", e);
        }
    }

    public Optional<User> findByEmail(String email) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            CriteriaBuilder cb = session.getCriteriaBuilder();
            CriteriaQuery<User> cr = cb.createQuery(User.class);
            Root<User> root = cr.from(User.class);
            cr.select(root).where(cb.equal(root.get("email"), email));

            Query<User> query = session.createQuery(cr);
            return Optional.ofNullable(query.getSingleResult());
        } catch (NoResultException e) {
            logger.debug("Пользователь с email не найден: {}", email);
            return Optional.empty();
        } catch (Exception e) {
            logger.error("Ошибка поиска пользователя по email: {}", e.getMessage());
            throw new RuntimeException("Не удалось найти пользователя по email", e);
        }
    }
}
