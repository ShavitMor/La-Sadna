package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class SpecificDateBuyPolicyTest extends BuyPolicyTest{
    private SpecificDateBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();
        policy = new SpecificDateBuyPolicy(2, List.of(BuyType.immidiatePurchase), new CategorySubject("Chocolate"), 2, -1, -1);
        // you can not buy chocolate on the second day of the month
    }

    @Test
    void canBuyOnSpecificDate() {
        try (MockedStatic<SpecificDateBuyPolicy> mocked = mockStatic(SpecificDateBuyPolicy.class)) {
            mocked.when(SpecificDateBuyPolicy::getCurrDate).thenReturn(LocalDate.of(2024, 6, 2));
            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 0, 80));
            cart.add(new CartItemDTO(0, 1, 12));

            Map<Integer, ProductDTO> cartMap = new HashMap<>();
            cartMap.put(0, productFacade.getProductDTO(0));
            cartMap.put(1, productFacade.getProductDTO(1));

            assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
        }
    }

    @Test
    void canBuyNotOnSpecificDate() {
        try (MockedStatic<SpecificDateBuyPolicy> mocked = mockStatic(SpecificDateBuyPolicy.class)) {
            mocked.when(SpecificDateBuyPolicy::getCurrDate).thenReturn(LocalDate.of(2024, 6, 3));
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
    void canBuyNoChocolate() {
        try (MockedStatic<SpecificDateBuyPolicy> mocked = mockStatic(SpecificDateBuyPolicy.class)) {
            mocked.when(SpecificDateBuyPolicy::getCurrDate).thenReturn(LocalDate.of(2024, 6, 2));
            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 0, 80));

            Map<Integer, ProductDTO> cartMap = new HashMap<>();
            cartMap.put(0, productFacade.getProductDTO(0));

            assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
        }
    }
}