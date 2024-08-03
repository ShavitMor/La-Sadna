package com.sadna.sadnamarket.domain.buyPolicies;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.stores.PolicyDescriptionDTO;
import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.domain.users.Permission;
import com.sadna.sadnamarket.domain.users.UserFacade;
import com.sadna.sadnamarket.service.Error;

import java.time.LocalTime;
import java.util.*;

public class BuyPolicyFacade {
    private Map<Integer, BuyPolicyManager> mapper;
    private IBuyPolicyRepository buyPolicyRepository;
    private ProductFacade productFacade;
    private UserFacade userFacade;
    private StoreFacade storeFacade;

    public BuyPolicyFacade(IBuyPolicyRepository buyPolicyRepository) {
        this.mapper = new HashMap<Integer, BuyPolicyManager>();
        this.buyPolicyRepository = buyPolicyRepository;
    }

    public void setProductFacade(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    public void setUserFacade(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    public void setStoreFacade(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }

    public BuyPolicy getBuyPolicy(int policyId) {
        return buyPolicyRepository.findBuyPolicyByID(policyId);
    }

    public String getPolicyDescription(int policyId) {
        return buyPolicyRepository.findBuyPolicyByID(policyId).getPolicyDesc();
    }

    private void checkUserAndProduct(int productId, String username) {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));
        if(!productFacade.productExists(productId))
            throw new IllegalArgumentException(Error.makeProductDoesntExistError(productId));
    }

    // only the ones that are in the requirements for now, we can add more if needed
    public synchronized int createProductKgBuyPolicy(int productId, List<BuyType> buytypes, double min, double max, String username) throws JsonProcessingException {
        checkUserAndProduct(productId, username);
        return buyPolicyRepository.addProductKgBuyPolicy(productId, buytypes, min, max);
    }

    public synchronized int createProductAmountBuyPolicy(int productId, List<BuyType> buytypes, int min, int max, String username) throws JsonProcessingException {
        checkUserAndProduct(productId, username);
        return buyPolicyRepository.addProductAmountBuyPolicy(productId, buytypes, min, max);
    }

    public synchronized int createCategoryAgeLimitBuyPolicy(String category, List<BuyType> buytypes, int min, int max, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addCategoryAgeLimitBuyPolicy(category, buytypes, min, max);
    }

    public synchronized int createCategoryHourLimitBuyPolicy(String category, List<BuyType> buytypes, LocalTime from, LocalTime to, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addCategoryHourLimitBuyPolicy(category, buytypes, from, to);
    }

    public synchronized int createCategoryRoshChodeshBuyPolicy(String category, List<BuyType> buytypes, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addCategoryRoshChodeshBuyPolicy(category, buytypes);
    }

    public synchronized int createCategoryHolidayBuyPolicy(String category, List<BuyType> buytypes, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addCategoryHolidayBuyPolicy(category, buytypes);
    }

    public synchronized int createSpecificDateBuyPolicy(String category, List<BuyType> buytypes, int day, int month, int year, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addCategorySpecificDateBuyPolicy(category, buytypes, day, month, year);
    }

    public synchronized int createAndBuyPolicy(int policyId1, int policyId2, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addAndBuyPolicy(policyId1, policyId2);
    }

    public synchronized int createOrBuyPolicy(int policyId1, int policyId2, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addOrBuyPolicy(policyId1, policyId2);
    }

    public synchronized int createConditioningBuyPolicy(int policyId1, int policyId2, String username) throws JsonProcessingException {
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeUserCanNotCreateBuyPolicyError(username));

        return buyPolicyRepository.addConditioningBuyPolicy(policyId1, policyId2);
    }

    private void checkAddBuyPolicyToStore(String username, int storeId, int policyId) {
        if (!storeFacade.isStoreActive(storeId))
            throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));
        if (!hasPermission(username, Permission.ADD_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeStoreUserCannotAddBuyPolicyError(username, storeId));
        //if(!buyPolicyRepository.buyPolicyExists(policyId))
        //    throw new IllegalArgumentException(Error.makeBuyPolicyWithIdDoesNotExistError(policyId));
        BuyPolicy policy = buyPolicyRepository.findBuyPolicyByID(policyId);

        Set<Integer> policyProducts = policy.getPolicyProductIds();
        if(!storeFacade.areProductsInStore(storeId, policyProducts))
            throw new IllegalArgumentException(Error.makePolicyProductsNotInStore(storeId, policyProducts, policyId));
    }

    public void addBuyPolicyToStore(String username, int storeId, int policyId) {
        synchronized (mapper) {
            checkAddBuyPolicyToStore(username, storeId, policyId);
            if (!mapper.containsKey(storeId))
                mapper.put(storeId, buyPolicyRepository.createManager(this,storeId));
            mapper.get(storeId).addBuyPolicy(policyId);
        }
    }

    public void addLawBuyPolicyToStore(String username, int storeId, int policyId) {
        synchronized (mapper) {
            checkAddBuyPolicyToStore(username, storeId, policyId);
            if (!mapper.containsKey(storeId))
                mapper.put(storeId, buyPolicyRepository.createManager(this,storeId));
            mapper.get(storeId).addLawBuyPolicy(policyId);
        }
    }

    public synchronized void removePolicyFromStore(String username, int storeId, int policyId) {
        if (!storeFacade.isStoreActive(storeId))
            throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));
        if (!storeFacade.hasPermission(username, storeId, Permission.REMOVE_BUY_POLICY))
            throw new IllegalArgumentException(Error.makeStoreUserCannotRemoveBuyPolicyError(username, storeId));
        if (!mapper.containsKey(storeId))
            mapper.put(storeId, buyPolicyRepository.createManager(this,storeId));
        mapper.get(storeId).removeBuyPolicy(policyId);
    }

    private boolean hasPermission(String username, Permission permission) {
        for(int storeId : storeFacade.getAllStoreIds())
            if(storeFacade.hasPermission(username, storeId, permission))
                return true;
        return false;
    }

    // --------------------------------------------------------------------------------

    public Set<String> canBuy(int storeId, List<CartItemDTO> cart, String username) {
        if (!mapper.containsKey(storeId))
            mapper.put(storeId, buyPolicyRepository.createManager(this,storeId));
        BuyPolicyManager buyPolicyManager = mapper.get(storeId);
        MemberDTO user;
        try {
            user = userFacade.getMemberDTO(username);
        }
        catch (Exception e) {
            user = null;
        }
        return buyPolicyManager.canBuy(cart, getProductDTOs(cart), user);
    }

    private Map<Integer, ProductDTO> getProductDTOs(List<CartItemDTO> cart) {
        Map<Integer, ProductDTO> res = new HashMap<>();
        for(CartItemDTO item : cart) {
            int productId = item.getProductId();
            res.put(productId, productFacade.getProductDTO(productId));
        }
        return res;
    }

    public boolean hasPolicy(int storeId, int policyId) {
        synchronized (mapper) {
            if (!mapper.containsKey(storeId))
                mapper.put(storeId, buyPolicyRepository.createManager(this,storeId));
            BuyPolicyManager manager = mapper.get(storeId);
            return manager.hasPolicy(policyId);
        }
    }

    public List<PolicyDescriptionDTO> getStorePolicyDescriptions(int storeId) throws Exception {
        if (!mapper.containsKey(storeId))
            mapper.put(storeId, buyPolicyRepository.createManager(this,storeId));
        BuyPolicyManager manager = mapper.get(storeId);
        List<PolicyDescriptionDTO> descs = new LinkedList<>();
        for(int id : manager.getAllPolicyIds()){
            descs.add(new PolicyDescriptionDTO(id,getPolicyDescription(id)));
        }
        return descs;
    }

    public void clear(){
        for(int storeId : mapper.keySet()){
            mapper.get(storeId).clear();
        }
        this.mapper = new HashMap<Integer, BuyPolicyManager>();
        buyPolicyRepository.clear();
    }
}
