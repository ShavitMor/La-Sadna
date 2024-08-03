package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class KgLimitBuyPolicyTest extends BuyPolicyTest{
    private KgLimitBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();
        this.policy = new KgLimitBuyPolicy(2, List.of(BuyType.immidiatePurchase), new ProductSubject(0), 8, 20);
    }

    @Test
    void canBuyNoProduct() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 1, 80));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(1, productFacade.getProductDTO(1));

        assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyTooLight() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 3));
        cart.add(new CartItemDTO(0, 1, 80));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyTooHeavy() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 11));
        cart.add(new CartItemDTO(0, 1, 80));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuySucess() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 4));
        cart.add(new CartItemDTO(0, 0, 5));
        cart.add(new CartItemDTO(0, 1, 80));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
    }
}