package com.sadna.sadnamarket.domain.users;

import com.sadna.sadnamarket.service.Error;

import javax.persistence.*;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.Map;
import java.io.Serializable;
import java.util.HashMap;

@Entity
@Table(name = "baskets")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Basket implements Serializable {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    @Column(name = "basket_id")
    private int id;

    @Column
    private int storeId;


    @ElementCollection(fetch = FetchType.EAGER)
    @CollectionTable(name = "basket_products", joinColumns =@JoinColumn(name = "basket_id"))
    @MapKeyColumn(name = "product_id")
    @Column(name = "quantity")
    private Map<Integer, Integer> products;
    private static final Logger logger = LogManager.getLogger(Basket.class);

    public Basket(int storeId) {
        logger.info("Entering Basket constructor with storeId={}", storeId);
        this.storeId = storeId;
        products = new HashMap<>();
        logger.info("Exiting Basket constructor");
    }
    public Basket(){
        logger.info("Entering Basket constructor");
        logger.info("Exiting Basket constructor");
    }
    public int getStoreId() {
        logger.info("Entering getStoreId");
        logger.info("Exiting getStoreId with result={}", storeId);
        return storeId;
    }

    public void addProduct(int productId, int amount) {
        logger.info("Entering addProduct with productId={} and amount={}", productId, amount);
        if (hasProduct(productId))
            throw new IllegalArgumentException(Error.makeBasketProductAlreadyExistsError());
        products.put(productId, amount);
        logger.info("Exiting addProduct");
    }

    public void removeProduct(int productId) {
        logger.info("Entering removeProduct with productId={}", productId);
        if (!hasProduct(productId))
            throw new IllegalArgumentException(Error.makeBasketProductDoesntExistError());
        products.remove(productId);
        logger.info("Exiting removeProduct");
    }

    private boolean hasProduct(int productId) {
        logger.info("Entering hasProduct with productId={}", productId);
        boolean result = products.containsKey(productId);
        logger.info("Exiting hasProduct with result={}", result);
        return result;
    }

    public void changeQuantity(int productId, int quantity) {
        logger.info("Entering changeQuantity with productId={} and quantity={}", productId, quantity);
        if (!hasProduct(productId))
            throw new IllegalArgumentException(Error.makeBasketProductDoesntExistError());
        products.replace(productId, quantity);
        logger.info("Exiting changeQuantity");
    }

    public Map<Integer,Integer> getProducts(){
        logger.info("return products from basket {}",products);
        return products;
    }
    public boolean isEmpty(){
        logger.info("check if basket empty");
        boolean isEmpty=products.isEmpty();
        logger.info("checked if basket empty and got: {}",isEmpty);
        return isEmpty;
    }
}
