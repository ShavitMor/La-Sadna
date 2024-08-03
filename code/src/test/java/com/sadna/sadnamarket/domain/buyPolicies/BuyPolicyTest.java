package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
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
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.time.LocalDate;
import java.time.LocalTime;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;

import static org.junit.jupiter.api.Assertions.assertFalse;
import static org.junit.jupiter.api.Assertions.assertTrue;

class BuyPolicyTest {
    protected StoreFacade storeFacade;
    protected AuthFacade authFacade;
    protected UserFacade userFacade;
    protected ProductFacade productFacade;
    protected BuyPolicyFacade buyPolicyFacade;

    @BeforeEach
    void setUp() throws JsonProcessingException {
        setUpFacades();
        registerUsers();
        generateStore();
        addProducts();
    }

    private void setUpFacades() {
        IOrderRepository orderRepo = new MemoryOrderRepository();
        OrderFacade orderFacade = new OrderFacade(orderRepo);

        IStoreRepository storeRepo = new MemoryStoreRepository();
        this.storeFacade = new StoreFacade(storeRepo);

        IUserRepository userRepo = new MemoryRepo();
        this.userFacade = new UserFacade(userRepo, storeFacade, orderFacade);

        IProductRepository productRepo = new MemoryProductRepository();
        this.productFacade = new ProductFacade(productRepo);

        this.buyPolicyFacade = new BuyPolicyFacade(new MemoryBuyPolicyRepository());

        this.storeFacade.setUserFacade(userFacade);
        this.storeFacade.setOrderFacade(orderFacade);
        this.storeFacade.setProductFacade(productFacade);
        this.storeFacade.setBuyPolicyFacade(buyPolicyFacade);

        this.authFacade = new AuthFacade(new AuthRepositoryMemoryImpl(), userFacade);
        this.buyPolicyFacade.setStoreFacade(storeFacade);
        this.buyPolicyFacade.setUserFacade(userFacade);
        this.buyPolicyFacade.setProductFacade(productFacade);
    }

    private void registerUsers() {
        authFacade.register("Mr. Krabs", "654321", "Eugene", "Krabs", "eugene@gmail.com", "0521957682", LocalDate.of(1942, 11, 30));
        authFacade.login("Mr. Krabs", "654321");
        authFacade.register("WillyTheChocolateDude", "123456", "Willy", "Wonka", "willy@gmail.com", "0541095600", LocalDate.of(1995, 12, 12));
        authFacade.login("WillyTheChocolateDude", "123456");
        authFacade.register("FourSeasonsOrlandoBaby", "654321", "Baby", "Orlando", "baby@gmail.com", "0528997287", LocalDate.of(2022, 8, 19));
        authFacade.login("FourSeasonsOrlandoBaby", "654321");
    }

    private void generateStore()  {
        storeFacade.createStore("WillyTheChocolateDude", "Chocolate Factory", "Beer Sheva", "chocolate@gmail.com", "0541075403");
    }

    private void addProducts() {
        storeFacade.addProductToStore("WillyTheChocolateDude", 0, "Beer", 133, 12, "Alcohol", 4.3,2);
        storeFacade.addProductToStore("WillyTheChocolateDude", 0, "Klik Cariot", 312, 7, "Chocolate", 5, 2);
    }
}