package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;
import org.springframework.cglib.core.Local;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class HourLimitBuyPolicyTest extends BuyPolicyTest{
    private HourLimitBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();
        this.policy = new HourLimitBuyPolicy(2, List.of(BuyType.immidiatePurchase), new CategorySubject("Alcohol"), LocalTime.of(6, 0), LocalTime.of(23,0));
    }

    @Test
    void canBuyNoAlcohol() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 1, 80));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(1, productFacade.getProductDTO(1));

        assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyAlcoholOnNoon() {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(LocalTime.of(12,0));

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 0, 80));
            cart.add(new CartItemDTO(0, 1, 12));

            Map<Integer, ProductDTO> cartMap = new HashMap<>();
            cartMap.put(0, productFacade.getProductDTO(0));
            cartMap.put(1, productFacade.getProductDTO(1));

            assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
        }
    }

    @Test
    void canBuyAlcoholOnMidnight() {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(LocalTime.of(0,0));

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 0, 80));
            cart.add(new CartItemDTO(0, 1, 12));

            Map<Integer, ProductDTO> cartMap = new HashMap<>();
            cartMap.put(0, productFacade.getProductDTO(0));
            cartMap.put(1, productFacade.getProductDTO(1));

            assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
        }
    }
}