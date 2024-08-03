package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.MemoryConditionRepository;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.MemoryDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.orders.IOrderRepository;
import com.sadna.sadnamarket.domain.orders.MemoryOrderRepository;
import com.sadna.sadnamarket.domain.orders.OrderFacade;
import com.sadna.sadnamarket.domain.products.IProductRepository;
import com.sadna.sadnamarket.domain.products.MemoryProductRepository;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.stores.IStoreRepository;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.domain.users.*;
import com.sadna.sadnamarket.service.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockedStatic;
import org.mockito.MockitoAnnotations;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

@Transactional
class BuyPolicyFacadeTest {
    private StoreFacade storeFacade;
    private AuthFacade authFacade;
    private UserFacade userFacade;
    private ProductFacade productFacade;
    private BuyPolicyFacade buyPolicyFacade;
    private IBuyPolicyRepository buyPolicyRepository;
    private DiscountPolicyFacade discountPolicyFacade;
    ObjectMapper objectMapper = new ObjectMapper();
    private SimpleFilterProvider idFilter;
    private final List<BuyType> buyTypes = new LinkedList<>();
    private final String ownerUsername = "WillyTheChocolateDude";
    private final LocalTime noon = LocalTime.of(12, 0);
    private final LocalTime behazotImHanadiBady = LocalTime.of(0, 0);
    private static IBuyPolicyRepository nextRepo = new MemoryBuyPolicyRepository();
    private int example1;
    private int example2;
    static Stream<IBuyPolicyRepository> repositoryStream() {
        return Stream.of(new HibernateBuyPolicyRepository(), new MemoryBuyPolicyRepository());
    }


    @BeforeEach
    void setUp() throws JsonProcessingException {
        this.idFilter = new SimpleFilterProvider().addFilter("idFilter", SimpleBeanPropertyFilter.serializeAllExcept("id"));
        objectMapper.registerModule(new JavaTimeModule());

        setUpFacades(nextRepo);
        buyPolicyFacade.clear();
        generateUsers();
        generateStore0();
        generateExamplePolicies();


        MockitoAnnotations.openMocks(this);
    }

    private void generateUsers() {
        authFacade.register("Mr. Krabs", "654321", "Eugene", "Krabs", "eugene@gmail.com", "0521957682", LocalDate.of(1942, 11, 30));
        authFacade.register(ownerUsername, "123456", "Willy", "Wonka", "willy@gmail.com", "0541095600", LocalDate.of(1995, 12, 12));
        authFacade.login(ownerUsername, "123456");
        authFacade.register("FourSeasonsOrlandoBaby", "654321", "Baby", "Orlando", "baby@gmail.com", "0528997287", LocalDate.of(2006, 8, 19));
        authFacade.login("FourSeasonsOrlandoBaby", "654321");
    }

    private void setUpFacades(IBuyPolicyRepository repo) {
        IOrderRepository orderRepo = new MemoryOrderRepository();
        OrderFacade orderFacade = new OrderFacade(orderRepo);

        IStoreRepository storeRepo = new MemoryStoreRepository();
        this.storeFacade = new StoreFacade(storeRepo);

        IUserRepository userRepo = new MemoryRepo();
        this.userFacade = new UserFacade(userRepo, storeFacade, orderFacade);

        IProductRepository productRepo = new MemoryProductRepository();
        this.productFacade = new ProductFacade(productRepo);

        this.buyPolicyRepository = repo;
        this.buyPolicyFacade = new BuyPolicyFacade(buyPolicyRepository);
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
    }

    private void generateStore0()  {
        storeFacade.createStore(ownerUsername, "Chocolate Factory", "Beer Sheva", "chocolate@gmail.com", "0541075403");

        storeFacade.addProductToStore(ownerUsername, 0, "Shokolad Parah", 1000, 5.5, "Chocolate", 4,2);
        storeFacade.addProductToStore(ownerUsername, 0, "Beer", 990, 15, "Alcohol", 4,0.3);
    }

    private void generateStore1()  {
        LocalTime openingHour = LocalTime.of(10, 0);
        LocalTime closingHour = LocalTime.of(21, 0);
        LocalTime fridayClosingHour = LocalTime.of(14, 0);
        LocalTime[] openingHours = new LocalTime[]{openingHour, openingHour, openingHour, openingHour, openingHour, openingHour, null};
        LocalTime[] closingHours = new LocalTime[]{closingHour, closingHour, closingHour, closingHour, closingHour, fridayClosingHour, null};
        storeFacade.createStore("FourSeasonsOrlandoBaby", "Shilav", "Beer Sheva", "shilav@gmail.com", "0541075403");

        storeFacade.addProductToStore("FourSeasonsOrlandoBaby", 1, "P2", 1000, 5.5, "Chocolate", 4,2);
        storeFacade.addProductToStore("FourSeasonsOrlandoBaby", 1, "P3", 1000, 5.5, "Chocolate", 4,2);
        storeFacade.addProductToStore("FourSeasonsOrlandoBaby", 1, "P4", 1000, 5.5, "Chocolate", 4,2);
        storeFacade.addProductToStore("FourSeasonsOrlandoBaby", 1, "P5", 1000, 5.5, "Chocolate", 4,2);
        storeFacade.addProductToStore("FourSeasonsOrlandoBaby", 1, "P6", 1000, 5.5, "Chocolate", 4,2);
    }

    private void generateExamplePolicies() throws JsonProcessingException {
        example1 = buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Alcohol",buyTypes,21,-1,ownerUsername);
        example2 = buyPolicyFacade.createCategoryRoshChodeshBuyPolicy("Alcohol",buyTypes,ownerUsername);
    }

    private String toJson(BuyPolicy buyPolicy) throws JsonProcessingException {
        return buyPolicy.getClass().getName() + "-" + objectMapper.writer(idFilter).writeValueAsString(buyPolicy);

    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void getBuyPolicySuccess(IBuyPolicyRepository repo) throws Exception {
        BuyPolicy res0BuyPolicy = buyPolicyFacade.getBuyPolicy(example1);
        BuyPolicy res1BuyPolicy = buyPolicyFacade.getBuyPolicy(example2);
        String res0Json = toJson(res0BuyPolicy);
        String res1Json = toJson(res1BuyPolicy);

        BuyPolicy expected0 = new AgeLimitBuyPolicy(example1, buyTypes , new CategorySubject("Alcohol"), 21, -1);
        BuyPolicy expected1 = new RoshChodeshBuyPolicy(example2, buyTypes, new CategorySubject("Alcohol"));
        String expected0Json = toJson(expected0);
        String expected1Json = toJson(expected1);

        assertEquals(expected0Json, res0Json);
        assertEquals(expected1Json, res1Json);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void getBuyPolicyNoPolicyId(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.getBuyPolicy(example2+100);
        });

        String expectedMessage = Error.makeBuyPolicyWithIdDoesNotExistError(example2+100);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductKgBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createProductKgBuyPolicy(0, buyTypes, 5, 10, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy expected = new KgLimitBuyPolicy(policyId, buyTypes, new ProductSubject(0), 5, 10);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductKgBuyPolicyNoProduct(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductKgBuyPolicy(9, buyTypes, 5, 10, ownerUsername);
        });

        String expectedMessage = Error.makeProductDoesntExistError(9);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductKgBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductKgBuyPolicy(0, buyTypes, 5, 10, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductKgBuyPolicyInvalidKg(IBuyPolicyRepository repo) {
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductKgBuyPolicy(0, buyTypes, 12, 10, ownerUsername);
        });
        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductKgBuyPolicy(0, buyTypes, -5, 10, ownerUsername);
        });

        String expectedMessage1 = Error.makeBuyPolicyParamsError("kg limit", String.valueOf(12), String.valueOf(10));
        String expectedMessage2 = Error.makeBuyPolicyParamsError("kg limit", String.valueOf(-5), String.valueOf(10));

        assertEquals(expectedMessage1, expected1.getMessage());
        assertEquals(expectedMessage2, expected2.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductAmountBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createProductAmountBuyPolicy(0, buyTypes, 5, 10, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy expected = new AmountBuyPolicy(policyId, buyTypes, new ProductSubject(0), 5, 10);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductAmountBuyPolicyNoProduct(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductAmountBuyPolicy(9, buyTypes, 5, 10, ownerUsername);
        });

        String expectedMessage = Error.makeProductDoesntExistError(9);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductAmountBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductAmountBuyPolicy(0, buyTypes, 5, 10, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createProductAmountBuyPolicyInvalidAmount(IBuyPolicyRepository repo) {
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductAmountBuyPolicy(0, buyTypes, 12, 10, ownerUsername);
        });
        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createProductAmountBuyPolicy(0, buyTypes, -5, 10, ownerUsername);
        });

        String expectedMessage1 = Error.makeBuyPolicyParamsError("amount", String.valueOf(12), String.valueOf(10));
        String expectedMessage2 = Error.makeBuyPolicyParamsError("amount", String.valueOf(-5), String.valueOf(10));

        assertEquals(expectedMessage1, expected1.getMessage());
        assertEquals(expectedMessage2, expected2.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryAgeLimitBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Fruits", buyTypes, 5, 10, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy expected = new AgeLimitBuyPolicy(policyId, buyTypes, new CategorySubject("Fruits"), 5, 10);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryAgeLimitBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Fruits", buyTypes, 5, 10, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryAgeLimitBuyPolicyInvalidAmount(IBuyPolicyRepository repo) {
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Fruit", buyTypes, 12, 10, ownerUsername);
        });
        IllegalArgumentException expected2 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Fruit", buyTypes, -5, 10, ownerUsername);
        });
        IllegalArgumentException expected3 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryAgeLimitBuyPolicy("", buyTypes, 3, 10, ownerUsername);
        });
        IllegalArgumentException expected4 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryAgeLimitBuyPolicy(null, buyTypes, 3, 10, ownerUsername);
        });

        String expectedMessage1 = Error.makeBuyPolicyParamsError("age limit", String.valueOf(12), String.valueOf(10));
        String expectedMessage2 = Error.makeBuyPolicyParamsError("age limit", String.valueOf(-5), String.valueOf(10));
        String expectedMessage3 = Error.makeEmptyCategoryError();
        String expectedMessage4 = Error.makeEmptyCategoryError();

        assertEquals(expectedMessage1, expected1.getMessage());
        assertEquals(expectedMessage2, expected2.getMessage());
        assertEquals(expectedMessage3, expected3.getMessage());
        assertEquals(expectedMessage4, expected4.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryHourLimitBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createCategoryHourLimitBuyPolicy("Fans", buyTypes, LocalTime.of(7,0),  LocalTime.of(16,0), ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy expected = new HourLimitBuyPolicy(policyId, buyTypes, new CategorySubject("Fans"), LocalTime.of(7,0),  LocalTime.of(16,0));
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryHourLimitBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryHourLimitBuyPolicy("Fans", buyTypes, LocalTime.of(7,0),  LocalTime.of(16,0), "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryHourLimitBuyPolicyInvalidHours(IBuyPolicyRepository repo) {
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryHourLimitBuyPolicy("Fans", buyTypes, LocalTime.of(22,0), LocalTime.of(13,0), ownerUsername);
        });

        String expectedMessage1 = Error.makeBuyPolicyParamsError("hour limit", LocalTime.of(22,0).toString(), LocalTime.of(13,0).toString());

        assertEquals(expectedMessage1, expected1.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryRoshChodeshBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createCategoryRoshChodeshBuyPolicy("Shoes", buyTypes, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy expected = new RoshChodeshBuyPolicy(policyId, buyTypes, new CategorySubject("Shoes"));
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryRoshChodeshBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryRoshChodeshBuyPolicy("Shoes", buyTypes, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryHolidayBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createCategoryHolidayBuyPolicy("Shoes", buyTypes, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy expected = new HolidayBuyPolicy(policyId, buyTypes, new CategorySubject("Shoes"));
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createCategoryHolidayBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createCategoryHolidayBuyPolicy("Shoes", buyTypes, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createAndBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createAndBuyPolicy(example1, example2, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy p0 = buyPolicyFacade.getBuyPolicy(example1);
        BuyPolicy p1 = buyPolicyFacade.getBuyPolicy(example2);
        BuyPolicy expected = new AndBuyPolicy(policyId, p0, p1);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createAndBuyPolicyPolicyDoesNotExist(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createAndBuyPolicy(10, example2, ownerUsername);
        });

        String expectedMessage = Error.makeBuyPolicyWithIdDoesNotExistError(10);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createAndBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createAndBuyPolicy(example1, example2, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createOrBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createOrBuyPolicy(example1, example2, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy p0 = buyPolicyFacade.getBuyPolicy(example1);
        BuyPolicy p1 = buyPolicyFacade.getBuyPolicy(example2);
        BuyPolicy expected = new OrBuyPolicy(policyId, p0, p1);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createOrBuyPolicyPolicyDoesNotExist(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createOrBuyPolicy(10, example2, ownerUsername);
        });

        String expectedMessage = Error.makeBuyPolicyWithIdDoesNotExistError(10);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createOrBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createOrBuyPolicy(example1, example2, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createConditioningBuyPolicySuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createConditioningBuyPolicy(example1, example2, ownerUsername);

        BuyPolicy resBuyPolicy = buyPolicyFacade.getBuyPolicy(policyId);
        String resJson = toJson(resBuyPolicy);

        BuyPolicy p0 = buyPolicyFacade.getBuyPolicy(example1);
        BuyPolicy p1 = buyPolicyFacade.getBuyPolicy(example2);
        BuyPolicy expected = new ConditioningBuyPolicy(policyId, p0, p1);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createConditioningBuyPolicyPolicyDoesNotExist(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createConditioningBuyPolicy(10, 1, ownerUsername);
        });

        String expectedMessage = Error.makeBuyPolicyWithIdDoesNotExistError(10);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createConditioningBuyPolicyNoPermissions(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.createConditioningBuyPolicy(example1, example2, "Mr. Krabs");
        });

        String expectedMessage = Error.makeUserCanNotCreateBuyPolicyError("Mr. Krabs");
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void addBuyPolicyToStoreSuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createOrBuyPolicy( example1, example2, ownerUsername);
        assertFalse(buyPolicyFacade.hasPolicy(0, policyId));
        buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 0, policyId);
        assertTrue(buyPolicyFacade.hasPolicy(0, policyId));
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void addBuyPolicyToStoreStoreDoesNotExist(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createOrBuyPolicy(example1, example2, ownerUsername);

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 1, policyId);
        });

        String expectedMessage = Error.makeStoreNoStoreWithIdError(1);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void addBuyPolicyToStorePolicyDoesNotExist(IBuyPolicyRepository repo) throws JsonProcessingException {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 0, 5);
        });

        String expectedMessage = Error.makeBuyPolicyWithIdDoesNotExistError(5);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void addBuyPolicyToStorePolicyAlreadyExists(IBuyPolicyRepository repo) throws JsonProcessingException {
        buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 0, example1);
        buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 0, example2);
        IllegalArgumentException expected0 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 0, example1);
        });
        IllegalArgumentException expected1 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 0, example2);
        });

        String expectedMessage0 = Error.makeBuyPolicyAlreadyExistsError(example1);
        String expectedMessage1 = Error.makeBuyPolicyAlreadyExistsError(example2);

        assertEquals(expectedMessage0, expected0.getMessage());
        assertEquals(expectedMessage1, expected1.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void addBuyPolicyToStoreNoPermission(IBuyPolicyRepository repo) throws JsonProcessingException {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.addBuyPolicyToStore("Mr. Krabs", 0, example1);
        });

        String expectedMessage = Error.makeStoreUserCannotAddBuyPolicyError("Mr. Krabs", 0);

        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void removePolicyFromStoreSuccess(IBuyPolicyRepository repo) throws JsonProcessingException {
        int policyId = buyPolicyFacade.createOrBuyPolicy(example1, example2, ownerUsername);
        buyPolicyFacade.addBuyPolicyToStore(ownerUsername, 0, policyId);
        assertTrue(buyPolicyFacade.hasPolicy(0, policyId));
        buyPolicyFacade.removePolicyFromStore(ownerUsername, 0, policyId);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void removePolicyFromStoreStoreDoesNotExist(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.removePolicyFromStore(ownerUsername, 1, example1);
        });

        String expectedMessage = Error.makeStoreNoStoreWithIdError(1);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void removePolicyFromStorePolicyDoesNotExist(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.removePolicyFromStore(ownerUsername, 0, 5);
        });

        String expectedMessage = Error.makeBuyPolicyWithIdDoesNotExistError(5);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void removePolicyFromStoreNoPermission(IBuyPolicyRepository repo) {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.removePolicyFromStore("Mr. Krabs", 0, example1);
        });

        String expectedMessage = Error.makeStoreUserCannotRemoveBuyPolicyError("Mr. Krabs", 0);

        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void removePolicyFromStoreLawBuyPolicy(IBuyPolicyRepository repo) {
        buyPolicyFacade.addLawBuyPolicyToStore(ownerUsername,0,example1);
        IllegalArgumentException expected0 = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.removePolicyFromStore(ownerUsername, 0, example1);
        });

        String expectedMessage0 = Error.makeCanNotRemoveLawBuyPolicyError(example1);

        assertEquals(expectedMessage0, expected0.getMessage());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void canBuyNoAlcohol(IBuyPolicyRepository repo) {
        List<CartItemDTO> cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 5));
        Set<String> expected = new HashSet<>();
        Set<String> res = buyPolicyFacade.canBuy(0, cart, null);
        assertEquals(expected, res);
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void canBuyAdultTriesToBuyAlcoholAtNoon(IBuyPolicyRepository repo) {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(noon);

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 1, 5));

            Set<String> expected = new HashSet<>();
            Set<String> res = buyPolicyFacade.canBuy(0, cart, "Mr. Krabs");
            assertEquals(expected, res);
            this.nextRepo = repo;
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void canBuyAdultTriesToBuyAlcoholAtMidnight(IBuyPolicyRepository repo) {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(behazotImHanadiBady);

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 1, 5));

            Set<String> expected = new HashSet<>();
            expected.add(Error.makeHourLimitBuyPolicyError("Alcohol", LocalTime.of(6, 0), LocalTime.of(23, 0)));
            Set<String> res = buyPolicyFacade.canBuy(0, cart, "Mr. Krabs");
            assertEquals(expected, res);
            this.nextRepo = repo;
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void canBuyBabyTriesToBuyAlcoholAtNoon(IBuyPolicyRepository repo) {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(noon);

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 1, 5));

            Set<String> expected = new HashSet<>();
            expected.add(Error.makeAgeLimitBuyPolicyError("Alcohol", 18, -1));
            Set<String> res = buyPolicyFacade.canBuy(0, cart, "FourSeasonsOrlandoBaby");
            assertEquals(expected, res);
            this.nextRepo = repo;
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void canBuyBabyTriesToBuyAlcoholAtMidnight(IBuyPolicyRepository repo) {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(behazotImHanadiBady);

            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 1, 5));

            Set<String> expected = new HashSet<>();
            expected.add(Error.makeHourLimitBuyPolicyError("Alcohol", LocalTime.of(6, 0), LocalTime.of(23, 0)));
            expected.add(Error.makeAgeLimitBuyPolicyError("Alcohol", 18, -1));
            Set<String> res = buyPolicyFacade.canBuy(0, cart, "FourSeasonsOrlandoBaby");
            assertEquals(expected, res);
            this.nextRepo = repo;
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void canBuyGuestTriesToBuyAlcoholAtNoon(IBuyPolicyRepository repo) {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(noon);


            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 1, 5));

            Set<String> expected = new HashSet<>();
            expected.add(Error.makeAgeLimitBuyPolicyError("Alcohol", 18, -1));
            Set<String> res = buyPolicyFacade.canBuy(0, cart, null);
            assertEquals(expected, res);
            this.nextRepo = repo;
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void canBuyGuestTriesToBuyAlcoholAtMidnight(IBuyPolicyRepository repo) {
        try (MockedStatic<HourLimitBuyPolicy> mocked = mockStatic(HourLimitBuyPolicy.class)) {
            mocked.when(HourLimitBuyPolicy::getCurrTime).thenReturn(behazotImHanadiBady);


            List<CartItemDTO> cart = new ArrayList<>();
            cart.add(new CartItemDTO(0, 1, 5));

            Set<String> expected = new HashSet<>();
            expected.add(Error.makeHourLimitBuyPolicyError("Alcohol", LocalTime.of(6, 0), LocalTime.of(23, 0)));
            expected.add(Error.makeAgeLimitBuyPolicyError("Alcohol", 18, -1));
            Set<String> res = buyPolicyFacade.canBuy(0, cart, null);
            assertEquals(expected, res);
            this.nextRepo = repo;
        }
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void createPolicyFlyweight(IBuyPolicyRepository repo) throws JsonProcessingException {
        Set<Integer> current = buyPolicyRepository.getAllPolicyIds();
        Set<Integer> expected1 = new HashSet<>(current);
        assertEquals(expected1, buyPolicyRepository.getAllPolicyIds()); // default

        // adding a policy that already exists
        buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Alcohol", List.of(BuyType.immidiatePurchase), 18, -1, "WillyTheChocolateDude");
        assertEquals(expected1, buyPolicyRepository.getAllPolicyIds());

        // adding a policy that does not exist
        buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Chocolate", List.of(BuyType.immidiatePurchase), 18, -1, "WillyTheChocolateDude");
        assertNotEquals(expected1, buyPolicyRepository.getAllPolicyIds());
        this.nextRepo = repo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    void addPolicyToStoreProductsNotInStore(IBuyPolicyRepository repo) throws JsonProcessingException {
        generateStore1();

        int policyId1 = buyPolicyFacade.createProductKgBuyPolicy(0, List.of(BuyType.immidiatePurchase), 18, -1, "WillyTheChocolateDude");
        int policyId2 = buyPolicyFacade.createProductKgBuyPolicy(4, List.of(BuyType.immidiatePurchase), 18, -1, "WillyTheChocolateDude");
        int policyId3 = buyPolicyFacade.createOrBuyPolicy(policyId1, policyId2, "WillyTheChocolateDude");

        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class, () -> {
            buyPolicyFacade.addBuyPolicyToStore("WillyTheChocolateDude", 0, policyId3);
        });

        String expectedMessage = Error.makePolicyProductsNotInStore(0, Set.of(0, 4), policyId3);
        assertEquals(expectedMessage, expected.getMessage());
        this.nextRepo = repo;
    }
}