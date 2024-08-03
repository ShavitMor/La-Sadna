package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.MockedStatic;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

class RoshChodeshBuyPolicyTest extends BuyPolicyTest{
    private RoshChodeshBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();
        this.policy = new RoshChodeshBuyPolicy(2, List.of(BuyType.immidiatePurchase), new ProductSubject(0));
    }

    @Test
    void canBuyNoProduct() {
        try (MockedStatic<RoshChodeshBuyPolicy> mocked = mockStatic(RoshChodeshBuyPolicy.class)) {
            mocked.when(RoshChodeshBuyPolicy::isRoshChodesh).thenReturn(false);

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 0, 80));

            Map<Integer, ProductDTO> cartMap = new HashMap<>();
            cartMap.put(0, productFacade.getProductDTO(0));

            assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
        }
    }

    @Test
    void canBuyNotRoshChodesh() {
        try (MockedStatic<RoshChodeshBuyPolicy> mocked = mockStatic(RoshChodeshBuyPolicy.class)) {
            mocked.when(RoshChodeshBuyPolicy::isRoshChodesh).thenReturn(false);

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
    void canBuyRoshChodesh() {
        try (MockedStatic<RoshChodeshBuyPolicy> mocked = mockStatic(RoshChodeshBuyPolicy.class)) {
            mocked.when(RoshChodeshBuyPolicy::isRoshChodesh).thenReturn(true);

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