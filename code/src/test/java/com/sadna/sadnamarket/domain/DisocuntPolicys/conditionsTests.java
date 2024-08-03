package com.sadna.sadnamarket.domain.DisocuntPolicys;

import com.sadna.sadnamarket.domain.discountPolicies.Conditions.Condition;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class conditionsTests extends DiscountPolicyTest{
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();

    }
    @Test
    public void checkMinBuyConditionWhenTrue() throws Exception {
        int minBuyConditionID = conditionRepository.createMinBuyCondition(100);
        Condition minBuyCondition = conditionRepository.findConditionByID(minBuyConditionID);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",1,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertTrue(minBuyCondition.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkMinBuyConditionWhenFalse() throws Exception {
        int minBuyConditionID = conditionRepository.createMinBuyCondition(100);
        Condition minBuyCondition = conditionRepository.findConditionByID(minBuyConditionID);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",1,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertFalse(minBuyCondition.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkMinAmountConditionWhenTrue() throws Exception {
        int minAmountConditionID1 = conditionRepository.createMinProductCondition(3, 2);
        Condition minMinAmountCondition1 = conditionRepository.findConditionByID(minAmountConditionID1);
        int minAmountConditionID2 = conditionRepository.createMinProductOnCategoryCondition(5, "dairy");
        Condition minMinAmountCondition2 = conditionRepository.findConditionByID(minAmountConditionID2);
        int minAmountConditionID3 = conditionRepository.createMinProductOnStoreCondition(6);
        Condition minMinAmountCondition3 = conditionRepository.findConditionByID(minAmountConditionID3);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 2,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",3,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertTrue(minMinAmountCondition1.checkCond(cartMap, listProductDataPrices));
        assertTrue(minMinAmountCondition2.checkCond(cartMap, listProductDataPrices));
        assertTrue(minMinAmountCondition3.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkMinAmountConditionWhenFalse() throws Exception {
        int minAmountConditionID1 = conditionRepository.createMinProductCondition(4, 2);
        Condition minMinAmountCondition1 = conditionRepository.findConditionByID(minAmountConditionID1);
        int minAmountConditionID2 = conditionRepository.createMinProductOnCategoryCondition(6, "dairy");
        Condition minMinAmountCondition2 = conditionRepository.findConditionByID(minAmountConditionID2);
        int minAmountConditionID3 = conditionRepository.createMinProductOnStoreCondition(7);
        Condition minMinAmountCondition3 = conditionRepository.findConditionByID(minAmountConditionID3);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 2,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",3,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertFalse(minMinAmountCondition1.checkCond(cartMap, listProductDataPrices));
        assertFalse(minMinAmountCondition2.checkCond(cartMap, listProductDataPrices));
        assertFalse(minMinAmountCondition3.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkOrConditionWhenTrue() throws Exception {
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(100);
        Condition minBuyConditionTrue = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID2 = conditionRepository.createMinProductCondition(4, 2);
        Condition minAmountConditionFalse = conditionRepository.findConditionByID(minBuyConditionID2);
        int minBuyConditionID3 = conditionRepository.createMinProductCondition(3, 2);
        Condition minAmountConditionTrue = conditionRepository.findConditionByID(minBuyConditionID3);

        int orConditionID1 = conditionRepository.createOrCondition(minBuyConditionTrue, minAmountConditionFalse);
        int orConditionID2 = conditionRepository.createOrCondition(minBuyConditionTrue, minAmountConditionTrue);
        Condition orCondition1 = conditionRepository.findConditionByID(orConditionID1);
        Condition orCondition2 = conditionRepository.findConditionByID(orConditionID2);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",1,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertTrue(orCondition1.checkCond(cartMap, listProductDataPrices));
        assertTrue(orCondition2.checkCond(cartMap, listProductDataPrices));

    }

    @Test
    public void checkOrConditionWhenFalse() throws Exception {
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(200);
        Condition minBuyConditionFalse = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID2 = conditionRepository.createMinProductCondition(4, 2);
        Condition minAmountConditionFalse = conditionRepository.findConditionByID(minBuyConditionID2);

        int orConditionID = conditionRepository.createOrCondition(minBuyConditionFalse, minAmountConditionFalse);
        Condition orCondition = conditionRepository.findConditionByID(orConditionID);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",1,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertFalse(orCondition.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkAndConditionWhenTrue() throws Exception {
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(100);
        Condition minBuyConditionTrue = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID3 = conditionRepository.createMinProductCondition(3, 2);
        Condition minAmountConditionTrue = conditionRepository.findConditionByID(minBuyConditionID3);

        int AndConditionID = conditionRepository.createAndCondition(minBuyConditionTrue, minAmountConditionTrue);
        Condition AndCondition = conditionRepository.findConditionByID(AndConditionID);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",3,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertTrue(AndCondition.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkAndConditionWhenFalse() throws Exception {
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(200);
        Condition minBuyConditionFalse = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID3 = conditionRepository.createMinProductCondition(1, 2);
        Condition minAmountConditionTrue = conditionRepository.findConditionByID(minBuyConditionID3);

        int AndConditionID = conditionRepository.createAndCondition(minBuyConditionFalse, minAmountConditionTrue);
        Condition AndCondition = conditionRepository.findConditionByID(AndConditionID);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",1,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertFalse(AndCondition.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkXorConditionWhenTrue() throws Exception {
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(200);
        Condition minBuyConditionFalse = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID3 = conditionRepository.createMinProductCondition(1, 2);
        Condition minAmountConditionTrue = conditionRepository.findConditionByID(minBuyConditionID3);

        int XorConditionID = conditionRepository.createXorCondition(minBuyConditionFalse, minAmountConditionTrue);
        Condition XorCondition = conditionRepository.findConditionByID(XorConditionID);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",1,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertTrue(XorCondition.checkCond(cartMap, listProductDataPrices));
    }

    @Test
    public void checkXorConditionWhenFalse() throws Exception {
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(100);
        Condition minBuyConditionTrue = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID3 = conditionRepository.createMinProductCondition(3, 2);
        Condition minAmountConditionTrue = conditionRepository.findConditionByID(minBuyConditionID3);
        int minBuyConditionID4 = conditionRepository.createMinBuyCondition(400);
        Condition minBuyConditionFalse = conditionRepository.findConditionByID(minBuyConditionID4);
        int minBuyConditionID5 = conditionRepository.createMinProductCondition(4, 2);
        Condition minAmountConditionFalse = conditionRepository.findConditionByID(minBuyConditionID5);

        int XorConditionID1 = conditionRepository.createXorCondition(minBuyConditionTrue, minAmountConditionTrue);
        Condition XorCondition1 = conditionRepository.findConditionByID(XorConditionID1);

        int XorConditionID2 = conditionRepository.createXorCondition(minBuyConditionFalse, minAmountConditionFalse);
        Condition XorCondition2 = conditionRepository.findConditionByID(XorConditionID2);

        List<ProductDataPrice> listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",3,40,40));

        Map<Integer, ProductDTO> cartMap = new HashMap<>();
        cartMap.put(0, productFacade.getProductDTO(0));
        cartMap.put(1, productFacade.getProductDTO(1));
        cartMap.put(2, productFacade.getProductDTO(2));

        assertFalse(XorCondition1.checkCond(cartMap, listProductDataPrices));
        assertFalse(XorCondition2.checkCond(cartMap, listProductDataPrices));

    }
}
