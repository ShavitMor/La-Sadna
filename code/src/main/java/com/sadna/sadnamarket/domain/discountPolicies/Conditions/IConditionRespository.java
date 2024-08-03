package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

import com.fasterxml.jackson.core.JsonProcessingException;

import java.util.Set;

public interface IConditionRespository {
    public boolean conditionExists(int condId);
    public Set<Integer> getAllConditionsIds();
    public Condition findConditionByID(int condId) throws Exception;

    int createMinBuyCondition(int minBuy) throws JsonProcessingException;
    int createMinProductCondition(int minAmount, int productID) throws JsonProcessingException;
    int createMinProductOnCategoryCondition(int minAmount, String categoryName) throws JsonProcessingException;
    int createMinProductOnStoreCondition(int minAmount) throws JsonProcessingException;
    int createTrueCondition()throws JsonProcessingException;
    int createXorCondition(Condition conditionA, Condition conditionB) throws JsonProcessingException;
    int createAndCondition(Condition conditionA, Condition conditionB) throws JsonProcessingException;
    int createOrCondition(Condition conditionA, Condition conditionB) throws JsonProcessingException;
    public void clear();
}
