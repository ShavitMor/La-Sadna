package com.sadna.sadnamarket.domain.stores;

import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.UserFacade;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.data.relational.core.sql.In;

import java.time.LocalTime;
import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeoutException;

public interface IStoreRepository {
    public Store findStoreByID(int storeId);

    public Set<Integer> getAllStoreIds();

    public int addStore(String founderUsername, String storeName, String address, String email, String phoneNumber);

    public boolean storeExists(int storeId);

    public boolean productExists(int storeId, int productId);

    public boolean hasProductInStock(int storeId, int productId, int amount);

    public int getProductAmountInStore(int storeId, int productId);

    //public void saveStore(Store store);

    public void addProductToStore(int storeId, int productId, int amount);

    public void deleteProductFromStore(int storeId, int productId);

    public void updateProductAmountInStore(int storeId, int productId, int newAmount);

    public StoreDTO getStoreDTO(int storeId);

    public Set<String> checkCartInStore(int storeId, List<CartItemDTO> cart);

    public Set<String> updateStockInStore(int storeId, List<CartItemDTO> cart);

    public Store findStoreByName(String storeName);

    public void setStoreBankAccount(int storeId, BankAccountDTO bankAccountDTO);

    public BankAccountDTO getStoreBankAccount(int storeId);

    //public boolean areStoresEqual(Store s1, Store s2);

    public boolean areProductsInStore(int storeId, Set<Integer> productIds);

    public void addManagerToStore(String username, int storeId);
    public void addOwnerToStore(String username, int storeId);
    public void addOrderIdToStore(int storeId, int orderId);
    public void changeStoreState(String username, int storeId, boolean open, UserFacade userFacade);

    public Map<ProductDTO, Integer> getProductsInfoAndFilter(ProductFacade productFacade, int storeId, String productName, String category, double price, double minProductRank);

    public static void cleanDB() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            try {
                session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE stores").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE store_products").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE store_owners").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE store_managers").executeUpdate();
                session.createNativeQuery("TRUNCATE TABLE store_orders").executeUpdate();
                session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();

                transaction.commit();
            } catch (Exception e) {
                if (transaction != null) {
                    transaction.rollback();
                }
                throw e;
            }
        }
    }

}