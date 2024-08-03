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

class AmountBuyPolicyTest extends BuyPolicyTest{
    private AmountBuyPolicy categoryPolicy;
    private AmountBuyPolicy productPolicy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();
        this.categoryPolicy = new AmountBuyPolicy(0, List.of(BuyType.immidiatePurchase), new CategorySubject("Chocolate"), 10, 20);
        this.productPolicy = new AmountBuyPolicy(0, List.of(BuyType.immidiatePurchase), new ProductSubject(0), 13, 15);
    }

    @Test
    void canBuySuccess() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 14)); // beer
        cart.add(new CartItemDTO(0, 1, 16)); // chocolate

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertTrue(categoryPolicy.canBuy(cart, cartMap, null).isEmpty());
        assertTrue(productPolicy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyFail() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 30)); // beer
        cart.add(new CartItemDTO(0, 1, 40)); // chocolate

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertFalse(categoryPolicy.canBuy(cart, cartMap, null).isEmpty());
        assertFalse(productPolicy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuySameProduct() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 3)); // beer
        cart.add(new CartItemDTO(0, 0, 18)); // more bber

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));

        assertFalse(productPolicy.canBuy(cart, cartMap, null).isEmpty());
    }
}