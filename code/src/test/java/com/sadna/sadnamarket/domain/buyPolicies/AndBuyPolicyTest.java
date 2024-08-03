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

class AndBuyPolicyTest extends BuyPolicyTest{
    private AndBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();
        int policyId0 = buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Alcohol", List.of(BuyType.immidiatePurchase), 18, -1, "WillyTheChocolateDude");
        int policyId1 = buyPolicyFacade.createProductAmountBuyPolicy(1, List.of(BuyType.immidiatePurchase), -1, 10, "WillyTheChocolateDude");
        int policyId2 = buyPolicyFacade.createProductKgBuyPolicy(0, List.of(BuyType.immidiatePurchase), 30, 40, "WillyTheChocolateDude");

        BuyPolicy policy1 = buyPolicyFacade.getBuyPolicy(policyId0);
        BuyPolicy policy2 = buyPolicyFacade.getBuyPolicy(policyId1);
        BuyPolicy policy3 = buyPolicyFacade.getBuyPolicy(policyId2);

        this.policy = new AndBuyPolicy(4, policy1, new OrBuyPolicy(3, policy2, policy3));
    }

    @Test
    void canBuySuccess() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 17));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));

        assertTrue(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("Mr. Krabs")).isEmpty());
    }

    @Test
    void canBuyNoAlcohol() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 1, 10));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(1, productFacade.getProductDTO(1));

        assertTrue(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("Mr. Krabs")).isEmpty());
        assertTrue(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("FourSeasonsOrlandoBaby")).isEmpty());
    }

    @Test
    void canBuyBabyBuysAlcohol() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 17));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));

        assertFalse(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("FourSeasonsOrlandoBaby")).isEmpty());
    }

    @Test
    void canBuyTooMuchChocolate() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 1, 17));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(1, productFacade.getProductDTO(1));

        assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyTooHeavyAlcohol() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 80));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));

        assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
    }

    @Test
    void canBuyTooHeavyAlcoholRightAmountChocolate() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 80));
        cart.add(new CartItemDTO(0, 1, 6));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        assertFalse(policy.canBuy(cart, cartMap, null).isEmpty());
        assertTrue(policy.canBuy(cart, cartMap, userFacade.getMemberDTO("Mr. Krabs")).isEmpty());
    }
}