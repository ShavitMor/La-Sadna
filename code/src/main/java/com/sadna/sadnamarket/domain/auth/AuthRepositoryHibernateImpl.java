package com.sadna.sadnamarket.domain.auth;

import java.util.HashMap;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.stream.Collectors;

import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.service.Error;
import jakarta.persistence.QueryHint;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.security.crypto.bcrypt.BCrypt;

import javax.persistence.PersistenceException;

public class AuthRepositoryHibernateImpl implements IAuthRepository {

    private static final Logger logger = LogManager.getLogger(AuthRepositoryHibernateImpl.class);

    @Override
    public void login(String username, String password) {
        logger.info("start-Login. username: {} ", username);
        if (!hasMember(username)) {
            logger.info("user doesn't exist");
            throw new NoSuchElementException(Error.makeAuthUserDoesntExistError());
        }
        if (!isPasswordCorrect(username, password)) {
            logger.info("password incorrect");
            throw new IllegalArgumentException(Error.makeAuthPasswordIncorrectError());
        }
        logger.info("end-Login.");
    }

    private boolean isPasswordCorrect(String username, String password) {
        logger.info("start-isPasswordCorrect. username: {} ", username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserCredential userCredential = session.get(UserCredential.class, username);
            boolean res = PasswordHash.verifyPassword(password, userCredential.getPassword());
            logger.info("end-isPasswordCorrect. returnedValue:{}", res);
            return res;
        }
    }

    private synchronized boolean hasMember(String username) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            UserCredential userCredential = session.get(UserCredential.class, username);
            return userCredential != null;
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public HashMap<String, String> getAll() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<UserCredential> userList = session.createQuery("from UserCredential", UserCredential.class).list();
            return (HashMap<String, String>) userList.stream()
                    .collect(Collectors.toMap(UserCredential::getUsername, UserCredential::getPassword));
        }catch (PersistenceException e){
            logger.error("Error in getAll {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void add(String username, String password) {
        logger.info("start-add. username: {} ", username);
        if (hasMember(username)) {
            throw new IllegalArgumentException(Error.makeAuthUsernameExistsError());
        }
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            UserCredential userCredential = new UserCredential();
            userCredential.setUsername(username);
            userCredential.setPassword(PasswordHash.hashPassword(password));
            session.save(userCredential);
            transaction.commit();
            logger.info("end-add. username: {} ", username);
        }catch (PersistenceException e){
            logger.error("Error in add {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

    @Override
    public void delete(String username) {
        logger.info("start-delete. username: {} ", username);
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            UserCredential userCredential = session.get(UserCredential.class, username);
            if (userCredential != null) {
                session.delete(userCredential);
            }
            transaction.commit();
            logger.info("end-delete {}", username);
        }catch (PersistenceException e){
            logger.error("Error in delete {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }

   

    @Override
    public void clear() {
        logger.info("start-clear");
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            String hql = "DELETE FROM UserCredential";
            session.createQuery(hql).executeUpdate();
            transaction.commit();
            logger.info("end-clear");
        }
        catch (PersistenceException e){
            logger.error("Error in clear {}", e.getMessage());
            throw new IllegalStateException(Error.makeDBError());
        }
    }
}
