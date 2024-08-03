package com.sadna.sadnamarket.domain.discountPolicies.Conditions;



import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicy;
import com.sadna.sadnamarket.service.Error;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemoryConditionRepository implements IConditionRespository{
    private Map<Integer, Condition> conditions;
    private Map<Condition, Integer> conditionsDesc;
    private int nextId;
    private final ObjectMapper objectMapper = new ObjectMapper();


    public MemoryConditionRepository(){
        conditions = new HashMap<>();
        conditionsDesc = new HashMap<>();
        nextId = 0;
    }

    @Override
    public int createMinBuyCondition(int minBuy) throws JsonProcessingException{
        if(minBuy < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeMinBuyCondition(minBuy));
        }
        Condition newCondition = new MinBuyCondition(nextId, minBuy);
        return addConditionToMaps(newCondition);
    }

    @Override
    public int createMinProductCondition(int minAmount, int productID)throws JsonProcessingException {
        if(minAmount < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeAmountCondition(minAmount));
        }
        MinProductCondition newCondition = new MinProductCondition(nextId, minAmount);
        newCondition.setOnProductName(productID);
        return addConditionToMaps(newCondition);
    }

    @Override
    public int createMinProductOnCategoryCondition(int minAmount, String categoryName)throws JsonProcessingException {
        if(minAmount < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeAmountCondition(minAmount));
        }
        MinProductCondition newCondition = new MinProductCondition(nextId, minAmount);
        newCondition.setOnCategoryName(categoryName);
        return addConditionToMaps(newCondition);
}

    @Override
    public int createMinProductOnStoreCondition(int minAmount)throws JsonProcessingException {
        if(minAmount < 0){
            throw new IllegalArgumentException(Error.CannotMakeNegativeAmountCondition(minAmount));
        }
        MinProductCondition newCondition = new MinProductCondition(nextId, minAmount);
        newCondition.setOnStore();
        return addConditionToMaps(newCondition);
}

    @Override
    public int createTrueCondition() throws JsonProcessingException {
        Condition newCondition = new TrueCondition(nextId);
        return addConditionToMaps(newCondition);
    }

    @Override
    public int createXorCondition(Condition conditionA, Condition conditionB)throws JsonProcessingException {
        Condition newCondition = new XorCondition(nextId, conditionA, conditionB);
        return addConditionToMaps(newCondition);
    }

    @Override
    public int createAndCondition(Condition conditionA, Condition conditionB)throws JsonProcessingException {
        Condition newCondition = new AndCondition(nextId, conditionA, conditionB);
        return addConditionToMaps(newCondition);
    }

    @Override
    public int createOrCondition(Condition conditionA, Condition conditionB) throws JsonProcessingException{
        Condition newCondition = new OrCondition(nextId, conditionA, conditionB);
        return addConditionToMaps(newCondition);
    }

    private int addConditionToMaps(Condition newCondition) throws JsonProcessingException {
        //String newConditionDesc = newCondition.getClass().getName() + "-" + objectMapper.writeValueAsString(newCondition);
        if(!conditionsDesc.containsKey(newCondition)) {
            conditions.put(nextId, newCondition);
            conditionsDesc.put(newCondition, nextId);
            nextId++;
            return nextId - 1;
        }
        else {
            return conditionsDesc.get(newCondition);
        }
    }
    public Condition findConditionByID(int condId) throws Exception {
        if(!conditionExists(condId)) {
            throw new IllegalArgumentException(Error.makeConditionWithIdDoesNotExistError(condId));
        }
        return conditions.get(condId);
    }
    public boolean conditionExists(int condId) {
        return conditions.containsKey(condId);
    }

    @Override
    public Set<Integer> getAllConditionsIds() {
        return conditions.keySet();
    }

    @Override
    public void clear() {
        this.conditions = new HashMap<>();
        this.conditionsDesc = new HashMap<>();
        this.nextId = 0;
    }

}
