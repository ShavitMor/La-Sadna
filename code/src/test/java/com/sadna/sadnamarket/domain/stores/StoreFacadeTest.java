package com.sadna.sadnamarket.domain.stores;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
import com.sadna.sadnamarket.domain.buyPolicies.*;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.Condition;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.MemoryConditionRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.MinProductCondition;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.Discount;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.MemoryDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.SimpleDiscount;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.orders.*;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.payment.PaymentInterface;
import com.sadna.sadnamarket.domain.payment.PaymentService;
import com.sadna.sadnamarket.domain.products.IProductRepository;
import com.sadna.sadnamarket.domain.products.MemoryProductRepository;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.supply.SupplyInterface;
import com.sadna.sadnamarket.domain.supply.SupplyService;
import com.sadna.sadnamarket.domain.users.*;
import com.sadna.sadnamarket.service.Error;
import org.junit.jupiter.api.*;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.Mockito;
import org.springframework.security.access.method.P;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.mockStatic;

@TestInstance(TestInstance.Lifecycle.PER_CLASS)
@Transactional
class StoreFacadeTest {
    private StoreFacade storeFacade;
    private AuthFacade authFacade;
    private UserFacade userFacade;
    private ProductFacade productFacade;
    private BuyPolicyFacade buyPolicyFacade;
    private DiscountPolicyFacade discountPolicyFacade;
    private OrderFacade orderFacade;
    static ObjectMapper objectMapper = new ObjectMapper();
    private static SimpleFilterProvider idFilter;
    private int storeId0Mem;
    private int storeId0Db;
    private int storeId0;
    private int storeId1Mem;
    private int storeId1Db;
    private int storeId1;
    private int p0Mem;
    private int p1Mem;
    private int p2Mem;
    private int p3Mem;
    private int p4Mem;
    private int p0Db;
    private int p1Db;
    private int p2Db;
    private int p3Db;
    private int p4Db;
    private int p0;
    private int p1;
    private int p2;
    private int p3;
    private int p4;

    private static IStoreRepository hibernateStoreRepository;
    private static IStoreRepository memoryStoreRepository;


    static Stream<IStoreRepository> repositoryProvider() {
        return Stream.of(memoryStoreRepository, hibernateStoreRepository);
    }

    @BeforeAll
    public void setUpBeforeAll() throws JsonProcessingException {
        idFilter = new SimpleFilterProvider().addFilter("idFilter", SimpleBeanPropertyFilter.serializeAllExcept("id"));
        objectMapper.registerModule(new JavaTimeModule());

        hibernateStoreRepository = new HibernateStoreRepository();
        memoryStoreRepository = new MemoryStoreRepository();
        IStoreRepository.cleanDB();
        setUpFacades();
        registerUsers();
        storeFacade.setStoreRepository(memoryStoreRepository);
        generateStore0(memoryStoreRepository);
        generateStore1(memoryStoreRepository);
        addProducts0(memoryStoreRepository);
        addProducts1(memoryStoreRepository);

        storeFacade.setStoreRepository(hibernateStoreRepository);
        generateStore0(hibernateStoreRepository);
        generateStore1(hibernateStoreRepository);
        addProducts0(hibernateStoreRepository);
        addProducts1(hibernateStoreRepository);

        int policyId1 = buyPolicyFacade.createCategoryHolidayBuyPolicy("Chocolate", List.of(BuyType.immidiatePurchase), "WillyTheChocolateDude");
        int policyId2Mem = buyPolicyFacade.createProductAmountBuyPolicy(p2Mem, List.of(BuyType.immidiatePurchase), 3, 10, "WillyTheChocolateDude");
        int policyId2Db = buyPolicyFacade.createProductAmountBuyPolicy(p2Db, List.of(BuyType.immidiatePurchase), 3, 10, "WillyTheChocolateDude");
        int policyId3Mem = buyPolicyFacade.createOrBuyPolicy(policyId1, policyId2Mem, "WillyTheChocolateDude");
        int policyId3Db = buyPolicyFacade.createOrBuyPolicy(policyId1, policyId2Db, "WillyTheChocolateDude");

        storeFacade.setStoreRepository(memoryStoreRepository);
        buyPolicyFacade.addBuyPolicyToStore("WillyTheChocolateDude", storeId0Mem, policyId3Mem);
        storeFacade.setStoreRepository(hibernateStoreRepository);
        buyPolicyFacade.addBuyPolicyToStore("WillyTheChocolateDude", storeId0Db, policyId3Db);
    }

    public void setUp(IStoreRepository repo) {
        this.storeId0 = (repo instanceof MemoryStoreRepository) ? storeId0Mem : storeId0Db;
        this.storeId1 = (repo instanceof MemoryStoreRepository) ? storeId1Mem : storeId1Db;
        this.p0 = (repo instanceof MemoryStoreRepository) ? p0Mem : p0Db;
        this.p1 = (repo instanceof MemoryStoreRepository) ? p1Mem : p1Db;
        this.p2 = (repo instanceof MemoryStoreRepository) ? p2Mem : p2Db;
        this.p3 = (repo instanceof MemoryStoreRepository) ? p3Mem : p3Db;
        this.p4 = (repo instanceof MemoryStoreRepository) ? p4Mem : p4Db;

        storeFacade.setStoreRepository(repo);
    }

    @AfterEach
    public void cleanUp() {
        storeFacade.setStoreRepository(memoryStoreRepository);
        if(!storeFacade.isStoreActive(storeId0Mem)) {
            storeFacade.reopenStore("WillyTheChocolateDude", storeId0Mem);
        }
        if(!storeFacade.isStoreActive(storeId1Mem)) {
            storeFacade.reopenStore("Mr. Krabs", storeId1Mem);
        }

        storeFacade.setStoreRepository(hibernateStoreRepository);
        if(!storeFacade.isStoreActive(storeId0Db)) {
            storeFacade.reopenStore("WillyTheChocolateDude", storeId0Db);
        }
        if(!storeFacade.isStoreActive(storeId1Db)) {
            storeFacade.reopenStore("Mr. Krabs", storeId1Db);
        }
    }

    private void setUpFacades() {
        IOrderRepository orderRepo = new MemoryOrderRepository();
        this.orderFacade = new OrderFacade(orderRepo);

        this.storeFacade = new StoreFacade();

        IUserRepository userRepo = new MemoryRepo();
        this.userFacade = new UserFacade(userRepo, storeFacade, orderFacade);

        IProductRepository productRepo = new MemoryProductRepository();
        this.productFacade = new ProductFacade(productRepo);

        this.buyPolicyFacade = new BuyPolicyFacade(new MemoryBuyPolicyRepository());
        this.discountPolicyFacade = new DiscountPolicyFacade(new MemoryConditionRepository(), new MemoryDiscountPolicyRepository());

        this.storeFacade.setUserFacade(userFacade);
        this.storeFacade.setOrderFacade(orderFacade);
        this.storeFacade.setProductFacade(productFacade);
        this.storeFacade.setBuyPolicyFacade(buyPolicyFacade);
        this.storeFacade.setDiscountPolicyFacade(discountPolicyFacade);

        this.authFacade = new AuthFacade(new AuthRepositoryMemoryImpl(), userFacade);
        this.buyPolicyFacade.setStoreFacade(storeFacade);
        this.buyPolicyFacade.setUserFacade(userFacade);
        this.buyPolicyFacade.setProductFacade(productFacade);

        this.discountPolicyFacade.setStoreFacade(storeFacade);
        this.discountPolicyFacade.setProductFacade(productFacade);

        this.orderFacade.setStoreFacade(storeFacade);
    }

    private void registerUsers() {
        authFacade.register("Mr. Krabs", "654321", "Eugene", "Krabs", "eugene@gmail.com", "0521957682", LocalDate.of(1942, 11, 30));
        authFacade.login("Mr. Krabs", "654321");
        authFacade.register("WillyTheChocolateDude", "123456", "Willy", "Wonka", "willy@gmail.com", "0541095600", LocalDate.of(1995, 3, 8));
        authFacade.login("WillyTheChocolateDude", "123456");
        authFacade.register("Bob", "24680", "Bob", "Cohen", "bob@gmail.com", "0544219674", LocalDate.of(2001, 12, 1));
        authFacade.login("Bob", "24680");
        authFacade.register("Alice", "08642", "Alice", "Levi", "alice@gmail.com", "0523176455", LocalDate.of(2004, 1, 18));
    }

    private void addProducts0(IStoreRepository repo) {
        this.storeId0 = (repo instanceof MemoryStoreRepository) ? storeId0Mem : storeId0Db;

        int id0 = storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "Shokolad Parah", 1000, 5.5, "Chocolate", 4,2);
        int id1 = storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "Kif Kef", 982, 4.2, "Chocolate", 4.3,2);
        int id2 = storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "Klik Cariot", 312, 7, "Chocolate", 5, 2);
        if(repo instanceof MemoryStoreRepository) {
            this.p0Mem = id0;
            this.p1Mem = id1;
            this.p2Mem = id2;
        }
        else {
            this.p0Db = id0;
            this.p1Db = id1;
            this.p2Db = id2;
        }
    }

    private void addProducts1(IStoreRepository repo) {
        this.storeId1 = (repo instanceof MemoryStoreRepository) ? storeId1Mem : storeId1Db;

        int id3 = storeFacade.addProductToStore("Mr. Krabs", storeId1, "Beer", 1000, 12, "Alcohol", 4,1);
        int id4 = storeFacade.addProductToStore("Mr. Krabs", storeId1, "Wine", 1000, 50, "Alcohol", 4.7,1);
        if(repo instanceof MemoryStoreRepository) {
            this.p3Mem = id3;
            this.p4Mem = id4;
        }
        else {
            this.p3Db = id3;
            this.p4Db = id4;
        }
    }

    private StoreInfo generateStore0Info() {
        return new StoreInfo("Chocolate Factory", "Beer Sheva", "chocolate@gmail.com", "0541075403");
    }

    private StoreInfo generateStore1Info() {
        return new StoreInfo("Krusty Krab", "Bikini Bottom", "krab@gmail.com", "0541085120");
    }

    private Store generateStoreObject0() {
        Store s = new Store(storeId0, "WillyTheChocolateDude", generateStore0Info());
        boolean storeClosed = !storeFacade.isStoreActive(storeId0);
        if(storeClosed) {
            storeFacade.reopenStore("WillyTheChocolateDude", storeId0);
        }
        s.addProduct(p0, storeFacade.getProductAmount(storeId0, p0));
        s.addProduct(p1, storeFacade.getProductAmount(storeId0, p1));
        s.addProduct(p2, storeFacade.getProductAmount(storeId0, p2));
        if(storeClosed) {
            storeFacade.closeStore("WillyTheChocolateDude", storeId0);
            s.closeStore();
        }
        return s;
    }

    private void generateStore0(IStoreRepository repo)  {
        if(repo instanceof MemoryStoreRepository) {
            storeId0Mem = storeFacade.createStore("WillyTheChocolateDude", "Chocolate Factory", "Beer Sheva", "chocolate@gmail.com", "0541075403");
        }
        else {
            storeId0Db = storeFacade.createStore("WillyTheChocolateDude", "Chocolate Factory", "Beer Sheva", "chocolate@gmail.com", "0541075403");
        }
    }

    private void generateStore1(IStoreRepository repo)  {
        if(repo instanceof MemoryStoreRepository) {
            storeId1Mem = storeFacade.createStore("Mr. Krabs", "Krusty Krab", "Bikini Bottom", "krab@gmail.com", "0541085120");
        }
        else {
            storeId1Db = storeFacade.createStore("Mr. Krabs", "Krusty Krab", "Bikini Bottom", "krab@gmail.com", "0541085120");
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void createStoreSuccess(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);

        Set<Integer> expected0 = (repo instanceof MemoryStoreRepository) ? Set.of(storeId0Mem, storeId1Mem) : Set.of(storeId0Db, storeId1Db);
        assertEquals(expected0, storeFacade.getAllStoreIds());

        int id = storeFacade.createStore("WillyTheChocolateDude", "test", "test", "test@gmail.con", "11234567890");
        Set<Integer> expected1 = (repo instanceof MemoryStoreRepository) ? Set.of(storeId0Mem, storeId1Mem, id) : Set.of(storeId0Db, storeId1Db, id);
        assertEquals(expected1, storeFacade.getAllStoreIds());

        StoreDTO res = storeFacade.getStoreInfo("WillyTheChocolateDude", id);
        StoreDTO expected = new StoreDTO(repo.findStoreByID(id));
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void createStoreNoUser(IStoreRepository repo) {
        setUp(repo);

        NoSuchElementException expected = assertThrows(NoSuchElementException.class, () -> {
            storeFacade.createStore("Dana", "Krusty Krab", "Bikini Bottom", "krab@gmail.com", "0541085120");
        });

        String expectedMessage = Error.makeMemberUserDoesntExistError("Dana");
        assertEquals(expectedMessage, expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void createStoreUserNotLoggedIn(IStoreRepository repo) {
        setUp(repo);
        
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.createStore("Alice", "Krusty Krab", "Bikini Bottom", "krab@gmail.com", "0541085120");
        });

        String expectedMessage = Error.makeStoreUserHasToBeLoggedInError("Alice");
        assertEquals(expectedMessage, expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void createStoreAlreadyExists(IStoreRepository repo) {
        setUp(repo);
        
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.createStore("Mr. Krabs", "Chocolate Factory", "Beer Sheva", "chocolate@gmail.com", "0541075403");
        });

        String expectedMessage = Error.makeStoreWithNameAlreadyExistsError("Chocolate Factory");
        assertEquals(expectedMessage, expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void createStoreParamsNotValid(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.createStore("Mr. Krabs", "", "", "", "");
        });

        String expectedMessage = Error.makeStoreNotValidAspectError("", "store name");
        assertEquals(expectedMessage, expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addProductToStoreSuccess_Owner(IStoreRepository repo) {
        setUp(repo);

        int productId = storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "Mekupelet", 409, 3.5, "Chocolate", 4.2,2);
        assertEquals(409, storeFacade.getProductAmount(storeId0, productId));

        ProductDTO expected = new ProductDTO(productId, "Mekupelet", 3.5, "Chocolate", 4.2, 2,true,storeId0);
        assertEquals(expected, productFacade.getProductDTO(productId));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addProductToStore_StoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.addProductToStore("WillyTheChocolateDude", Integer.MAX_VALUE, "Mekupelet", 409, 3.5, "Chocolate", 4.2,2);
        });

        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addProductToStore_StoreNotActive(IStoreRepository repo) {
        setUp(repo);

        storeFacade.closeStore("WillyTheChocolateDude", storeId0);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "Mekupelet", 409, 3.5, "Chocolate", 4.2,2);
        });

        assertEquals(Error.makeStoreWithIdNotActiveError(storeId0), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addProductToStore_ParamsNotValid(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "", -57, -3.5, "", 8,2);
        });

        String expectedError = "Product information is invalid: Product name cannot be null or empty. Product price cannot be negative. Product category cannot be null or empty. Product rank must be between 0 and 5. ";
        assertEquals(expectedError, expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addProductToStore_NoPermissions(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.addProductToStore("Mr. Krabs", storeId0, "Mekupelet", 409, 3.5, "Chocolate", 4.2,2);
        });
        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.addProductToStore("Bob", storeId0, "Mekupelet", 409, 3.5, "Chocolate", 4.2,2);
        });
        IllegalArgumentException expected3 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.addProductToStore("Alice", storeId0, "Mekupelet", 409, 3.5, "Chocolate", 4.2,2);
        });
        NoSuchElementException expected4 = assertThrows(NoSuchElementException.class, () -> {
            storeFacade.addProductToStore("Dana", storeId0, "Mekupelet", 409, 3.5, "Chocolate", 4.2,2);
        });
        assertEquals(Error.makeStoreUserCannotAddProductError("Mr. Krabs", storeId0), expected1.getMessage());
        assertEquals(Error.makeStoreUserCannotAddProductError("Bob", storeId0), expected2.getMessage());
        assertEquals(Error.makeStoreUserCannotAddProductError("Alice", storeId0), expected3.getMessage());
        assertEquals(Error.makeMemberUserDoesntExistError("Dana"), expected4.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void deleteProductSuccess(IStoreRepository repo) {
        setUp(repo);

        int id = storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "Shokolad Parah", 1000, 5.5, "Chocolate", 4,2);
        storeFacade.deleteProduct("WillyTheChocolateDude", storeId0, id);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getProductAmount(storeId0, id);
        });
        assertEquals(Error.makeStoreProductDoesntExistError(storeId0, id), expected1.getMessage());
        assertFalse(productFacade.getProductDTO(id).isActiveProduct());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void deleteProduct_StoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.deleteProduct("WillyTheChocolateDude", Integer.MAX_VALUE, p0);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void deleteProduct_ProductDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.deleteProduct("WillyTheChocolateDude", storeId0, Integer.MAX_VALUE);
        });
        assertEquals(Error.makeStoreProductDoesntExistError(storeId0,Integer.MAX_VALUE), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void deleteProduct_NoPermission(IStoreRepository repo) {
        setUp(repo);

        int initialAmount = storeFacade.getProductAmount(storeId0, p0);
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.deleteProduct("Mr. Krabs", storeId0, p0);
        });
        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.deleteProduct("Bob", storeId0, p0);
        });
        IllegalArgumentException expected3 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.deleteProduct("Alice", storeId0, p0);
        });
        assertEquals(Error.makeStoreUserCannotDeleteProductError("Mr. Krabs", storeId0), expected1.getMessage());
        assertEquals(Error.makeStoreUserCannotDeleteProductError("Bob", storeId0), expected2.getMessage());
        assertEquals(Error.makeStoreUserCannotDeleteProductError("Alice", storeId0), expected3.getMessage());
        assertEquals(initialAmount, storeFacade.getProductAmount(storeId0, p0));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProductSuccess(IStoreRepository repo) {
        setUp(repo);

        storeFacade.updateProduct("WillyTheChocolateDude", storeId0, p0, "Cow Chocolate", 123, 10, "Chocolate", 4.8);
        ProductDTO expected1 = new ProductDTO(p0, "Cow Chocolate", 10, "Chocolate", 4.8, 2,true,storeId0);
        assertEquals(expected1, productFacade.getProductDTO(p0));
        storeFacade.updateProduct("WillyTheChocolateDude", storeId0, p0,"Shokolad Parah", 1000, 5.5, "Chocolate", 4,"");

        storeFacade.updateProduct("WillyTheChocolateDude", storeId0, p1, "Kit Kat", 100, 8, "Chocolate", 4.21);
        ProductDTO expected2 = new ProductDTO(p1, "Kit Kat", 8, "Chocolate", 4.21, 2,true, storeId0);
        assertEquals(expected2, productFacade.getProductDTO(p1));
        storeFacade.updateProduct("WillyTheChocolateDude", storeId0, p1,"Kif Kef", 982, 4.2, "Chocolate", 4.3,"");
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProduct_StoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProduct("WillyTheChocolateDude", Integer.MAX_VALUE, p0, "Cow Chocolate", 123, 10, "Chocolate", 4.8);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProduct_ProductDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProduct("WillyTheChocolateDude", storeId0, Integer.MAX_VALUE, "Cow Chocolate", 123, 10, "Chocolate", 4.8);
        });
        assertEquals(Error.makeStoreProductDoesntExistError(storeId0, Integer.MAX_VALUE), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProduct_NoPermission(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProduct("Mr. Krabs", storeId0, p0, "Cow Chocolate", 123, 10, "Chocolate", 4.8);
        });
        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProduct("Alice", storeId0, p0, "Cow Chocolate", 123, 10, "Chocolate", 4.8);
        });
        IllegalArgumentException expected3 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProduct("Bob", storeId0, p0, "Cow Chocolate", 123, 10, "Chocolate", 4.8);
        });
        assertEquals(Error.makeStoreUserCannotUpdateProductError("Mr. Krabs", storeId0), expected1.getMessage());
        assertEquals(Error.makeStoreUserCannotUpdateProductError("Alice", storeId0), expected2.getMessage());
        assertEquals(Error.makeStoreUserCannotUpdateProductError("Bob", storeId0), expected3.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProductAmountSuccess(IStoreRepository repo) {
        setUp(repo);

        storeFacade.updateProductAmount("WillyTheChocolateDude", storeId0, p0, 12);
        assertEquals(12, storeFacade.getProductAmount( storeId0, p0));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProductAmount_StoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        int initialAmount = storeFacade.getProductAmount( storeId0, p0);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProductAmount("WillyTheChocolateDude", Integer.MAX_VALUE, p0, 12);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected1.getMessage());

        assertEquals(initialAmount, storeFacade.getProductAmount( storeId0, p0));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProductAmount_ProductDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        int initialAmount = storeFacade.getProductAmount(storeId0, p0);
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProductAmount("WillyTheChocolateDude", storeId0, Integer.MAX_VALUE, 12);
        });
        assertEquals(Error.makeStoreProductDoesntExistError(storeId0, Integer.MAX_VALUE), expected1.getMessage());

        assertEquals(initialAmount, storeFacade.getProductAmount(storeId0, p0));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProductAmount_NegAmount(IStoreRepository repo) {
        setUp(repo);
        int initialAmount = storeFacade.getProductAmount( storeId0, p0);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProductAmount("WillyTheChocolateDude", storeId0, p0, -903);
        });
        assertEquals(Error.makeStoreIllegalProductAmountError(-903), expected1.getMessage());

        assertEquals(initialAmount, storeFacade.getProductAmount( storeId0, p0));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateProductAmount_NoPermissions(IStoreRepository repo) {
        setUp(repo);

        int initialAmount = storeFacade.getProductAmount( storeId0, p0);
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProductAmount("Mr. Krabs", storeId0, p0, 5);
        });
        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProductAmount("Bob", storeId0, p0, 5);
        });
        IllegalArgumentException expected3 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.updateProductAmount("Alice", storeId0, p0, 5);
        });
        assertEquals(Error.makeStoreUserCannotUpdateProductError("Mr. Krabs", storeId0), expected1.getMessage());
        assertEquals(Error.makeStoreUserCannotUpdateProductError("Bob", storeId0), expected2.getMessage());
        assertEquals(Error.makeStoreUserCannotUpdateProductError("Alice", storeId0), expected3.getMessage());
        assertEquals(initialAmount, storeFacade.getProductAmount( storeId0, p0));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void sendStoreOwnerRequestNotAccepted(IStoreRepository repo) {
        setUp(repo);

        storeFacade.sendStoreOwnerRequest("WillyTheChocolateDude", "Bob", storeId0);

        Set<String> expected = new HashSet<>();
        Collections.addAll(expected, "WillyTheChocolateDude");
        List<MemberDTO> res = storeFacade.getOwners("WillyTheChocolateDude", storeId0);
        Set<String> resUsernames = new HashSet<>();
        for(MemberDTO memberDTO : res)
            resUsernames.add(memberDTO.getUsername());
        assertEquals(expected, resUsernames);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void sendStoreOwnerRequestNoUser(IStoreRepository repo) {
        setUp(repo);

        NoSuchElementException expected = assertThrows(NoSuchElementException.class, () -> {
            storeFacade.sendStoreOwnerRequest("WillyTheChocolateDude", "Dana", storeId0);
        });

        assertEquals(Error.makeMemberUserDoesntExistError("Dana"), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addManagerPermissionNotManager(IStoreRepository repo) {
        setUp(repo);

        Set<Permission> permissions = new HashSet<>();
        permissions.add(Permission.ADD_PRODUCTS);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.addManagerPermission("WillyTheChocolateDude", "Bob", storeId0, permissions);
        });
        assertEquals(Error.makeMemberUserHasNoRoleError(), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void closeStore_Success(IStoreRepository repo) {
        setUp(repo);

        assertTrue(storeFacade.getStoreInfo("WillyTheChocolateDude", storeId0).isActive());
        storeFacade.closeStore("WillyTheChocolateDude", storeId0);
        assertFalse(storeFacade.getStoreInfo("WillyTheChocolateDude", storeId0).isActive());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void closeStore_StoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.closeStore("WillyTheChocolateDude", Integer.MAX_VALUE);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void closeStore_AlreadyClosed(IStoreRepository repo) {
        setUp(repo);

        storeFacade.closeStore("WillyTheChocolateDude", storeId0);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.closeStore("WillyTheChocolateDude", storeId0);
        });
        assertEquals(Error.makeStoreAlreadyClosedError(storeId0), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void closeStore_NoPermissions(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.closeStore("Mr. Krabs", storeId0);
        });
        assertEquals(Error.makeStoreUserCannotCloseStoreError("Mr. Krabs", storeId0), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getOwnersSuccess(IStoreRepository repo) {
        setUp(repo);

        Set<String> expected = new HashSet<>();
        Collections.addAll(expected, "WillyTheChocolateDude");
        List<MemberDTO> res = storeFacade.getOwners("WillyTheChocolateDude", storeId0);
        Set<String> resUsernames = new HashSet<>();
        for(MemberDTO memberDTO : res)
            resUsernames.add(memberDTO.getUsername());
        assertEquals(expected, resUsernames);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getOwnersNotOwner(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getOwners("Mr. Krabs", storeId0);
        });
        assertEquals(Error.makeStoreUserCannotGetRolesInfoError("Mr. Krabs", storeId0), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getOwnersStoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getOwners("WillyTheChocolateDude", Integer.MAX_VALUE);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getManagersNotOwner(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getManagers("Mr. Krabs", storeId0);
        });
        assertEquals(Error.makeStoreUserCannotGetRolesInfoError("Mr. Krabs", storeId0), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getManagersStoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getManagers("WillyTheChocolateDude", Integer.MAX_VALUE);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected1.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getStoreOrderHistory(IStoreRepository repo) throws Exception {
        setUp(repo);

        SupplyInterface supplyMock = Mockito.mock(SupplyInterface.class);
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        SupplyService.getInstance().setController(supplyMock);
        Mockito.when(supplyMock.canMakeOrder(Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(supplyMock.makeOrder(Mockito.any(), Mockito.any())).thenReturn("");
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);

        Map<Integer, Integer> amounts = new HashMap<>();
        amounts.put(p0, 5);
        amounts.put(p1, 15);
        amounts.put(p2, 12);
        List<ProductDataPrice> products = new LinkedList<>();
        products.add(new ProductDataPrice(p0, storeId0, "Shokolad Parah", 5, 5.5, 5.5));
        products.add( new ProductDataPrice(p1, storeId0, "Kif Kef", 15, 4.2, 4.2));
        products.add(new ProductDataPrice(p2, storeId0, "Klik Cariot", 12, 7, 7));

        userFacade.addProductToCart("Mr. Krabs", storeId0, p0, 5);
        userFacade.addProductToCart("Mr. Krabs", storeId0, p1, 15);
        userFacade.addProductToCart("Mr. Krabs", storeId0, p2, 12);

        CreditCardDTO creditCard = new CreditCardDTO("123", "456", new Date(), "291845322");
        AddressDTO address = new AddressDTO("Israel", "Tel Aviv", "A", "B", "123", "Mr. Krabs", "0541762645", "krab@gmail.com");
        userFacade.purchaseCart("Mr. Krabs", creditCard, address);

        List<ProductDataPrice> history = storeFacade.getStoreOrderHistory("WillyTheChocolateDude", storeId0);
        assertEquals(new HashSet<>(products), new HashSet<>(history));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getStoreInfoSuccess(IStoreRepository repo)  {
        setUp(repo);
        int id = storeFacade.createStore("WillyTheChocolateDude", "test1", "test", "test@gmail.com", "1234567890");
        StoreDTO dto1 = storeFacade.getStoreInfo("WillyTheChocolateDude", id);
        StoreDTO dto2 = new StoreDTO(repo.findStoreByID(id));
        assertEquals(dto1, dto2);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getStoreInfoStoreClosed(IStoreRepository repo) {
        setUp(repo);

        storeFacade.closeStore("WillyTheChocolateDude", storeId0);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getStoreInfo(null, storeId0);
        });
        assertEquals(Error.makeStoreWithIdNotActiveError(storeId0), expected1.getMessage());

        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getStoreInfo("Mr. Krabs", storeId0);
        });
        assertEquals(Error.makeStoreWithIdNotActiveError(storeId0), expected2.getMessage());

        StoreDTO res = storeFacade.getStoreInfo("WillyTheChocolateDude", storeId0);
        StoreDTO expected = generateStoreObject0().getStoreDTO();
        res.setOrderIds(new HashSet<>());
        expected.setOrderIds(new HashSet<>());
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getStoreInfoStoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getStoreInfo("WillyTheChocolateDude", Integer.MAX_VALUE);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductInfoSuccess(IStoreRepository repo) {
        setUp(repo);
        ProductDTO expected = new ProductDTO(p0, "Shokolad Parah", 5.5, "Chocolate", 4, 2,true,storeId0);
        ProductDTO res = storeFacade.getProductInfo("WillyTheChocolateDude", p0);
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductInfoStoreClosed(IStoreRepository repo) {
        setUp(repo);
        storeFacade.closeStore("WillyTheChocolateDude", storeId0);

        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getProductInfo(null, p0);
        });
        assertEquals(Error.makeStoreOfProductIsNotActiveError(p0), expected1.getMessage());

        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getProductInfo("Mr. Krabs", p0);
        });
        assertEquals(Error.makeStoreOfProductIsNotActiveError(p0), expected2.getMessage());

        ProductDTO expected = new ProductDTO(p0, "Shokolad Parah", 5.5, "Chocolate", 4, 2,true,storeId0);
        ProductDTO res = storeFacade.getProductInfo("WillyTheChocolateDude", p0);
        assertEquals(expected, res);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductInfoProductDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getProductInfo("WillyTheChocolateDude", 50);
        });
        assertEquals(Error.makeProductDoesntExistError(50), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductsInfoNotFiltered(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);

        ProductDTO product0 = new ProductDTO(p0, "Shokolad Parah", 5.5, "Chocolate", 4, 2,true,storeId0);
        ProductDTO product1 = new ProductDTO(p1, "Kif Kef", 4.2, "Chocolate", 4.3, 2,true,storeId0);
        ProductDTO product2 = new ProductDTO(p2, "Klik Cariot", 7, "Chocolate", 5, 2,true,storeId0);
        Map<ProductDTO, Integer> expected = new HashMap<>();
        expected.put(product0, storeFacade.getProductAmount(storeId0, p0));
        expected.put(product1, storeFacade.getProductAmount(storeId0, p1));
        expected.put(product2, storeFacade.getProductAmount(storeId0, p2));
        assertEquals(expected, storeFacade.getProductsInfoAndFilter(null, storeId0, null, null, -1, -1));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductsInfoFiltered(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        ProductDTO product1 = new ProductDTO(p1, "Kif Kef", 4.2, "Chocolate", 4.3, 2,true,storeId0);
        Map<ProductDTO, Integer> expected = new HashMap<>();
        expected.put(product1, storeFacade.getProductAmount(storeId0, p1));
        assertEquals(expected, storeFacade.getProductsInfoAndFilter(null, storeId0, "Kif Kef", null, 5, -1));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductsInfoDeleteProduct(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        int id = storeFacade.addProductToStore("WillyTheChocolateDude", storeId0, "Shokolad Parah", 1000, 5.5, "Chocolate", 4,2);
        storeFacade.deleteProduct("WillyTheChocolateDude", storeId0, id);
        ProductDTO product1 = new ProductDTO(p1, "Kif Kef", 4.2, "Chocolate", 4.3, 2,true,storeId0);
        Map<ProductDTO, Integer> expected = new HashMap<>();
        expected.put(product1, storeFacade.getProductAmount(storeId0, p1));
        assertEquals(expected, storeFacade.getProductsInfoAndFilter(null, storeId0, null, null, 5, -1));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductsInfoStoreClosed(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        int init0 = storeFacade.getProductAmount(storeId0, p0);
        int init1 = storeFacade.getProductAmount(storeId0, p1);
        int init2 = storeFacade.getProductAmount(storeId0, p2);
        storeFacade.closeStore("WillyTheChocolateDude", storeId0);
        ProductDTO product0 = new ProductDTO(p0, "Shokolad Parah", 5.5, "Chocolate", 4, 2,true,storeId0);
        ProductDTO product1 = new ProductDTO(p1, "Kif Kef", 4.2, "Chocolate", 4.3, 2,true,storeId0);
        ProductDTO product2 = new ProductDTO(p2, "Klik Cariot", 7, "Chocolate", 5, 2,true,storeId0);
        Map<ProductDTO, Integer> expected = new HashMap<>();
        expected.put(product0, init0);
        expected.put(product1, init1);
        expected.put(product2, init2);
        assertEquals(expected, storeFacade.getProductsInfoAndFilter("WillyTheChocolateDude", storeId0, null, null, -1, -1));

        IllegalArgumentException expectedError = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getProductsInfoAndFilter(null, storeId0, null, null, -1, -1);
        });
        assertEquals(Error.makeStoreWithIdNotActiveError(storeId0), expectedError.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductAmountStoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getProductAmount(Integer.MAX_VALUE, p0);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getProductAmountProductDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getProductAmount(storeId0, Integer.MAX_VALUE);
        });
        assertEquals(Error.makeStoreProductDoesntExistError(storeId0, Integer.MAX_VALUE), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addBuyPolicy(IStoreRepository repo) throws Exception {
        setUp(repo);
        List<BuyType> buyTypes = List.of(BuyType.immidiatePurchase);
        int policyId = buyPolicyFacade.createCategoryHolidayBuyPolicy("Fruits", List.of(BuyType.immidiatePurchase), "WillyTheChocolateDude");
        buyPolicyFacade.addBuyPolicyToStore("WillyTheChocolateDude", storeId0, policyId);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        BuyPolicy expectedBuyPolicy = new HolidayBuyPolicy(policyId, buyTypes, new CategorySubject("Fruits"));

        assertEquals(expectedBuyPolicy, resBuyPolicy);
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void addDiscountPolicy(IStoreRepository repo) throws Exception {
        setUp(repo);
        int conditionId = discountPolicyFacade.createMinProductCondition(10, p0, "WillyTheChocolateDude");
        int policyId = discountPolicyFacade.createOnProductConditionDiscountPolicy(50, p0, conditionId,"WillyTheChocolateDude");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);
        MinProductCondition expectedCondition = new MinProductCondition(conditionId, 10);
        expectedCondition.setOnProductName(p0); // set product id
        SimpleDiscount expectedDiscount = new SimpleDiscount(resDiscount.getId(), 50, expectedCondition);
        expectedDiscount.setOnProductID(p0);
        String discountJson = objectMapper.writeValueAsString(resDiscount);
        String expectedJson = objectMapper.writeValueAsString(expectedDiscount);
        assertEquals(expectedJson, discountJson);
        //discountPolicyFacade.removeDiscountPolicyFromStore(storeId0, policyId, "WillyTheChocolateDude");
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void checkCartOnHoliday(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        try (MockedStatic<HolidayBuyPolicy> mocked1 = mockStatic(HolidayBuyPolicy.class)) {
            mocked1.when(HolidayBuyPolicy::isHoliday).thenReturn(true);

            List<CartItemDTO> cart1 = new ArrayList<>();
            cart1.add(new CartItemDTO(storeId0, p0, 10));
            cart1.add(new CartItemDTO(storeId0, p2, 8));
            assertDoesNotThrow(() -> {storeFacade.checkCart(null, cart1);});

            List<CartItemDTO> cart2 = new ArrayList<>();
            cart2.add(new CartItemDTO(storeId0, p0, 10));
            cart2.add(new CartItemDTO(storeId0, p2, 18));
            IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
                storeFacade.checkCart(null, cart2);
            });
            String expectedError = Error.makeAmountBuyPolicyError(String.valueOf(p2), 3, 10) + "\n" +
                    Error.makeHolidayBuyPolicyError("Alcohol");
            assertEquals(expectedError, expected.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void checkCartNotHoliday(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        try (MockedStatic<HolidayBuyPolicy> mocked1 = mockStatic(HolidayBuyPolicy.class)) {
            mocked1.when(HolidayBuyPolicy::isHoliday).thenReturn(false);

            List<CartItemDTO> cart1 = new ArrayList<>();
            cart1.add(new CartItemDTO(storeId0, p0, 10));
            cart1.add(new CartItemDTO(storeId0, p2, 18));
            assertDoesNotThrow(() -> {storeFacade.checkCart(null, cart1);});
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void checkCartOnHolidayWithAlcohol(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        try (MockedStatic<HolidayBuyPolicy> mocked1 = mockStatic(HolidayBuyPolicy.class);
             MockedStatic<HourLimitBuyPolicy> mocked2 = mockStatic(HourLimitBuyPolicy.class)) {
            mocked1.when(HolidayBuyPolicy::isHoliday).thenReturn(true);
            mocked2.when(HourLimitBuyPolicy::getCurrTime).thenReturn(LocalTime.of(12, 0));

            List<CartItemDTO> cart1 = new ArrayList<>();
            cart1.add(new CartItemDTO(storeId0, p0, 10));
            cart1.add(new CartItemDTO(storeId0, p2, 8));
            cart1.add(new CartItemDTO(storeId1, p3, 1));

            assertDoesNotThrow(() -> {storeFacade.checkCart("Mr. Krabs", cart1);});

            cart1.add(new CartItemDTO(storeId0, p2, 18));
            cart1.add(new CartItemDTO(storeId1, p3, 1));

            IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
                storeFacade.checkCart(null, cart1);
            });

            String expectedErr =
                    Error.makeAmountBuyPolicyError(String.valueOf(p2), 3, 10) + "\n" +
                    Error.makeAgeLimitBuyPolicyError("Alcohol", 18, -1) + "\n" +
                    Error.makeHolidayBuyPolicyError("Alcohol");

            assertEquals(expectedErr, expected.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void checkCartWithAlcoholNotHoliday(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        try (MockedStatic<HolidayBuyPolicy> mocked1 = mockStatic(HolidayBuyPolicy.class);
             MockedStatic<HourLimitBuyPolicy> mocked2 = mockStatic(HourLimitBuyPolicy.class)) {
            mocked1.when(HolidayBuyPolicy::isHoliday).thenReturn(false);
            mocked2.when(HourLimitBuyPolicy::getCurrTime).thenReturn(LocalTime.of(12, 0));

            List<CartItemDTO> cart1 = new ArrayList<>();
            cart1.add(new CartItemDTO(storeId0, p0, 10));
            cart1.add(new CartItemDTO(storeId0, p2, 98));
            cart1.add(new CartItemDTO(storeId1, p3, 1));
            assertDoesNotThrow(() -> {storeFacade.checkCart("Mr. Krabs", cart1);});

            IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
                storeFacade.checkCart(null, cart1);
            });

            String expectedErr = Error.makeAgeLimitBuyPolicyError("Alcohol", 18, -1);
            assertEquals(expectedErr, expected.getMessage());
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void checkCartNotInStock(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        try (MockedStatic<HolidayBuyPolicy> mocked1 = mockStatic(HolidayBuyPolicy.class);
             MockedStatic<HourLimitBuyPolicy> mocked2 = mockStatic(HourLimitBuyPolicy.class)) {
            mocked1.when(HolidayBuyPolicy::isHoliday).thenReturn(false);
            mocked2.when(HourLimitBuyPolicy::getCurrTime).thenReturn(LocalTime.of(12, 0));

            List<CartItemDTO> cart1 = new ArrayList<>();
            cart1.add(new CartItemDTO(storeId0, p0, 100000));
            cart1.add(new CartItemDTO(storeId0, p2, 5));
            cart1.add(new CartItemDTO(storeId1, p3, 100000));

            IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
                storeFacade.checkCart("Mr. Krabs", cart1);
            });

            String expectedErr1 = Error.makeNotEnoughInStcokError(storeId0, p0, 100000, storeFacade.getProductAmount(storeId0, p0)) + "\n";
            expectedErr1 += Error.makeNotEnoughInStcokError(storeId1, p3, 100000, storeFacade.getProductAmount(storeId1, p3));
            String expectedErr2 = Error.makeNotEnoughInStcokError(storeId1, p3, 100000, storeFacade.getProductAmount(storeId1, p3)) + "\n";
            expectedErr2 += Error.makeNotEnoughInStcokError(storeId0, p0, 100000, storeFacade.getProductAmount(storeId0, p0));
            boolean res = expected.getMessage().equals(expectedErr1) || expected.getMessage().equals(expectedErr2);

            assertTrue(res);
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getIsOwnerSuccess(IStoreRepository repo) {
        setUp(repo);
        assertTrue(storeFacade.getIsOwner("WillyTheChocolateDude", storeId0, "WillyTheChocolateDude"));
        assertFalse(storeFacade.getIsOwner("WillyTheChocolateDude", storeId0, "Mr. Krabs"));
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getIsOwnerStoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getIsOwner("Mr. Krabs", Integer.MAX_VALUE, "WillyTheChocolateDude");
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void hasProductInStock(IStoreRepository repo) {
        setUp(repo);
        assertTrue(storeFacade.hasProductInStock(storeId0, p0, 1000));
        assertTrue(storeFacade.hasProductInStock(storeId0, p1, 982));
        assertTrue(storeFacade.hasProductInStock(storeId0, p2, 312));

        assertFalse(storeFacade.hasProductInStock(storeId0, p0, 1001));
        assertFalse(storeFacade.hasProductInStock(storeId0, p1, 983));
        assertFalse(storeFacade.hasProductInStock(storeId0, p2, 313));

        assertFalse(storeFacade.hasProductInStock(storeId0, Integer.MAX_VALUE, 0));

    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void hasProductInStockStoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.hasProductInStock(Integer.MAX_VALUE, p0, 1001);
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void getIsManagerStoreDoesNotExist(IStoreRepository repo) {
        setUp(repo);
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            storeFacade.getIsManager("Mr. Krabs", Integer.MAX_VALUE, "WillyTheChocolateDude");
        });
        assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), expected.getMessage());
    }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void updateStockSuccess(IStoreRepository repo) throws JsonProcessingException {
        setUp(repo);
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(LocalTime.of(12, 0));

            int initial0 = storeFacade.getProductAmount(storeId0, p0);
            int initial1 = storeFacade.getProductAmount(storeId0, p1);
            int initial2 = storeFacade.getProductAmount(storeId0, p2);
            int initial3 = storeFacade.getProductAmount(storeId1, p3);
            int initial4 = storeFacade.getProductAmount(storeId1, p4);

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(storeId0, p0, 1));
            cart.add(new CartItemDTO(storeId0, p1, 1));
            cart.add(new CartItemDTO(storeId0, p2, 1));
            cart.add(new CartItemDTO(storeId1, p3, 1));
            cart.add(new CartItemDTO(storeId1, p4, 1));

            storeFacade.updateStock("WillyTheChocolateDude", cart);

            int after0 = storeFacade.getProductAmount(storeId0, p0);
            int after1 = storeFacade.getProductAmount(storeId0, p1);
            int after2 = storeFacade.getProductAmount(storeId0, p2);
            int after3 = storeFacade.getProductAmount(storeId1, p3);
            int after4 = storeFacade.getProductAmount(storeId1, p4);

            assertEquals(after0, initial0 - 1);
            assertEquals(after1, initial1 - 1);
            assertEquals(after2, initial2 - 1);
            assertEquals(after3, initial3 - 1);
            assertEquals(after4, initial4 - 1);
        }
    }

     @ParameterizedTest
    @MethodSource("repositoryProvider")
     void updateStockFail(IStoreRepository repo) throws JsonProcessingException {
         // only checking one fail case because all of them are checked in check cart
         setUp(repo);
         try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
             mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(LocalTime.of(12, 0));

             int initial0 = storeFacade.getProductAmount(storeId0, p0);
             int initial1 = storeFacade.getProductAmount(storeId0, p1);
             int initial2 = storeFacade.getProductAmount(storeId0, p2);
             int initial3 = storeFacade.getProductAmount(storeId1, p3);
             int initial4 = storeFacade.getProductAmount(storeId1, p4);

             List<CartItemDTO> cart = new ArrayList<>();
             cart.add(new CartItemDTO(storeId0, p0, 10000));
             cart.add(new CartItemDTO(storeId0, p1, 1));
             cart.add(new CartItemDTO(storeId0, p2, 1));
             cart.add(new CartItemDTO(storeId1, p3, 10000));
             cart.add(new CartItemDTO(storeId1, p4, 1));

             String expectedError1 = Error.makeNotEnoughInStcokError(storeId0, p0, 10000, initial0) + "\n" +
                     Error.makeNotEnoughInStcokError(storeId1, p3, 10000, initial3);
             String expectedError2 = Error.makeNotEnoughInStcokError(storeId1, p3, 10000, initial3) + "\n" +
                     Error.makeNotEnoughInStcokError(storeId0, p0, 10000, initial0);

             IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
                 storeFacade.updateStock("WillyTheChocolateDude", cart);
             });
             assertTrue(expected.getMessage().equals(expectedError1) || expected.getMessage().equals(expectedError2));

             int after0 = storeFacade.getProductAmount(storeId0, p0);
             int after1 = storeFacade.getProductAmount(storeId0, p1);
             int after2 = storeFacade.getProductAmount(storeId0, p2);
             int after3 = storeFacade.getProductAmount(storeId1, p3);
             int after4 = storeFacade.getProductAmount(storeId1, p4);

             assertEquals(after0, initial0);
             assertEquals(after1, initial1);
             assertEquals(after2, initial2);
             assertEquals(after3, initial3);
             assertEquals(after4, initial4);
         }
     }

    @ParameterizedTest
    @MethodSource("repositoryProvider")
    void calculatePrice(IStoreRepository repo) throws Exception {
        setUp(repo);

        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(storeId0, p0, 10));
        cart.add(new CartItemDTO(storeId0, p1, 30));
        cart.add(new CartItemDTO(storeId0, p2, 50));
        cart.add(new CartItemDTO(storeId1, p3, 10));
        cart.add(new CartItemDTO(storeId1, p4, 10));

        int conditionId = discountPolicyFacade.createMinProductOnStoreCondition(30, "WillyTheChocolateDude");
        int policyId = discountPolicyFacade.createOnStoreConditionDiscountPolicy(50, conditionId, "WillyTheChocolateDude");
        discountPolicyFacade.addDiscountPolicyToStore(storeId0, policyId, "WillyTheChocolateDude");

        List<ProductDataPrice> res = storeFacade.calculatePrice("WillyTheChocolateDude", cart);
        Set<ProductDataPrice> expected = new HashSet<>();
        expected.add(new ProductDataPrice(p0, storeId0, "Shokolad Parah", 10, 5.5, 2.75));
        expected.add(new ProductDataPrice(p1, storeId0, "Kif Kef", 30, 4.2, 2.1));
        expected.add(new ProductDataPrice(p2, storeId0, "Klik Cariot", 50, 7, 3.5));
        expected.add(new ProductDataPrice(p3, storeId1, "Beer", 10, 12, 12));
        expected.add(new ProductDataPrice(p4, storeId1, "Wine", 10, 50, 50));

        assertEquals(expected, new HashSet<>(res));

        discountPolicyFacade.removeDiscountPolicyFromStore(storeId0, policyId, "WillyTheChocolateDude");
    }


}