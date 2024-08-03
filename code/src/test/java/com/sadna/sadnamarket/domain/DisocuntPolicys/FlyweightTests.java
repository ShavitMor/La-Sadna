package com.sadna.sadnamarket.domain.DisocuntPolicys;

import com.sadna.sadnamarket.domain.discountPolicies.Conditions.Condition;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.Discount;
import org.junit.Test;
import org.junit.jupiter.api.BeforeEach;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotEquals;

public class FlyweightTests extends DiscountPolicyTest{


    @BeforeEach
    public void setUp() throws Exception {
        super.setUp();
    }

    @Test
    public void checkFlyWeightDiscount() throws Exception {
        int trueConditionID = conditionRepository.createTrueCondition();
        Condition conditionTrue = conditionRepository.findConditionByID(trueConditionID);
        int minBuyConditionID = conditionRepository.createMinBuyCondition(400);
        Condition minBuyCondition = conditionRepository.findConditionByID(minBuyConditionID);
        int OnStore10discountTrue1ID = discountPolicyRepository.addOnStoreSimpleDiscount(10, conditionTrue);
        Discount onStore10DiscountTrue1 = discountPolicyRepository.findDiscountPolicyByID(OnStore10discountTrue1ID);
        assertEquals(1 , OnStore10discountTrue1ID);

        int OnCategoryDairy10discountTrue1ID = discountPolicyRepository.addOnCategorySimpleDiscount(10,"dairy", conditionTrue);
        Discount onCategoryDairy10DiscountTrue1 = discountPolicyRepository.findDiscountPolicyByID(OnCategoryDairy10discountTrue1ID);;
        assertEquals(2, OnCategoryDairy10discountTrue1ID);

        int OnCategoryDairy10discountFalse1ID = discountPolicyRepository.addOnCategorySimpleDiscount(10,"dairy", minBuyCondition);
        Discount onCategoryDairy10DiscountFalse1 = discountPolicyRepository.findDiscountPolicyByID(OnCategoryDairy10discountFalse1ID);
        assertEquals(3, OnCategoryDairy10discountFalse1ID);

        int DiscountID1 = discountPolicyRepository.addAndDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        int DiscountID2 = discountPolicyRepository.addAndDiscount(onCategoryDairy10DiscountTrue1, onStore10DiscountTrue1);
        assertEquals(DiscountID1 , DiscountID2);
        assertEquals(4 , DiscountID2);

        int DiscountID4 = discountPolicyRepository.addAndDiscount(onCategoryDairy10DiscountFalse1, onStore10DiscountTrue1);
        assertNotEquals(DiscountID1 , DiscountID4);
        assertEquals(DiscountID1 + 1 , DiscountID4);
        assertEquals(5 , DiscountID4);


    }

    @Test
    public void checkFlyWeightDiscount2() throws Exception {
        int onProductSimpleDiscountId = discountPolicyFacade.createOnProductSimpleDiscountPolicy(10,0,"hila");
        assertEquals(1 , onProductSimpleDiscountId);

        int minBuyConditionId = discountPolicyFacade.createMinBuyCondition(100, "hila");

        int onStoreSimpleDiscountId = discountPolicyFacade.createOnStoreConditionDiscountPolicy(10, minBuyConditionId,  "hila");
        assertEquals(2, onStoreSimpleDiscountId);


        int DiscountID1 = discountPolicyFacade.createAndDiscountPolicy(onProductSimpleDiscountId, onStoreSimpleDiscountId, "hila");
        int DiscountID2 = discountPolicyFacade.createAndDiscountPolicy(onProductSimpleDiscountId, onStoreSimpleDiscountId, "hila");
        assertEquals(DiscountID1 , DiscountID2);
        assertEquals(3 , DiscountID2);
    }

}
