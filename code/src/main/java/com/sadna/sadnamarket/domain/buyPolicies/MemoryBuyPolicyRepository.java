package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.fasterxml.jackson.datatype.jsr310.JavaTimeModule;
import com.sadna.sadnamarket.service.Error;

import java.time.LocalTime;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Set;

public class MemoryBuyPolicyRepository implements IBuyPolicyRepository{
    private Map<Integer, BuyPolicy> buyPolicies;
    private Map<BuyPolicy, Integer> buyPoliciesDesc;
    private int nextId;
    //private ObjectMapper objectMapper = new ObjectMapper();
    //private SimpleFilterProvider idFilter;

    public MemoryBuyPolicyRepository() {
        this.buyPolicies = new HashMap<>();
        this.buyPoliciesDesc = new HashMap<>();
        this.nextId = 0;
        //this.idFilter = new SimpleFilterProvider().addFilter("idFilter", SimpleBeanPropertyFilter.serializeAllExcept("id"));
        //objectMapper.registerModule(new JavaTimeModule());
    }

    @Override
    public BuyPolicy findBuyPolicyByID(int policyId) {
        synchronized (buyPolicies) {
            if (!buyPolicyExists(policyId))
                throw new IllegalArgumentException(Error.makeBuyPolicyWithIdDoesNotExistError(policyId));

            return buyPolicies.get(policyId);
        }
    }

    @Override
    public Set<Integer> getAllPolicyIds() {
        synchronized (buyPolicies) {
            return buyPolicies.keySet();
        }
    }

    @Override
    public int addProductKgBuyPolicy(int productId, List<BuyType> buytypes, double min, double max) throws JsonProcessingException {
        BuyPolicy newPolicy =  new KgLimitBuyPolicy(nextId, buytypes, new ProductSubject(productId), min, max);
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addProductAmountBuyPolicy(int productId, List<BuyType> buytypes, int min, int max) throws JsonProcessingException {
        BuyPolicy newPolicy = new AmountBuyPolicy(nextId, buytypes, new ProductSubject(productId), min, max);
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addCategoryAgeLimitBuyPolicy(String category, List<BuyType> buytypes, int min, int max) throws JsonProcessingException {
        BuyPolicy newPolicy = new AgeLimitBuyPolicy(nextId, buytypes, new CategorySubject(category), min, max);
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addCategoryHourLimitBuyPolicy(String category, List<BuyType> buytypes, LocalTime from, LocalTime to) throws JsonProcessingException {
        BuyPolicy newPolicy = new HourLimitBuyPolicy(nextId, buytypes, new CategorySubject(category), from, to);
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addCategoryRoshChodeshBuyPolicy(String category, List<BuyType> buytypes) throws JsonProcessingException {
        BuyPolicy newPolicy = new RoshChodeshBuyPolicy(nextId, buytypes, new CategorySubject(category));
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addCategoryHolidayBuyPolicy(String category, List<BuyType> buytypes) throws JsonProcessingException {
        BuyPolicy newPolicy = new HolidayBuyPolicy(nextId, buytypes, new CategorySubject(category));
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addCategorySpecificDateBuyPolicy(String category, List<BuyType> buytypes, int day, int month, int year) throws JsonProcessingException {
        BuyPolicy newPolicy = new SpecificDateBuyPolicy(nextId, buytypes, new CategorySubject(category), day, month, year);
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addAndBuyPolicy(int id1, int id2) throws JsonProcessingException {
        BuyPolicy policy1 = findBuyPolicyByID(id1);
        BuyPolicy policy2 = findBuyPolicyByID(id2);
        BuyPolicy newPolicy = new AndBuyPolicy(nextId, policy1, policy2);
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addOrBuyPolicy(int id1, int id2) throws JsonProcessingException {
        BuyPolicy policy1 = findBuyPolicyByID(id1);
        BuyPolicy policy2 = findBuyPolicyByID(id2);
        BuyPolicy newPolicy = new OrBuyPolicy(nextId, policy1, policy2);
        return addPolicyToMaps(newPolicy);
    }

    @Override
    public int addConditioningBuyPolicy(int id1, int id2) throws JsonProcessingException {
        BuyPolicy policy1 = findBuyPolicyByID(id1);
        BuyPolicy policy2 = findBuyPolicyByID(id2);
        BuyPolicy newPolicy = new ConditioningBuyPolicy(nextId, policy1, policy2);
        return addPolicyToMaps(newPolicy);
    }

    private int addPolicyToMaps(BuyPolicy newPolicy) throws JsonProcessingException {
        //String policyDesc = newPolicy.getClass().getName() + "-" + objectMapper.writer(idFilter).writeValueAsString(newPolicy);
        synchronized (buyPolicies) {
            if (!buyPoliciesDesc.containsKey(newPolicy)) {
                buyPolicies.put(nextId, newPolicy);
                buyPoliciesDesc.put(newPolicy, nextId);
                nextId++;
                return nextId - 1;
            } else {
                return buyPoliciesDesc.get(newPolicy);
            }
        }
    }

    @Override
    public boolean buyPolicyExists(int policyId) {
        synchronized (buyPolicies) {
            return buyPolicies.containsKey(policyId);
        }
    }

    @Override
    public BuyPolicyManager createManager(BuyPolicyFacade facade, int storeId) {
        return new MemoryBuyPolicyManager(facade);
    }

    @Override
    public void clear() {
        this.buyPolicies = new HashMap<>();
        this.buyPoliciesDesc = new HashMap<>();
        this.nextId = 0;
    }

}
