package com.sadna.sadnamarket;

import com.sadna.sadnamarket.domain.auth.AuthRepositoryHibernateImpl;
import com.sadna.sadnamarket.domain.auth.IAuthRepository;
import com.sadna.sadnamarket.domain.buyPolicies.HibernateBuyPolicyRepository;
import com.sadna.sadnamarket.domain.buyPolicies.IBuyPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.HibernateConditionRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.IConditionRespository;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.HibernateDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.IDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.orders.HibernateOrderRepository;
import com.sadna.sadnamarket.domain.orders.IOrderRepository;
import com.sadna.sadnamarket.domain.products.HibernateProductRepository;
import com.sadna.sadnamarket.domain.products.IProductRepository;
import com.sadna.sadnamarket.domain.products.Product;
import com.sadna.sadnamarket.domain.stores.HibernateStoreRepository;
import com.sadna.sadnamarket.domain.stores.IStoreRepository;
import com.sadna.sadnamarket.domain.stores.Store;
import com.sadna.sadnamarket.domain.users.IUserRepository;
import com.sadna.sadnamarket.domain.users.UserHibernateRepo;
import com.sadna.sadnamarket.service.MarketService;
import com.sadna.sadnamarket.service.RealtimeService;
import org.junit.After;
import org.junit.jupiter.api.*;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.test.context.ActiveProfiles;
import com.sadna.sadnamarket.service.Error;
import static org.junit.jupiter.api.Assertions.assertThrows;


public class BootingTests {

    static MarketService market;
    RealtimeService realtimeService;
    IStoreRepository storeRepo;
    IOrderRepository orderRepo;
    IProductRepository productRepo;
    IBuyPolicyRepository policyRepo;
    IDiscountPolicyRepository discountRepo;
    IConditionRespository conditionRepo;
    IAuthRepository authRepo;
    IUserRepository userRepo;

    void createMarket(){
        realtimeService = null;
        storeRepo = new HibernateStoreRepository();
        orderRepo = new HibernateOrderRepository();
        productRepo = new HibernateProductRepository();
        policyRepo = new HibernateBuyPolicyRepository();
        discountRepo = new HibernateDiscountPolicyRepository();
        conditionRepo = new HibernateConditionRepository();
        authRepo = new AuthRepositoryHibernateImpl();
        userRepo = new UserHibernateRepo();

        market = new MarketService(realtimeService, storeRepo,
                orderRepo, productRepo, policyRepo, discountRepo, conditionRepo, authRepo, userRepo);

    }

    @Test
    public void testCrashConfig() throws Exception {
        //config contain state that register the two same user
        Config.read("configForTests/configTestCrash.json");

        Exception exception = assertThrows(Exception.class, this::createMarket);
        String expectedMessage = "Start from state failed at command register: username already exists";
        String actualMessage = exception.getMessage();

        Assertions.assertEquals(expectedMessage, actualMessage);

        //to do clear or something
    }

    @AfterAll
    public static void after() {
        if(market != null){
            market.clear();
        }
        Config.read("testconfig.json");
    }

    @Nested
    @TestInstance(TestInstance.Lifecycle.PER_CLASS)
    class TestNest {
        @BeforeAll
        public void setUpOnce() {
            Config.read("configForTests/configCheck.json");
            createMarket();
        }

        @Test
        public void testConfigUsersExist() {

            Assertions.assertEquals("true", market.memberExists("u1").getDataJson());
            Assertions.assertEquals("true", market.memberExists("u2").getDataJson());
            Assertions.assertEquals("true", market.memberExists("u3").getDataJson());
            Assertions.assertEquals("true", market.memberExists("u4").getDataJson());
            Assertions.assertEquals("true", market.memberExists("u5").getDataJson());
            Assertions.assertEquals("true", market.memberExists("u6").getDataJson());
            Assertions.assertEquals(Error.makeMemberUserDoesntExistError("u7"), market.memberExists("u7").getErrorString());
        }
        @Test
        public void testConfigUserRoles() {
            Assertions.assertEquals("true", market.checkIfSystemManager("u1").getDataJson());
            Store store = storeRepo.findStoreByName("s1");
            Assertions.assertTrue(store.isStoreManager("u3"));
            Assertions.assertTrue(store.isStoreOwner("u4"));
            Assertions.assertTrue(store.isStoreOwner("u5"));
            Assertions.assertEquals("u2", store.getFounderUsername());

        }
        @Test
        public void testConfigStoreAndProductSetUp() {
            //checking if users have the right roles and store 0 exist

            Store store = storeRepo.findStoreByName("s1");
            productRepo.getAllProducts();
            //check there is product in store and it has 20 instances of it and its name is Bamba
            Assertions.assertEquals(1,productRepo.getAllProducts().size());
            Product product = productRepo.getAllProducts().get(0);
            Assertions.assertEquals("Bamba", product.getProductName());
            Assertions.assertEquals(store.getStoreId(), product.getStoreId());
            Assertions.assertEquals(20, storeRepo.getProductAmountInStore(store.getStoreId(), product.getProductId()));


        }

        @Test
        public void testConfigUserLoggedOut() {
            Assertions.assertFalse(userRepo.isLoggedIn("u5"));
            Assertions.assertFalse(userRepo.isLoggedIn("u4"));
            Assertions.assertFalse(userRepo.isLoggedIn("u3"));
            Assertions.assertFalse(userRepo.isLoggedIn("u2"));
            Assertions.assertFalse(userRepo.isLoggedIn("u1"));
        }
        @AfterAll
        public void after() {
            if(market != null){
                market.clear();
            }
            Config.read("testconfig.json");
        }
    }

}