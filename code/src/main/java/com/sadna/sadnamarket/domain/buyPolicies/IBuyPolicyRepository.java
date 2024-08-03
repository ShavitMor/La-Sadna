package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.stores.Store;

import java.time.LocalTime;
import java.util.List;
import java.util.Set;

public interface IBuyPolicyRepository {
    public BuyPolicy findBuyPolicyByID(int policyId);
    public Set<Integer> getAllPolicyIds();
    public int addProductKgBuyPolicy(int productId, List<BuyType> buytypes, double min, double max) throws JsonProcessingException;
    public int addProductAmountBuyPolicy(int productId, List<BuyType> buytypes, int min, int max) throws JsonProcessingException;
    public int addCategoryAgeLimitBuyPolicy(String category, List<BuyType> buytypes, int min, int max) throws JsonProcessingException;
    public int addCategoryHourLimitBuyPolicy(String category, List<BuyType> buytypes, LocalTime from, LocalTime to) throws JsonProcessingException;
    public int addCategoryRoshChodeshBuyPolicy(String category, List<BuyType> buytypes) throws JsonProcessingException;
    public int addCategoryHolidayBuyPolicy(String category, List<BuyType> buytypes) throws JsonProcessingException;
    public int addCategorySpecificDateBuyPolicy(String category, List<BuyType> buytypes, int day, int month, int year) throws JsonProcessingException;
    public int addAndBuyPolicy(int policyId1, int policyId2) throws JsonProcessingException;
    public int addOrBuyPolicy(int policyId1, int policyId2) throws JsonProcessingException;
    public int addConditioningBuyPolicy(int policyId1, int policyId2) throws JsonProcessingException;
    public boolean buyPolicyExists(int policyId);
    public BuyPolicyManager createManager(BuyPolicyFacade facade, int storeId);
    public void clear();
}
