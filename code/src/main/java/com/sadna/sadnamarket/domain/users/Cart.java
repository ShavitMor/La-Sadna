package com.sadna.sadnamarket.domain.users;

import com.sadna.sadnamarket.service.Error;

import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.persistence.*;

@Entity
@Table(name = "carts")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Cart {
    
    @Id
    @GeneratedValue(strategy = javax.persistence.GenerationType.TABLE)
    private int cartId;

    @OneToMany(cascade = CascadeType.ALL, orphanRemoval = true)
    @JoinColumn(name = "cart_id") // Foreign key column in Basket table
    private Map<Integer, Basket> baskets;
    private static final Logger logger = LogManager.getLogger(Cart.class);

    public Cart() {
        logger.info("Entering Cart constructor");
        baskets = new HashMap<>();
        logger.info("Exiting Cart constructor");
    }
    public Cart(List<CartItemDTO> cartItems){
        logger.info("Entering Cart constructor with cartItems={}",cartItems);
        baskets = new HashMap<>();
        for (CartItemDTO cartItem : cartItems) {
            addProduct(cartItem.getStoreId(),cartItem.getProductId(),cartItem.getAmount());
        }
        logger.info("Exiting Cart constructor");
    }

    public void addProduct(int storeId, int productId, int amount) {
        logger.info("Entering addProduct with storeId={}, productId={}, and amount={}", storeId, productId, amount);
        if (hasStore(storeId)) {
            baskets.get(storeId).addProduct(productId, amount);
        } else {
            Basket basket = new Basket(storeId);
            basket.addProduct(productId, amount);
            baskets.put(storeId, basket);
        }
        logger.info("Exiting addProduct");
    }

    public void clear(){
        logger.info("clearing cart");
        baskets.clear();
        logger.info("cart cleared");
    }

    public void removeProduct(int storeId, int productId) {
        logger.info("Entering removeProduct with storeId={} and productId={}", storeId, productId);
        if (!hasStore(storeId)) {
            logger.error("Exception in removeProduct: product doesn't exist in cart");
            throw new IllegalArgumentException(Error.makeBasketProductDoesntExistError());
        }
        baskets.get(storeId).removeProduct(productId);
        if(baskets.get(storeId).isEmpty())
            baskets.remove(storeId);
        logger.info("Exiting removeProduct");
    }

    private boolean hasStore(int storeId) {
        logger.info("Entering hasStore with storeId={}", storeId);
        boolean result = baskets.containsKey(storeId);
        logger.info("Exiting hasStore with result={}", result);
        return result;
    }

    public boolean isEmpty() {
        logger.info("Entering isEmpty");
        boolean result = baskets.isEmpty();
        logger.info("Exiting isEmpty with result={}", result);
        return result;
    }

    public void changeQuantity(int storeId, int productId, int quantity) {
        logger.info("Entering changeQuantity with storeId={}, productId={}, and quantity={}", storeId, productId, quantity);
        if (!hasStore(storeId)) {
            logger.error("Exception in changeQuantity: product doesn't exist in cart");
            throw new IllegalArgumentException(Error.makeBasketProductDoesntExistError());
        }
        baskets.get(storeId).changeQuantity(productId, quantity);
        logger.info("Exiting changeQuantity");
    }

    public List<CartItemDTO> getCartItems() {
        logger.info("getting cart items");
        List<CartItemDTO> cartItems=new ArrayList<>();
        for (Integer basketStore : baskets.keySet()) {
            Map<Integer,Integer> basketProducts=baskets.get(basketStore).getProducts();
            for (Integer productId : basketProducts.keySet()) {
                CartItemDTO cartItemDTO=new CartItemDTO(basketStore, productId, basketProducts.get(productId));
                cartItems.add(cartItemDTO);
            }
        }
        logger.info("got cart items {}",cartItems);
        return cartItems;
    }
}
