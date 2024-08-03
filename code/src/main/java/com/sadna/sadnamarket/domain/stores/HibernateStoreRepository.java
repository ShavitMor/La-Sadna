package com.sadna.sadnamarket.domain.stores;

import java.util.*;
import java.util.concurrent.TimeoutException;

import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.UserFacade;
import com.sadna.sadnamarket.service.Error;
import jakarta.persistence.QueryHint;
import org.hibernate.HibernateException;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.TransactionException;
import org.hibernate.exception.JDBCConnectionException;
import org.hibernate.query.Query;
import org.springframework.data.relational.core.sql.In;

import javax.persistence.LockTimeoutException;
import javax.persistence.NoResultException;
import org.springframework.data.jpa.repository.QueryHints;

import javax.persistence.PersistenceException;
import javax.transaction.Transactional;

public class HibernateStoreRepository implements IStoreRepository{

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Store findStoreByID(int storeId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            return store;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Set<Integer> getAllStoreIds() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> res = session.createQuery( "select s.storeId from Store s" ).list();
            return new HashSet<>(res);
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    private void verify(String storeName, String address, String email, String phoneNumber) {
        if(storeName == null || storeName.trim().equals(""))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(storeName, "store name"));
        if(address == null || address.trim().equals(""))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(address, "address"));
        if(email == null || !email.matches("^[a-zA-Z0-9_+&*-]+(?:\\.[a-zA-Z0-9_+&*-]+)*@(?:[a-zA-Z0-9-]+\\.)+[a-zA-Z]{2,7}$"))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(email, "email address"));
        if(phoneNumber == null || phoneNumber.matches("^\\d{9}$"))
            throw new IllegalArgumentException(Error.makeStoreNotValidAspectError(phoneNumber, "phone number"));
    }

    @Override
    public int addStore(String founderUsername, String storeName, String address, String email, String phoneNumber) {
        verify(storeName, address, email, phoneNumber);

        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();

            try{
                //check if store exists
                Store storeWithName = session.createQuery("SELECT s FROM Store s where s.storeInfo.storeName = :name", Store.class).setParameter("name", storeName).getSingleResult();
                throw new IllegalArgumentException(Error.makeStoreWithNameAlreadyExistsError(storeName));
            }
            catch (NoResultException e) {
                Store createdStore = new Store(founderUsername, new StoreInfo(storeName, address, email, phoneNumber));
                session.save(createdStore);
                session.getTransaction().commit();
                return createdStore.getStoreId();
            }
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    public boolean storeExists(int storeId) {
        try {
            findStoreByID(storeId);
            return true;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            return false;
        }
    }

    /*@Override
    public void saveStore(Store store) {
        Session session = HibernateUtil.getSessionFactory().openSession();
        try {
            session.beginTransaction();
            session.merge(store);
            session.getTransaction().commit();
        }
        catch (Exception e) {
            session.getTransaction().rollback();
        }
        finally {
            session.close();
        }
    }*/

    @Override
    public void addProductToStore(int storeId, int productId, int amount) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            if (store.getProductAmounts().containsKey(productId)) {
                throw new IllegalArgumentException(Error.makeStoreProductAlreadyExistsError(productId));
            }
            store.addProduct(productId, amount);
            session.merge(store);
            session.getTransaction().commit();
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    public boolean productExists(int storeId, int productId) {
        try {
            getProductAmountInStore(storeId, productId);
            return true;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch(Exception e) {
            if (e.getMessage().equals(Error.makeStoreProductDoesntExistError(storeId, productId))) {
                return false;
            }
            throw e;
        }
    }

    @Override
    public boolean hasProductInStock(int storeId, int productId, int amount) {
        int amountInStore;
        try {
            amountInStore = getProductAmountInStore(storeId, productId);
            return amountInStore >= amount;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            return false;
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public int getProductAmountInStore(int storeId, int productId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()){
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            if (!store.getProductAmounts().containsKey(productId)) {
                throw new IllegalArgumentException(Error.makeStoreProductDoesntExistError(storeId, productId));
            }
            return store.getProductAmounts().get(productId);
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void deleteProductFromStore(int storeId, int productId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            if (!store.getProductAmounts().containsKey(productId)) {
                throw new IllegalArgumentException(Error.makeStoreProductDoesntExistError(storeId, productId));
            }
            store.deleteProduct(productId);
            //String deleteQuery = "DELETE FROM store_products WHERE store_id = :storeId AND product_id = :productId";
            //Query query = session.createNativeQuery(deleteQuery).setParameter("storeId", storeId).setParameter("productId", productId);
            //query.executeUpdate();
            session.update(store);
            session.getTransaction().commit();
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    public void updateProductAmountInStore(int storeId, int productId, int newAmount) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            if (!store.getProductAmounts().containsKey(productId)) {
                throw new IllegalArgumentException(Error.makeStoreProductDoesntExistError(storeId, productId));
            }

            store.setProductAmounts(productId, newAmount);
            session.update(store);
            session.getTransaction().commit();
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public StoreDTO getStoreDTO(int storeId) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            // for lazy loading
            for(int productId : store.getProductAmounts().keySet()) {
                store.getProductAmounts().get(productId);
            }
            return store.getStoreDTO();
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Set<String> checkCartInStore(int storeId, List<CartItemDTO> cart) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            return store.checkCart(cart);
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public Set<String> updateStockInStore(int storeId, List<CartItemDTO> cart) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            Set<String> errors = store.updateStock(cart);
            session.update(store);
            session.getTransaction().commit();
            return errors;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Store findStoreByName(String storeName) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Store res = session.createQuery("SELECT s FROM Store s where s.storeInfo.storeName = :name", Store.class).setParameter("name", storeName).getSingleResult();
            return res;
        }
        catch (NoResultException e) {
            throw new IllegalArgumentException(Error.makeNoStoreWithNameError(storeName));
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void setStoreBankAccount(int storeId, BankAccountDTO bankAccountDTO) {
        Session session = null;
        try{
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            store.setBankAccount(bankAccountDTO);
            //session.merge(store);
            session.saveOrUpdate(store.getBankAccount());
            session.getTransaction().commit();
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
        finally {
            session.close();
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public BankAccountDTO getStoreBankAccount(int storeId) {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            //StoreDTO res = session.createQuery("select s from StoreDTO s where s.storeName = :name", StoreDTO.class).getSingleResult();
            //return new Store(res);
            BankAccountDTO bankAccountDTO = session.get(BankAccountDTO.class, storeId);
            return bankAccountDTO;
        }
        catch (NoResultException e) {
            throw new IllegalArgumentException(Error.makeStoreDidNotSetBankAccountError(storeId));
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    /*@Override
    public boolean areStoresEqual(Store s1, Store s2) {
        if(!s1.equals(s2))
            return false;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            List<Integer> res1 = session.createQuery("SELECT DISTINCT key(sp) FROM StoreDTO s JOIN s.productAmounts sp WHERE s.storeId = :storeId").setParameter("storeId", s1.getStoreId()).list();
            List<Integer> res2 = session.createQuery("SELECT DISTINCT key(sp) FROM StoreDTO s JOIN s.productAmounts sp WHERE s.storeId = :storeId").setParameter("storeId", s2.getStoreId()).list();
            return Objects.equals(new HashSet<>(res1), new HashSet<>(res2));
        }
    }*/

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public boolean areProductsInStore(int storeId, Set<Integer> productIds) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            return store.hasProducts(productIds);
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    public void addManagerToStore(String username, int storeId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            store.addStoreManager(username);
            session.update(store);
            session.getTransaction().commit();
        }
        catch (PersistenceException e) {
            session.getTransaction().rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    public void addOwnerToStore(String username, int storeId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            store.addStoreOwner(username);
            session.update(store);
            session.getTransaction().commit();
        }
        catch (PersistenceException e) {
            session.getTransaction().rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    public void addOrderIdToStore(int storeId, int orderId) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            store.addOrderId(orderId);
            session.update(store);
            session.getTransaction().commit();
        }
        catch (PersistenceException e) {
            session.getTransaction().rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    @Override
    public void changeStoreState(String username, int storeId, boolean open, UserFacade userFacade) {
        Session session = null;
        try {
            session = HibernateUtil.getSessionFactory().openSession();
            session.beginTransaction();
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            if (!store.getFounderUsername().equals(username))
                throw new IllegalArgumentException(Error.makeStoreUserCannotCloseStoreError(username, storeId));
            if(!open) {
                store.closeStore();
            }
            else {
                store.reopenStore();
            }
            session.update(store);
            session.getTransaction().commit();

            notifyAboutStore(open, store, userFacade);
        }
        catch (PersistenceException e) {
            session.getTransaction().rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
        catch (Exception e) {
            session.getTransaction().rollback();
            throw e;
        }
        finally {
            session.close();
        }
    }

    private void notifyAboutStore(boolean open, Store store, UserFacade userFacade) {
        String format = open ? "The store \"%s\" was reopened." : "The store \"%s\" was closed.";
        String msg = String.format(format, store.getStoreInfo().getStoreName());
        Set<String> ownerUsernames = store.getOwnerUsernames();
        Set<String> managerUsernames = store.getManagerUsernames();
        for (String ownerUsername : ownerUsernames) {
            userFacade.notify(ownerUsername, msg);
        }
        for (String managerUsername : managerUsernames) {
            userFacade.notify(managerUsername, msg);
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Map<ProductDTO, Integer> getProductsInfoAndFilter(ProductFacade productFacade, int storeId, String productName, String category, double price, double minProductRank) {
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            Store store = session.get(Store.class, storeId);
            if (store == null) {
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            }
            Map<Integer, Integer> productAmounts = store.getProductAmounts();
            List<Integer> storeProductIds = new ArrayList<>(productAmounts.keySet());
            List<ProductDTO> filteredProducts = productFacade.getFilteredProducts(storeProductIds, productName, price, category, minProductRank);
            Map<ProductDTO, Integer> res = new HashMap<>();
            for (ProductDTO product : filteredProducts)
                res.put(product, productAmounts.get(product.getProductID()));
            return res;
        }
        catch (PersistenceException e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

}
