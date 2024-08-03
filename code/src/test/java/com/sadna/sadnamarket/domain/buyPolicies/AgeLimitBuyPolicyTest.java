package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.orders.IOrderRepository;
import com.sadna.sadnamarket.domain.orders.MemoryOrderRepository;
import com.sadna.sadnamarket.domain.orders.OrderFacade;
import com.sadna.sadnamarket.domain.products.IProductRepository;
import com.sadna.sadnamarket.domain.products.MemoryProductRepository;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.stores.IStoreRepository;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.domain.users.*;
import com.sadna.sadnamarket.service.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.*;

class AgeLimitBuyPolicyTest extends BuyPolicyTest{
    private AgeLimitBuyPolicy policy;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        super.setUp();
        this.policy = new AgeLimitBuyPolicy(0, List.of(BuyType.immidiatePurchase), new CategorySubject("Alcohol"), 18, -1);
    }

    @Test
    void canBuyWithAlcohol() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 1));
        cart.add(new CartItemDTO(0, 1, 2));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));

        MemberDTO adult = userFacade.getMemberDTO("Mr. Krabs");
        MemberDTO baby = userFacade.getMemberDTO("FourSeasonsOrlandoBaby");
        MemberDTO guest = null;

        assertTrue(policy.canBuy(cart, cartMap, adult).isEmpty());
        assertFalse(policy.canBuy(cart, cartMap, baby).isEmpty());
        assertFalse(policy.canBuy(cart, cartMap, guest).isEmpty());
    }

    @Test
    void canBuyNoAlcohol() {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 1, 1));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(1, productFacade.getProductDTO(1));

        MemberDTO adult = userFacade.getMemberDTO("Mr. Krabs");
        MemberDTO baby = userFacade.getMemberDTO("FourSeasonsOrlandoBaby");
        MemberDTO guest = null;

        assertTrue(policy.canBuy(cart, cartMap, adult).isEmpty());
        assertTrue(policy.canBuy(cart, cartMap, baby).isEmpty());
        assertTrue(policy.canBuy(cart, cartMap, guest).isEmpty());
    }
}