package com.sadna.sadnamarket.domain.DisocuntPolicys;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;

import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicyFacade;
import com.sadna.sadnamarket.domain.buyPolicies.MemoryBuyPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.*;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.*;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.orders.MemoryOrderRepository;
import com.sadna.sadnamarket.domain.orders.OrderFacade;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemoryRepo;
import com.sadna.sadnamarket.domain.users.UserFacade;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.params.ParameterizedTest;
import org.junit.jupiter.params.provider.Arguments;
import org.junit.jupiter.params.provider.MethodSource;
import org.mockito.MockitoAnnotations;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.concurrent.TimeoutException;
import java.util.stream.Stream;

import static org.junit.jupiter.api.Assertions.*;

public class DiscountPolicyFacadeTest{
    Map<Integer, ProductDTO> productDTOMap;
    Condition conditionTrue1;
    Condition conditionTrue2;
    Condition conditionFalse1;
    Condition conditionFalse2;

    int onStore10discountTrue1ID;
    int onCategoryDairy10discountTrue1ID;
    int onStore10discountFalse1ID;
    int onCategoryDairy10discountFalse1ID;

    ObjectMapper objectMapper = new ObjectMapper();

    List<CartItemDTO> cart;

    protected IConditionRespository conditionRepository;
    protected IDiscountPolicyRepository discountPolicyRepository;

    private StoreFacade storeFacade;
    private AuthFacade authFacade;
    private ProductFacade productFacade;
    protected DiscountPolicyFacade discountPolicyFacade;

    private static IDiscountPolicyRepository nextRepoDiscountPolicy = new MemoryDiscountPolicyRepository();
    private static IConditionRespository nextRepoCondition = new MemoryConditionRepository();

    private String toJson(Object object) throws JsonProcessingException {
        return object.getClass().getName() + "-" + objectMapper.writeValueAsString(object);

    }
    static Stream<Arguments> repositoryStream() {
        return Stream.of(Arguments.of(new HibernateDiscountPolicyRepository(), new HibernateConditionRepository()), Arguments.of(new MemoryDiscountPolicyRepository(), new MemoryConditionRepository()));
    }

    @BeforeEach
    public void setUp() throws Exception {
        setUpFacades(nextRepoDiscountPolicy, nextRepoCondition);
        discountPolicyFacade.clear();
        generateUsers();
        generateStore0();
        generateConditionAndPolicies();
        MockitoAnnotations.openMocks(this);
    }

    private void setUpFacades(IDiscountPolicyRepository nextRepoDiscountPolicy, IConditionRespository nextRepoCondition) {
        conditionRepository = nextRepoCondition;
        discountPolicyRepository = nextRepoDiscountPolicy;
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
    }

    private void generateStore0()  {

        storeFacade.createStore("hila", "hila's bakery", "stips", "hilala@gmail.com", "0546661111");

        storeFacade.addProductToStore("hila", 0, "eyal", 312, 100, "kids", 5, 2);
        storeFacade.addProductToStore("hila", 0, "milk", 312, 20, "dairy", 5, 2);
        storeFacade.addProductToStore("hila", 0, "cheese", 312, 40, "dairy", 5, 2);

        storeFacade.createStore("maki", "maki's bakery", "stips", "maki@gmail.com", "0546661112");

        storeFacade.addProductToStore("maki", 1, "eyal", 312, 100, "kids", 5, 2);
        storeFacade.addProductToStore("maki", 1, "milk", 312, 20, "dairy", 5, 2);
        storeFacade.addProductToStore("maki", 1, "cheese", 312, 40, "dairy", 5, 2);

    }

    private void generateUsers() {
        authFacade.register("hila", "654321", "Eugene", "Krabs", "hilala@gmail.com", "0521957682", LocalDate.of(1942, 11, 30));
        authFacade.login("hila", "654321");
        authFacade.register("maki", "654321", "Eugene", "Krabs", "maki@gmail.com", "0521957682", LocalDate.of(1942, 11, 30));
        authFacade.login("maki", "654321");

    }

    private void generateConditionAndPolicies() throws Exception {
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(100);
        conditionTrue1 = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID3 = conditionRepository.createMinProductCondition(3, 2);
        conditionTrue2 = conditionRepository.findConditionByID(minBuyConditionID3);
        int minBuyConditionID4 = conditionRepository.createMinBuyCondition(400);
        conditionFalse1 = conditionRepository.findConditionByID(minBuyConditionID4);
        int minBuyConditionID5 = conditionRepository.createMinProductCondition(4, 2);
        conditionFalse2 = conditionRepository.findConditionByID(minBuyConditionID5);
        //---
        onStore10discountTrue1ID = discountPolicyRepository.addOnStoreSimpleDiscount(10, conditionTrue1);

        onCategoryDairy10discountTrue1ID = discountPolicyRepository.addOnCategorySimpleDiscount(10,"dairy", conditionTrue2);

        onStore10discountFalse1ID = discountPolicyRepository.addOnStoreSimpleDiscount(10, conditionFalse1);

        onCategoryDairy10discountFalse1ID = discountPolicyRepository.addOnCategorySimpleDiscount(10,"dairy", conditionFalse2);

        productDTOMap = new HashMap<>();
        productDTOMap.put(0, productFacade.getProductDTO(0));
        productDTOMap.put(1, productFacade.getProductDTO(1));
        productDTOMap.put(2, productFacade.getProductDTO(2));

        cart = new ArrayList<>();
        cart.add(new CartItemDTO(0, 0, 1));
        cart.add(new CartItemDTO(0, 1, 1));
        cart.add(new CartItemDTO(0, 2, 3));

    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void checkAddingDiscountToStore(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        discountPolicyFacade.addDiscountPolicyToStore(0, onStore10discountTrue1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onCategoryDairy10discountTrue1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onStore10discountFalse1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onCategoryDairy10discountFalse1ID, "hila");

        List<ProductDataPrice> listProductDataPrices = discountPolicyFacade.calculatePrice(0, cart);
        assertEquals(90 , listProductDataPrices.get(0).getNewPrice());
        assertEquals(16.2 , listProductDataPrices.get(1).getNewPrice());
        assertEquals(32.4 , listProductDataPrices.get(2).getNewPrice());

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void checkRemovingDiscountToStore(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        discountPolicyFacade.addDiscountPolicyToStore(0, onStore10discountTrue1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onCategoryDairy10discountTrue1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onStore10discountFalse1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onCategoryDairy10discountFalse1ID, "hila");
        discountPolicyFacade.removeDiscountPolicyFromStore(0, onCategoryDairy10discountTrue1ID, "hila");
        List<ProductDataPrice> listProductDataPrices = discountPolicyFacade.calculatePrice(0, cart);
        assertEquals(90 , listProductDataPrices.get(0).getNewPrice());
        assertEquals(18 , listProductDataPrices.get(1).getNewPrice());
        assertEquals(36 , listProductDataPrices.get(2).getNewPrice());

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void checkMultipleStore(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        discountPolicyFacade.addDiscountPolicyToStore(0, onStore10discountTrue1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onCategoryDairy10discountTrue1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onStore10discountFalse1ID, "hila");
        discountPolicyFacade.addDiscountPolicyToStore(0, onCategoryDairy10discountFalse1ID, "hila");
        discountPolicyFacade.removeDiscountPolicyFromStore(0, onCategoryDairy10discountTrue1ID, "hila");

        discountPolicyFacade.addDiscountPolicyToStore(1, onStore10discountTrue1ID, "maki");
        discountPolicyFacade.addDiscountPolicyToStore(1, onCategoryDairy10discountTrue1ID, "maki");
        discountPolicyFacade.addDiscountPolicyToStore(1, onStore10discountFalse1ID, "maki");
        discountPolicyFacade.addDiscountPolicyToStore(1, onCategoryDairy10discountFalse1ID, "maki");

        List<ProductDataPrice> listProductDataPricesA = discountPolicyFacade.calculatePrice(0, cart);
        List<ProductDataPrice> listProductDataPricesB = discountPolicyFacade.calculatePrice(1, cart);

        assertEquals(90 , listProductDataPricesA.get(0).getNewPrice());
        assertEquals(18 , listProductDataPricesA.get(1).getNewPrice());
        assertEquals(36 , listProductDataPricesA.get(2).getNewPrice());

        assertEquals(90 , listProductDataPricesB.get(0).getNewPrice());
        assertEquals(16.2 , listProductDataPricesB.get(1).getNewPrice());
        assertEquals(32.4 , listProductDataPricesB.get(2).getNewPrice());

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnProductSimpleDiscountDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        int policyId = discountPolicyFacade.createOnProductSimpleDiscountPolicy(10, 0, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount expected = new SimpleDiscount(policyId, 10, new TrueCondition(9));
        expected.setOnProductID(0);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnCategorySimpleDiscountDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        int policyId = discountPolicyFacade.createOnCategorySimpleDiscountPolicy(10, "dairy", "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount expected = new SimpleDiscount(policyId, 10, new TrueCondition(9));
        expected.setOnCategoryName("dairy");
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnStoreSimpleDiscountDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        int policyId = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(10, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount expected = new SimpleDiscount(policyId, 10, new TrueCondition(9));
        expected.setOnStore();
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnProductConditionDiscountDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId = discountPolicyFacade.createOnProductConditionDiscountPolicy(10,0,conditionTrue1.getId(), "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount expected = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        expected.setOnProductID(0);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnProductConditionDiscountDiscountFail(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionTrue1 is  MinBuyCondition(100);
        int policyId = discountPolicyFacade.createOnProductConditionDiscountPolicy(10,0,conditionTrue1.getId(), "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount expected = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 90));
        expected.setOnProductID(0);
        String expectedJson = toJson(expected);

        assertNotEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnCategoryConditionDiscountDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId = discountPolicyFacade.createOnCategoryConditionDiscountPolicy(10,"dairy",conditionTrue1.getId(), "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount expected = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        expected.setOnCategoryName("dairy");
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }


    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnStoreConditionDiscountDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10,conditionTrue1.getId(), "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount expected = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        expected.setOnStore();
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOrDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId1 = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10, conditionTrue1.getId(), "hila");
        int policyId2 = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(10, "hila");
        int policyId = discountPolicyFacade.createOrDiscountPolicy(policyId1,policyId2, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount simpleDiscount1 = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        simpleDiscount1.setOnStore();
        SimpleDiscount simpleDiscount2 =  new SimpleDiscount(policyId, 10, new TrueCondition(0));
        simpleDiscount2.setOnStore();
        OrDiscount expected = new OrDiscount(0, simpleDiscount1,simpleDiscount2);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createAndDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId1 = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10, conditionTrue1.getId(), "hila");
        int policyId2 = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(10, "hila");
        int policyId = discountPolicyFacade.createAndDiscountPolicy(policyId1,policyId2, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount simpleDiscount1 = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        simpleDiscount1.setOnStore();
        SimpleDiscount simpleDiscount2 =  new SimpleDiscount(policyId, 10, new TrueCondition(0));
        simpleDiscount2.setOnStore();
        AndDiscount expected = new AndDiscount(0, simpleDiscount1,simpleDiscount2);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }
    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createTakeMinXorDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId1 = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10, conditionTrue1.getId(), "hila");
        int policyId2 = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(10, "hila");
        int policyId = discountPolicyFacade.createTakeMinXorDiscountPolicy(policyId1,policyId2, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount simpleDiscount1 = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        simpleDiscount1.setOnStore();
        SimpleDiscount simpleDiscount2 =  new SimpleDiscount(policyId, 10, new TrueCondition(0));
        simpleDiscount2.setOnStore();
        XorDiscount expected = new XorDiscount(0, simpleDiscount1,simpleDiscount2);
        expected.setMin();
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createTakeMaxXorDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId1 = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10, conditionTrue1.getId(), "hila");
        int policyId2 = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(10, "hila");
        int policyId = discountPolicyFacade.createTakeMaxXorDiscountPolicy(policyId1,policyId2, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount simpleDiscount1 = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        simpleDiscount1.setOnStore();
        SimpleDiscount simpleDiscount2 =  new SimpleDiscount(policyId, 10, new TrueCondition(0));
        simpleDiscount2.setOnStore();
        XorDiscount expected = new XorDiscount(0, simpleDiscount1,simpleDiscount2);
        expected.setMax();
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createAdditionDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId1 = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10, conditionTrue1.getId(), "hila");
        int policyId2 = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(10, "hila");
        int policyId = discountPolicyFacade.createAdditionDiscountPolicy(policyId1,policyId2, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount simpleDiscount1 = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        simpleDiscount1.setOnStore();
        SimpleDiscount simpleDiscount2 =  new SimpleDiscount(policyId, 10, new TrueCondition(0));
        simpleDiscount2.setOnStore();
        AdditionDiscount expected = new AdditionDiscount(0, simpleDiscount1,simpleDiscount2);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createMaximumDiscountSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int policyId1 = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10, conditionTrue1.getId(), "hila");
        int policyId2 = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(10, "hila");
        int policyId = discountPolicyFacade.createMaximumDiscountPolicy(policyId1,policyId2, "hila");

        Discount resDiscount = discountPolicyFacade.getDiscountPolicy(policyId);

        String resJson = toJson(resDiscount);

        SimpleDiscount simpleDiscount1 = new SimpleDiscount(policyId, 10, new MinBuyCondition(0, 100));
        simpleDiscount1.setOnStore();
        SimpleDiscount simpleDiscount2 =  new SimpleDiscount(policyId, 10, new TrueCondition(0));
        simpleDiscount2.setOnStore();
        MaximumDiscount expected = new MaximumDiscount(0, simpleDiscount1,simpleDiscount2);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createMinBuyConditionSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int conditionId = discountPolicyFacade.createMinBuyCondition(10, "hila");

        Condition resCondition = conditionRepository.findConditionByID(conditionId);

        String resJson = toJson(resCondition);

        MinBuyCondition expected = new MinBuyCondition(1, 10);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOnProductConditionSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int conditionId = discountPolicyFacade.createMinProductCondition(10, 0, "hila");

        Condition resCondition = conditionRepository.findConditionByID(conditionId);

        String resJson = toJson(resCondition);

        MinProductCondition expected = new MinProductCondition(1, 10);
        expected.setOnProductName(0);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createMinProductOnCategoryConditionSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int conditionId = discountPolicyFacade.createMinProductOnCategoryCondition(10, "dairy", "hila");

        Condition resCondition = conditionRepository.findConditionByID(conditionId);

        String resJson = toJson(resCondition);

        MinProductCondition expected = new MinProductCondition(1, 10);
        expected.setOnCategoryName("dairy");
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createMinProductOnStoreConditionSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int conditionId = discountPolicyFacade.createMinProductOnStoreCondition(10, "hila");

        Condition resCondition = conditionRepository.findConditionByID(conditionId);

        String resJson = toJson(resCondition);

        MinProductCondition expected = new MinProductCondition(1, 10);
        expected.setOnStore();
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createAndConditionSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int conditionId1 = discountPolicyFacade.createMinProductOnStoreCondition(10, "hila");
        int conditionId2 = discountPolicyFacade.createMinProductOnCategoryCondition(10, "dairy", "hila");
        int conditionId3 = discountPolicyFacade.createAndCondition(conditionId1, conditionId2, "hila");
        Condition resCondition = conditionRepository.findConditionByID(conditionId3);

        String resJson = toJson(resCondition);
        MinProductCondition cond1 = new MinProductCondition(1, 10);
        cond1.setOnStore();
        MinProductCondition cond2 = new MinProductCondition(1, 10);
        cond2.setOnCategoryName("dairy");
        AndCondition expected = new AndCondition(1,cond1, cond2);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createOrConditionSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int conditionId1 = discountPolicyFacade.createMinProductOnStoreCondition(10, "hila");
        int conditionId2 = discountPolicyFacade.createMinProductOnCategoryCondition(10, "dairy", "hila");
        int conditionId3 = discountPolicyFacade.createOrCondition(conditionId1, conditionId2, "hila");
        Condition resCondition = conditionRepository.findConditionByID(conditionId3);

        String resJson = toJson(resCondition);
        MinProductCondition cond1 = new MinProductCondition(1, 10);
        cond1.setOnStore();
        MinProductCondition cond2 = new MinProductCondition(1, 10);
        cond2.setOnCategoryName("dairy");
        OrCondition expected = new OrCondition(1,cond1, cond2);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }

    @ParameterizedTest
    @MethodSource("repositoryStream")
    public void createXorConditionSuccess(IDiscountPolicyRepository discountPolicyRepo, IConditionRespository conditionRepo) throws Exception {
        //conditionId 1 is  MinBuyCondition(100);
        int conditionId1 = discountPolicyFacade.createMinProductOnStoreCondition(10, "hila");
        int conditionId2 = discountPolicyFacade.createMinProductOnCategoryCondition(10, "dairy", "hila");
        int conditionId3 = discountPolicyFacade.createXorCondition(conditionId1, conditionId2, "hila");
        Condition resCondition = conditionRepository.findConditionByID(conditionId3);

        String resJson = toJson(resCondition);
        MinProductCondition cond1 = new MinProductCondition(1, 10);
        cond1.setOnStore();
        MinProductCondition cond2 = new MinProductCondition(1, 10);
        cond2.setOnCategoryName("dairy");
        XorCondition expected = new XorCondition(1,cond1, cond2);
        String expectedJson = toJson(expected);

        assertEquals(expectedJson, resJson);

        nextRepoDiscountPolicy = discountPolicyRepo;
        nextRepoCondition = conditionRepo;
    }
}
