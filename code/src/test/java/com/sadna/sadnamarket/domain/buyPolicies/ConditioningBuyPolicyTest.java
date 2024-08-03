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

class ConditioningBuyPolicyTest extends BuyPolicyTest{
    private ConditioningBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();

        BuyPolicy policy1 = new KgLimitBuyPolicy(2, List.of(BuyType.immidiatePurchase), new ProductSubject(1), 10, 20);
        BuyPolicy policy2 = new AmountBuyPolicy(3, List.of(BuyType.immidiatePurchase), new CategorySubject("Alcohol"), -1, 9);

        this.policy = new ConditioningBuyPolicy(2, policy1, policy2);
    }

    @Test
    void canBuyBothConditions() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 3));
        cart.add(new CartItemDTO(0, 1, 6));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyNoConditions() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 1));
        cart.add(new CartItemDTO(0, 1, 13));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyOnlyFirstCondition() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 15));
        cart.add(new CartItemDTO(0, 1, 7));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyOnlySecondCondition() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 3));
        cart.add(new CartItemDTO(0, 1, 13));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
    }
}