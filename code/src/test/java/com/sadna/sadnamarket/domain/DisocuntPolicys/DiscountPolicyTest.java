package com.sadna.sadnamarket.domain.DisocuntPolicys;

import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicyFacade;
import com.sadna.sadnamarket.domain.buyPolicies.MemoryBuyPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.IConditionRespository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.MemoryConditionRepository;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.IDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.MemoryDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.orders.MemoryOrderRepository;
import com.sadna.sadnamarket.domain.orders.OrderFacade;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.domain.users.MemoryRepo;
import com.sadna.sadnamarket.domain.users.UserFacade;
import org.junit.Before;
import org.junit.jupiter.api.BeforeEach;

import java.time.LocalDate;

public class DiscountPolicyTest {

    protected IConditionRespository conditionRepository;
    protected IDiscountPolicyRepository discountPolicyRepository;

    protected AuthFacade authFacade;
    protected ProductFacade productFacade;

    protected StoreFacade storeFacade;
    protected DiscountPolicyFacade discountPolicyFacade;

    @Before
    public void setUp() throws Exception {
        conditionRepository = new MemoryConditionRepository();
        discountPolicyRepository = new MemoryDiscountPolicyRepository();
        productFacade = new ProductFacade();
        storeFacade = new StoreFacade(new MemoryStoreRepository());
        OrderFacade orderFacade = new OrderFacade(new MemoryOrderRepository());
        UserFacade userFacade = new UserFacade(new MemoryRepo(), storeFacade, orderFacade);
        authFacade = new AuthFacade(new AuthRepositoryMemoryImpl(), userFacade);
        discountPolicyFacade = new DiscountPolicyFacade(conditionRepository, discountPolicyRepository);
        this.discountPolicyFacade.setProductFacade(productFacade);
        this.discountPolicyFacade.setStoreFacade(storeFacade);

        this.storeFacade.setUserFacade(userFacade);
        this.storeFacade.setOrderFacade(orderFacade);
        this.storeFacade.setProductFacade(productFacade);
        this.storeFacade.setDiscountPolicyFacade(discountPolicyFacade);
        BuyPolicyFacade buyPolicyFacade = new BuyPolicyFacade(new MemoryBuyPolicyRepository());
        buyPolicyFacade.setStoreFacade(storeFacade);
        buyPolicyFacade.setUserFacade(userFacade);
        buyPolicyFacade.setProductFacade(productFacade);
        this.storeFacade.setBuyPolicyFacade(buyPolicyFacade);

        authFacade.register("hila", "654321", "Eugene", "Krabs", "hilala@gmail.com", "0521957682", LocalDate.of(1942, 11, 30));
        authFacade.login("hila", "654321");

        storeFacade.createStore("hila", "hila's bakery", "stips", "hilala@gmail.com", "0546661111");

        storeFacade.addProductToStore("hila", 0, "eyal", 312, 100, "kids", 5, 2);
        storeFacade.addProductToStore("hila", 0, "milk", 312, 20, "dairy", 5, 2);
        storeFacade.addProductToStore("hila", 0, "cheese", 312, 40, "dairy", 5, 2);

        authFacade.register("maki", "654321", "Eugene", "Krabs", "maki@gmail.com", "0521957682", LocalDate.of(1942, 11, 30));
        authFacade.login("maki", "654321");

        storeFacade.createStore("maki", "maki's bakery", "stips", "maki@gmail.com", "0546661112");

        storeFacade.addProductToStore("maki", 1, "eyal", 312, 100, "kids", 5, 2);
        storeFacade.addProductToStore("maki", 1, "milk", 312, 20, "dairy", 5, 2);
        storeFacade.addProductToStore("maki", 1, "cheese", 312, 40, "dairy", 5, 2);

    }
}
