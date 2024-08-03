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
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.stores.IStoreRepository;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.domain.users.IUserRepository;
import com.sadna.sadnamarket.domain.users.MemoryRepo;
import com.sadna.sadnamarket.domain.users.UserFacade;
import com.sadna.sadnamarket.service.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.List;

import static org.junit.jupiter.api.Assertions.*;

class BuyPolicyManagerTest {
    private BuyPolicyManager manager;
    private BuyPolicyFacade buyPolicyFacade;

    @BeforeEach
    void setUp() {
        this.buyPolicyFacade = new BuyPolicyFacade(new MemoryBuyPolicyRepository());
        this.manager = new MemoryBuyPolicyManager(buyPolicyFacade);
    }

    @Test
    void hasPolicy() {
        manager.addBuyPolicy(0);
        manager.addLawBuyPolicy(1);

        assertTrue(manager.hasPolicy(0));
        assertTrue(manager.hasPolicy(1));
        assertFalse(manager.hasPolicy(2));
    }

    @Test
    void addBuyPolicySuccess() {
        manager.addBuyPolicy(0);
        assertTrue(manager.hasPolicy(0));
    }

    @Test
    void addBuyPolicyAlreadyExists() {
        manager.addBuyPolicy(0);

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            manager.addBuyPolicy(0);
        });

        String expectedMessage = Error.makeBuyPolicyAlreadyExistsError(0);

        assertEquals(expectedMessage, expected.getMessage());
    }

    @Test
    void addLawBuyPolicySuccess() {
        manager.addLawBuyPolicy(0);
        assertTrue(manager.hasPolicy(0));
    }

    @Test
    void addLawBuyPolicyAlreadyExists() {
        manager.addBuyPolicy(0);

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            manager.addLawBuyPolicy(0);
        });

        String expectedMessage = Error.makeBuyPolicyAlreadyExistsError(0);

        assertEquals(expectedMessage, expected.getMessage());
    }

    @Test
    void removeBuyPolicySuccess() {
        manager.addBuyPolicy(0);
        assertTrue(manager.hasPolicy(0));
        manager.removeBuyPolicy(0);
        assertFalse(manager.hasPolicy(0));
    }


    @Test
    void removeBuyPolicyNoPolicy() {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            manager.removeBuyPolicy(0);
        });

        String expectedMessage = Error.makeBuyPolicyWithIdDoesNotExistError(0);

        assertEquals(expectedMessage, expected.getMessage());
    }

    @Test
    void removeBuyPolicyLawPolicy() {
        manager.addLawBuyPolicy(0);

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            manager.removeBuyPolicy(0);
        });

        String expectedMessage = Error.makeCanNotRemoveLawBuyPolicyError(0);

        assertEquals(expectedMessage, expected.getMessage());
    }
}