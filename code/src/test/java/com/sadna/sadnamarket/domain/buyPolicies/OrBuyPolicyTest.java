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

class OrBuyPolicyTest extends BuyPolicyTest{
    private OrBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();

        BuyPolicy policy1 = new AgeLimitBuyPolicy(2, List.of(BuyType.immidiatePurchase), new CategorySubject("Chocolate"), -1, 10);
        BuyPolicy policy2 = new AmountBuyPolicy(3, List.of(BuyType.immidiatePurchase), new CategorySubject("Chocolate"), 100, -1);
        BuyPolicy policy3 = new HolidayBuyPolicy(4, List.of(BuyType.immidiatePurchase), new CategorySubject("Alcohol"));
        this.policy = new OrBuyPolicy(6, new ConditioningBuyPolicy(5, policy1, policy2), policy3);
    }

    @Test
    void canBuyNotHoliday() {
        try (MockedStatic<HolidayBuyPolicy> mocked = mockStatic(HolidayBuyPolicy.class)) {
            mocked.when(HolidayBuyPolicy::isHoliday).thenReturn(false);
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
    void canBuyALittleBitChocolateOnHoliday() {
        try (MockedStatic<HolidayBuyPolicy> mocked = mockStatic(HolidayBuyPolicy.class)) {
            mocked.when(HolidayBuyPolicy::isHoliday).thenReturn(true);
            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 0, 80));
            cart.add(new CartItemDTO(0, 1, 12));

            Map<Integer, ProductDTO> cartMap = new HashMap<>();
            cartMap.put(0, productFacade.getProductDTO(0));
            cartMap.put(1, productFacade.getProductDTO(1));

            assertTrue(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("Mr. Krabs")).isEmpty());
            assertFalse(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("FourSeasonsOrlandoBaby")).isEmpty());
            assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
        }
    }

    @Test
    void canBuyALotOfChocolateOnHoliday() {
        try (MockedStatic<HolidayBuyPolicy> mocked = mockStatic(HolidayBuyPolicy.class)) {
            mocked.when(HolidayBuyPolicy::isHoliday).thenReturn(true);
            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 0, 80));
            cart.add(new CartItemDTO(0, 1, 200));

            Map<Integer, ProductDTO> cartMap = new HashMap<>();
            cartMap.put(0, productFacade.getProductDTO(0));
            cartMap.put(1, productFacade.getProductDTO(1));

            assertTrue(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("Mr. Krabs")).isEmpty());
            assertTrue(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("FourSeasonsOrlandoBaby")).isEmpty());
            assertTrue(policy.canBuy(cart, cartMap, null).isEmpty());
        }
    }
}