package com.sadna.sadnamarket.domain.DisocuntPolicys;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;

import com.sadna.sadnamarket.domain.discountPolicies.Conditions.Condition;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.Discount;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;
import static org.junit.jupiter.api.Assertions.*;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.mockito.ArgumentMatchers.any;

public class DiscountTests extends DiscountPolicyTest{
    List<ProductDataPrice> listProductDataPrices;
    Map<Integer, ProductDTO> productDTOMap;
    Condition conditionTrue1;
    Condition conditionTrue2;
    Condition conditionFalse1;
    Condition conditionFalse2;

    Discount onStore10DiscountTrue1;
    Discount onCategoryDairy10DiscountTrue1;
    Discount onStore10DiscountFalse1;
    Discount onCategoryDairy10DiscountFalse1;
    ProductDataPrice eyalItemWithId0;
    ProductDataPrice milkItem;
    ProductDataPrice cheeseItem;
    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
        int minBuyConditionID1 = conditionRepository.createMinBuyCondition(100);
        conditionTrue1 = conditionRepository.findConditionByID(minBuyConditionID1);
        int minBuyConditionID3 = conditionRepository.createMinProductCondition(3, 2);
        conditionTrue2 = conditionRepository.findConditionByID(minBuyConditionID3);
        int minBuyConditionID4 = conditionRepository.createMinBuyCondition(400);
        conditionFalse1 = conditionRepository.findConditionByID(minBuyConditionID4);
        int minBuyConditionID5 = conditionRepository.createMinProductCondition(4, 2);
        conditionFalse2 = conditionRepository.findConditionByID(minBuyConditionID5);
        //---
        int OnStore10discountTrue1ID = discountPolicyRepository.addOnStoreSimpleDiscount(10, conditionTrue1);
        onStore10DiscountTrue1 = discountPolicyRepository.findDiscountPolicyByID(OnStore10discountTrue1ID);

        int OnCategoryDairy10discountTrue1ID = discountPolicyRepository.addOnCategorySimpleDiscount(10,"dairy", conditionTrue2);
        onCategoryDairy10DiscountTrue1 = discountPolicyRepository.findDiscountPolicyByID(OnCategoryDairy10discountTrue1ID);;

        int OnStore10discountFalse1ID = discountPolicyRepository.addOnStoreSimpleDiscount(10, conditionFalse1);
        onStore10DiscountFalse1 = discountPolicyRepository.findDiscountPolicyByID(OnStore10discountFalse1ID);

        int OnCategoryDairy10discountFalse1ID = discountPolicyRepository.addOnCategorySimpleDiscount(10,"dairy", conditionFalse2);
        onCategoryDairy10DiscountFalse1 = discountPolicyRepository.findDiscountPolicyByID(OnCategoryDairy10discountFalse1ID);

        listProductDataPrices = new ArrayList<>();
        listProductDataPrices.add(new ProductDataPrice(0,0, "eyal",1, 100,100));
        listProductDataPrices.add(new ProductDataPrice(1,0, "milk", 1,20,20));
        listProductDataPrices.add(new ProductDataPrice(2,0, "cheese",3,40,40));

        productDTOMap = new HashMap<>();
        productDTOMap.put(0, productFacade.getProductDTO(0));
        productDTOMap.put(1, productFacade.getProductDTO(1));
        productDTOMap.put(2, productFacade.getProductDTO(2));
        eyalItemWithId0 = listProductDataPrices.get(0);
        milkItem = listProductDataPrices.get(1);
        cheeseItem = listProductDataPrices.get(2);
    }

    
    @Test
    public void checkConditions() throws Exception {
        assertTrue(conditionTrue1.checkCond(productDTOMap, listProductDataPrices));
        assertTrue(conditionTrue2.checkCond(productDTOMap, listProductDataPrices));
        assertFalse(conditionFalse1.checkCond(productDTOMap, listProductDataPrices));
        assertFalse(conditionFalse2.checkCond(productDTOMap, listProductDataPrices));

    }


    @Test
    public void checkSimpleDiscountOnProductCondIsTrue() throws Exception {
        //condition is true
        double percentDiscount = 50;
        int simpleDiscountID1 = discountPolicyRepository.addOnProductSimpleDiscount(percentDiscount, 0, conditionTrue1);
        Discount simpleDiscount1 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID1);
        simpleDiscount1.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice()*(1 - percentDiscount / 100) , eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice(), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice(), cheeseItem.getNewPrice());

    }

    @Test
    public void checkSimpleDiscountOnProductCondIsFalse() throws Exception {
        //condition is false now
        double percentDiscount = 50;
        int simpleDiscountID2 = discountPolicyRepository.addOnProductSimpleDiscount(percentDiscount, 0, conditionFalse1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice(), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice(), cheeseItem.getNewPrice());
}

    @Test
    public void checkSimpleDiscountOnCategoryCondTrue() throws Exception {
        //condition is true
        double percentDiscount = 50;
        int simpleDiscountID1 = discountPolicyRepository.addOnCategorySimpleDiscount(50, "dairy", conditionTrue1);
        Discount simpleDiscount1 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID1);
        simpleDiscount1.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice()*(1 - percentDiscount / 100)  , milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice()*(1 - percentDiscount / 100)  , cheeseItem.getNewPrice());

    }

    @Test
    public void checkSimpleDiscountOnCategoryCondIsFalse() throws Exception {
        //condition is true
        double percentDiscount = 50;
        int simpleDiscountID2 = discountPolicyRepository.addOnCategorySimpleDiscount(percentDiscount, "dairy", conditionFalse1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice(), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice(), cheeseItem.getNewPrice());
    }

    @Test
    public void checkSimpleDiscountOnStoreCondTrue() throws Exception {
        double percentDiscount = 50;
        //condition is true
        int simpleDiscountID1 = discountPolicyRepository.addOnStoreSimpleDiscount(percentDiscount, conditionTrue1);
        Discount simpleDiscount1 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID1);
        simpleDiscount1.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice()*(1 - percentDiscount / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice()*(1 - percentDiscount / 100)  , milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice()*(1 - percentDiscount / 100)  , cheeseItem.getNewPrice());

    }

    @Test
    public void checkSimpleDiscountOnStoreCondFalse() throws Exception {
        double percentDiscount = 50;
        //condition is false now
        int simpleDiscountID2 = discountPolicyRepository.addOnStoreSimpleDiscount(percentDiscount, conditionFalse1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice(), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice(), cheeseItem.getNewPrice());
    }

    @Test
    public void checkMaximumDiscountBothTrue() throws Exception {
        double percentDiscount = 10;
        //twos condition are true
        int DiscountID1 = discountPolicyRepository.addMaximumDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        Discount Discount1 = discountPolicyRepository.findDiscountPolicyByID(DiscountID1);
        Discount1.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice() * (1 - percentDiscount / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount / 100), cheeseItem.getNewPrice());

    }


    @Test
    public void checkMaximumDiscountOneTrue() throws Exception {
        double percentDiscount = 10;
        //one condition is false, one is true
        int simpleDiscountID2 = discountPolicyRepository.addMaximumDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountFalse1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount / 100), cheeseItem.getNewPrice());
    }

    @Test
    public void checkMaximumDiscountBothFalse() throws Exception {
        double percentDiscount = 10;
        //two conditions are false,
        int simpleDiscountID3 = discountPolicyRepository.addMaximumDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountFalse1);
        Discount simpleDiscount3 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID3);
        simpleDiscount3.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice(), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice(), cheeseItem.getNewPrice());
    }


    @Test
    public void checkAdditionDiscountBothCondTrue() throws Exception {
        //twos condition are true
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;

        int DiscountID1 = discountPolicyRepository.addAdditionDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        Discount Discount1 = discountPolicyRepository.findDiscountPolicyByID(DiscountID1);
        Discount1.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() * (1 - percentDiscount1 / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - (percentDiscount1 + percentDiscount2) / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - (percentDiscount1 + percentDiscount2) / 100), cheeseItem.getNewPrice());
    }
    @Test
    public void checkAdditionDiscountOneCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //one condition is false, one is true
        int simpleDiscountID2 = discountPolicyRepository.addAdditionDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountFalse1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100), cheeseItem.getNewPrice());
    }
    @Test
    public void checkAdditionDiscountBothCondFalse() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //two conditions are false,
        int simpleDiscountID3 = discountPolicyRepository.addAdditionDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountFalse1);
        Discount simpleDiscount3 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID3);
        simpleDiscount3.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() , eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() , milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() , cheeseItem.getNewPrice());
    }

    @Test
    public void checkOrDiscountBothCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //twos condition are true
        int DiscountID1 = discountPolicyRepository.addOrDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        Discount Discount1 = discountPolicyRepository.findDiscountPolicyByID(DiscountID1);
        Discount1.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() * (1 - percentDiscount1 / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100) * (1 - percentDiscount2 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100) * (1 - percentDiscount2 / 100), cheeseItem.getNewPrice());

    }
    @Test
    public void checkOrDiscountOneCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //one condition is false, one is true
        int simpleDiscountID2 = discountPolicyRepository.addOrDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountFalse1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() * (1 - percentDiscount1 / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100) * (1 - percentDiscount2 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100) * (1 - percentDiscount2 / 100), cheeseItem.getNewPrice());

    }
    @Test
    public void checkOrDiscountBothCondFalse() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //two conditions are false,
        int simpleDiscountID3 = discountPolicyRepository.addOrDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountFalse1);
        Discount simpleDiscount3 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID3);
        simpleDiscount3.giveDiscount(productDTOMap, listProductDataPrices);

        assertEquals(eyalItemWithId0.getOldPrice() , eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() , milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() , cheeseItem.getNewPrice());
    }

    @Test
    public void checkMaxXorDiscountBothCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //twos condition are true
        int DiscountID1 = discountPolicyRepository.addTakeMaxXorDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        Discount Discount1 = discountPolicyRepository.findDiscountPolicyByID(DiscountID1);
        Discount1.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() * (1 - percentDiscount1 / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100), cheeseItem.getNewPrice());
    }
    @Test
    public void checkMaxXorDiscountOneCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //one condition is false, one is true
        int simpleDiscountID2 = discountPolicyRepository.addTakeMaxXorDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountFalse1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100), cheeseItem.getNewPrice());
    }

    @Test
    public void checkMaxXorDiscountBothCondFalse() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //two conditions are false,
        int simpleDiscountID3 = discountPolicyRepository.addTakeMaxXorDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountFalse1);
        Discount simpleDiscount3 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID3);
        simpleDiscount3.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() , eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() , milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() , cheeseItem.getNewPrice());
    }

    @Test
    public void checkMinXorDiscountBothCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //twos condition are true
        int DiscountID1 = discountPolicyRepository.addTakeMinXorDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        Discount Discount1 = discountPolicyRepository.findDiscountPolicyByID(DiscountID1);
        Discount1.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100), cheeseItem.getNewPrice());
    }

    @Test
    public void checkMinXorDiscountOneCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //one condition is false, one is true
        int simpleDiscountID2 = discountPolicyRepository.addTakeMinXorDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountTrue1);
        Discount simpleDiscount2 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID2);
        simpleDiscount2.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() * (1 - percentDiscount1 / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100), cheeseItem.getNewPrice());
    }
    @Test
    public void checkMinXorDiscountBothCondFalse() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //two conditions are false,
        int simpleDiscountID3 = discountPolicyRepository.addTakeMinXorDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountFalse1);
        Discount simpleDiscount3 = discountPolicyRepository.findDiscountPolicyByID(simpleDiscountID3);
        simpleDiscount3.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() , eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() , milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() , cheeseItem.getNewPrice());
    }


    @Test
    public void checkAndDiscountBothCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //twos condition are true
        int DiscountID1 = discountPolicyRepository.addAndDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        Discount Discount1 = discountPolicyRepository.findDiscountPolicyByID(DiscountID1);
        Discount1.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice() * (1 - percentDiscount1 / 100), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice() * (1 - percentDiscount1 / 100) * (1 - percentDiscount2 / 100), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice() * (1 - percentDiscount1 / 100) * (1 - percentDiscount2 / 100), cheeseItem.getNewPrice());
    }

    @Test
    public void checkAndDiscountOneCondTrue() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //one condition is false, one is true
        int Discount2ID2 = discountPolicyRepository.addAndDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountFalse1);
        Discount Discount2 = discountPolicyRepository.findDiscountPolicyByID(Discount2ID2);
        Discount2.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice(), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice(), cheeseItem.getNewPrice());
    }

    @Test
    public void checkAndDiscountBothCondFalse() throws Exception {
        double percentDiscount1 = 10;
        double percentDiscount2 = 10;
        //two conditions are false,
        int DiscountID3 = discountPolicyRepository.addAndDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountFalse1);
        Discount Discount3 = discountPolicyRepository.findDiscountPolicyByID(DiscountID3);
        Discount3.giveDiscount(productDTOMap, listProductDataPrices);
        assertEquals(eyalItemWithId0.getOldPrice(), eyalItemWithId0.getNewPrice());
        assertEquals(milkItem.getOldPrice(), milkItem.getNewPrice());
        assertEquals(cheeseItem.getOldPrice(), cheeseItem.getNewPrice());
    }




}
