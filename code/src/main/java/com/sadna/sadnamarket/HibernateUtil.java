package com.sadna.sadnamarket;

import com.sadna.sadnamarket.domain.stores.Store;
import com.sadna.sadnamarket.domain.stores.StoreDTO;
import com.sadna.sadnamarket.service.Error;
import org.hibernate.Session;
import org.hibernate.SessionFactory;
import org.hibernate.Transaction;
import org.hibernate.cfg.Configuration;

import io.github.cdimascio.dotenv.Dotenv;
import org.hibernate.query.Query;

import java.util.List;

public class HibernateUtil {
    private static final SessionFactory sessionFactory = buildSessionFactory();

    private static SessionFactory buildSessionFactory() {
        try {
            // Create the SessionFactory from hibernate.cfg.xml
            Dotenv dotenv = Dotenv.load();
            Configuration configuration = new Configuration().configure();
            configuration.setProperty("hibernate.connection.url", Config.DB_URL);
            configuration.setProperty("hibernate.connection.username", dotenv.get("DB_USERNAME"));
            configuration.setProperty("hibernate.connection.password", dotenv.get("DB_PASSWORD"));
            return configuration.buildSessionFactory();
        } catch (Throwable ex) {
            // Make sure you log the exception, as it might be swallowed
            System.err.println("Initial SessionFactory creation failed." + ex);
            throw new ExceptionInInitializerError(ex);
        }
    }

    public static SessionFactory getSessionFactory() {
        return sessionFactory;
    }

    public static void shutdown() {
        // Close caches and connection pools
        getSessionFactory().close();
    }

    /*public static void cleanDB() {
        List<String> tableNames = List.of("stores", "store_products", "store_owners", "store_managers", "store_orders");
        Session session = sessionFactory.openSession();
        try {
            session.beginTransaction();
            for(String tableName : tableNames) {
                String queryStr = String.format("DELETE FROM %s;", tableName);
                Query query = session.createNativeQuery(queryStr);
                query.executeUpdate();
            }
            session.getTransaction().commit();
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
        finally {
            session.close();
        }
    }*/


}
