package com.sadna.sadnamarket.domain.discountPolicies.Discounts;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.Condition;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyManager;
import com.sadna.sadnamarket.domain.discountPolicies.HibernateDiscountPolicyManager;
import com.sadna.sadnamarket.domain.discountPolicies.MemoryDiscountPolicyManager;
import com.sadna.sadnamarket.service.Error;

import java.util.HashMap;
import java.util.Map;
import java.util.Set;

public class MemoryDiscountPolicyRepository implements IDiscountPolicyRepository{
    private Map<Integer, Discount> discountPolicies;
    private Map<Discount, Integer> discountPoliciesDesc;
    private int nextId;
    private final ObjectMapper objectMapper = new ObjectMapper();

    public MemoryDiscountPolicyRepository(){
        discountPolicies = new HashMap<>();
        discountPoliciesDesc = new HashMap<>();
        nextId = 0;
    }
    @Override
    public int addMaximumDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new MaximumDiscount(nextId, discountA, discountB);
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addOrDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new OrDiscount(nextId, discountA, discountB);
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addOnCategorySimpleDiscount(double percentage, String categoryName, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(nextId, percentage, condition);
        newDiscountPolicy.setOnCategoryName(categoryName);
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addOnProductSimpleDiscount(double percentage, int productID, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(nextId, percentage, condition);
        newDiscountPolicy.setOnProductID(productID);
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addOnStoreSimpleDiscount(double percentage, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(nextId, percentage, condition);
        newDiscountPolicy.setOnStore();
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addTakeMaxXorDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        XorDiscount newDiscountPolicy = new XorDiscount(nextId, discountA, discountB);
        newDiscountPolicy.setMax();
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addTakeMinXorDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        XorDiscount newDiscountPolicy = new XorDiscount(nextId, discountA, discountB);
        newDiscountPolicy.setMin();
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addAdditionDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new AdditionDiscount(nextId, discountA, discountB);
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addAndDiscount(Discount discountA, Discount discountB) throws JsonProcessingException {
        Discount newDiscountPolicy = new AndDiscount(nextId, discountA, discountB);
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    @Override
    public int addDefaultDiscount(double percentage, Condition condition) throws JsonProcessingException {
        if(percentage >100 || percentage <0){
            throw new IllegalArgumentException(Error.percentageForDiscountIsNotInRange(percentage));
        }
        SimpleDiscount newDiscountPolicy = new SimpleDiscount(nextId, percentage, condition);
        newDiscountPolicy.setOnStore();
        newDiscountPolicy.setDefault();
        return addDiscountPolicyToMaps(newDiscountPolicy);
    }

    private int addDiscountPolicyToMaps(Discount newDiscountPolicy) throws JsonProcessingException {
        //String newConditionDesc = newDiscountPolicy.getClass().getName() + "-" + objectMapper.writeValueAsString(newDiscountPolicy);
        if(!discountPoliciesDesc.containsKey(newDiscountPolicy)) {
            discountPolicies.put(nextId, newDiscountPolicy);
            discountPoliciesDesc.put(newDiscountPolicy, nextId);
            nextId++;
            return nextId - 1;
        }
        else {
            return discountPoliciesDesc.get(newDiscountPolicy);
        }
    }


    @Override
    public Set<Integer> getAllPolicyIds() {
        return discountPolicies.keySet();
    }

    public Discount findDiscountPolicyByID(int discountPolicyID) throws Exception {
        if(!discountPolicyExists(discountPolicyID)) {
            throw new IllegalArgumentException(Error.makeDiscountPolicyWithIdDoesNotExistError(discountPolicyID));
        }
        return discountPolicies.get(discountPolicyID);
    }
    public boolean discountPolicyExists(int discountPolicyID) {
        return discountPolicies.containsKey(discountPolicyID);
    }

    @Override
    public DiscountPolicyManager createManager(DiscountPolicyFacade facade, int storeId) {
        return new MemoryDiscountPolicyManager(facade);
    }

    @Override
    public void clear() {
        this.discountPolicies = new HashMap<>();
        this.discountPoliciesDesc = new HashMap<>();
        this.nextId = 0;
    }

}
