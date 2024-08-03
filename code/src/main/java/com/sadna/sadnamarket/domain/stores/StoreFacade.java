package com.sadna.sadnamarket.domain.stores;

import java.time.LocalTime;

import java.util.*;
import java.util.concurrent.TimeoutException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicyFacade;
import com.sadna.sadnamarket.domain.buyPolicies.BuyType;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.orders.OrderDTO;
import com.sadna.sadnamarket.domain.orders.OrderFacade;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.domain.users.Permission;
import com.sadna.sadnamarket.domain.users.UserFacade;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import com.sadna.sadnamarket.service.Error;

public class StoreFacade {
    private UserFacade userFacade;
    private ProductFacade productFacade;
    private OrderFacade orderFacade;
    private BuyPolicyFacade buyPolicyFacade;
    private DiscountPolicyFacade discountPolicyFacade;
    private IStoreRepository storeRepository;

    public StoreFacade(IStoreRepository storeRepository) {
        // this.userFacade = userFacade;
        // this.productFacade = productFacade;
        // this.orderFacade = orderFacade;
        this.storeRepository = storeRepository;
    }

    public StoreFacade() {}

    public void setStoreRepository(IStoreRepository repo) {
        this.storeRepository = repo;
    }

    public void setUserFacade(UserFacade userFacade) {
        this.userFacade = userFacade;
    }

    public void setProductFacade(ProductFacade productFacade) {
        this.productFacade = productFacade;
    }

    public void setOrderFacade(OrderFacade orderFacade) {
        this.orderFacade = orderFacade;
    }

    public void setBuyPolicyFacade(BuyPolicyFacade buyPolicyFacade) {
        this.buyPolicyFacade = buyPolicyFacade;
    }

    public void setDiscountPolicyFacade(DiscountPolicyFacade discountPolicyFacade) {
        this.discountPolicyFacade = discountPolicyFacade;
    }

    public int createStore(String founderUserName, String storeName, String address, String email, String phoneNumber)  {
        if (!userFacade.isLoggedIn(founderUserName))
            throw new IllegalArgumentException(
                    Error.makeStoreUserHasToBeLoggedInError(founderUserName));

        int storeId = storeRepository.addStore(founderUserName, storeName, address, email, phoneNumber);
        userFacade.addStoreFounder(founderUserName, storeId);

        // adding default buy policies (laws)
        List<BuyType> buyTypes1 = new ArrayList<>();
        List<BuyType> buyTypes2 = new ArrayList<>();
        buyTypes1.add(BuyType.immidiatePurchase);
        buyTypes2.add(BuyType.immidiatePurchase);
        try {
            // this will not throw an exception since all the parameters are legal
            int policyId1 = buyPolicyFacade.createCategoryAgeLimitBuyPolicy("Alcohol", buyTypes1, 18, -1, founderUserName);
            int policyId2 = buyPolicyFacade.createCategoryHourLimitBuyPolicy("Alcohol", buyTypes2, LocalTime.of(6, 0), LocalTime.of(23, 0), founderUserName);

            // default buy policies (laws)
            buyPolicyFacade.addLawBuyPolicyToStore(founderUserName, storeId, policyId1);
            buyPolicyFacade.addLawBuyPolicyToStore(founderUserName, storeId, policyId2);

            //adding kind of null discount
            int discountPolicyID = discountPolicyFacade.createDefaultDiscountPolicy(0, founderUserName);
            discountPolicyFacade.addDiscountPolicyToStore(storeId, discountPolicyID, founderUserName);
        }
        catch (Exception ignored) {
            String msg = ignored.getMessage();
        }
        return storeId;
    }

    public int addProductToStore(String username, int storeId, String productName, int productQuantity,
            double productPrice, String category, double rank, double productWeight, String description) {
        if (!hasPermission(username, storeId, Permission.ADD_PRODUCTS))
            throw new IllegalArgumentException(Error.makeStoreUserCannotAddProductError(username, storeId));
        //if (!storeRepository.storeExists(storeId))
        //    throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
        if (!isStoreActive(storeId))
            throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));

        int newProductId = productFacade.addProduct(storeId, productName, productPrice, category, rank, productWeight, description);
        storeRepository.addProductToStore(storeId, newProductId, productQuantity);
        return newProductId;
    }

    public int addProductToStore(String username, int storeId, String productName, int productQuantity,
                                 double productPrice, String category, double rank, double productWeight) {
        if (!hasPermission(username, storeId, Permission.ADD_PRODUCTS))
            throw new IllegalArgumentException(Error.makeStoreUserCannotAddProductError(username, storeId));
        //if (!storeRepository.storeExists(storeId))
        //    throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
        if (!isStoreActive(storeId))
            throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));

        int newProductId = productFacade.addProduct(storeId, productName, productPrice, category, rank, productWeight,"");
        //storeRepository.findStoreByID(storeId).addProduct(newProductId, productQuantity);
        storeRepository.addProductToStore(storeId, newProductId, productQuantity);
        return newProductId;
    }

    public int deleteProduct(String username, int storeId, int productId) {
        if (!hasPermission(username, storeId, Permission.DELETE_PRODUCTS))
            throw new IllegalArgumentException(Error.makeStoreUserCannotDeleteProductError(username, storeId));


        /*Store store = storeRepository.findStoreByID(storeId);
        synchronized (store.getProductAmounts()) {
            store.deleteProduct(productId);
            productFacade.removeProduct(storeId, productId);
        }
        return productId;*/
        storeRepository.deleteProductFromStore(storeId, productId);
        productFacade.removeProduct(storeId, productId);
        return productId;
    }

    public int updateProduct(String username, int storeId, int productId, String newProductName, int newQuantity,
            double newPrice, String newCategory, double newRank, String newDesc) {
        if (!hasPermission(username, storeId, Permission.UPDATE_PRODUCTS))
            throw new IllegalArgumentException(Error.makeStoreUserCannotUpdateProductError(username, storeId));

        storeRepository.updateProductAmountInStore(storeId, productId, newQuantity);
        productFacade.updateProduct(storeId, productId, newProductName, newPrice, newCategory, newRank, newDesc);
        return productId;
    }

    public int updateProduct(String username, int storeId, int productId, String newProductName, int newQuantity,
            double newPrice, String newCategory, double newRank) {
        updateProductAmount(username, storeId, productId, newQuantity);
        productFacade.updateProduct(storeId, productId, newProductName, newPrice, newCategory, newRank);
        return productId;
    }

    public int updateProductAmount(String username, int storeId, int productId, int newQuantity) {
        if (!hasPermission(username, storeId, Permission.UPDATE_PRODUCTS))
            throw new IllegalArgumentException(Error.makeStoreUserCannotUpdateProductError(username, storeId));

        storeRepository.updateProductAmountInStore(storeId, productId, newQuantity);
        return productId;
    }

    public void sendStoreOwnerRequest(String currentOwnerUsername, String newOwnerUsername, int storeId) {
        Store store = storeRepository.findStoreByID(storeId);
        if(store.isStoreOwner(newOwnerUsername)){
            throw new IllegalArgumentException(Error.makeStoreUserAlreadyOwnerError(newOwnerUsername, storeId));
        }
        if (!canAddOwnerToStore(storeId, currentOwnerUsername, newOwnerUsername))
            throw new IllegalArgumentException(Error.makeStoreUserCannotAddOwnerError(currentOwnerUsername, newOwnerUsername, storeId));

        synchronized (storeRepository) {
            //if (!storeRepository.storeExists(storeId))
            //    throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            if (!store.getIsActive())
                throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));

            userFacade.addOwnerRequest(currentOwnerUsername, newOwnerUsername, storeId);
        }
    }

    public void sendStoreManagerRequest(String currentOwnerUsername, String newManagerUsername, int storeId) {
        Store store = storeRepository.findStoreByID(storeId);
        if(store.isStoreOwner(newManagerUsername)){
            throw new IllegalArgumentException(Error.makeStoreUserAlreadyOwnerError(newManagerUsername, storeId));
        }
        if(store.isStoreManager(newManagerUsername)){
            throw new IllegalArgumentException(Error.makeStoreUserAlreadyManagerError(newManagerUsername, storeId));
        }
        if (!canAddManagerToStore(storeId, currentOwnerUsername, newManagerUsername))
            throw new IllegalArgumentException(Error.makeStoreUserCannotAddManagerError(currentOwnerUsername, newManagerUsername, storeId));

        synchronized (storeRepository) {
            //if (!storeRepository.storeExists(storeId))
            //    throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            if (!store.getIsActive())
                throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));

            userFacade.addManagerRequest(currentOwnerUsername, newManagerUsername, storeId);
        }
    }

    public void addStoreOwner(String newOwnerUsername, int storeId) {
        storeRepository.addOwnerToStore(newOwnerUsername,storeId);
    }

    public void addStoreManager(String newManagerUsername, int storeId) {
        storeRepository.addManagerToStore(newManagerUsername,storeId);
    }

    public void addManagerPermission(String currentOwnerUsername, String newManagerUsername, int storeId,
            Set<Permission> permission) {
        Store store = storeRepository.findStoreByID(storeId);
        if(!store.isStoreManager(newManagerUsername)){
            throw new IllegalArgumentException(Error.makeMemberUserHasNoRoleError());
        }
        if (!canAddPermissionToManager(storeId, currentOwnerUsername, newManagerUsername))
            throw new IllegalArgumentException(Error.makeStoreUserCannotAddManagerPermissionsError(currentOwnerUsername, newManagerUsername, storeId));

        synchronized (storeRepository) {
            //if (!storeRepository.storeExists(storeId))
            //    throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            if (!store.getIsActive())
                throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));

            Set<Permission> currPermissions = new HashSet<>();
            for(int i : userFacade.getMemberPermissions(newManagerUsername, storeId)){
                currPermissions.add(Permission.getEnumByInt(i));
            }
            Set<Permission> toRemove = new HashSet(currPermissions);
            toRemove.removeAll(permission);

            Set<Permission> toAdd = new HashSet(permission);
            toAdd.removeAll(currPermissions);

            for (Permission p : toRemove) {
                userFacade.removePremssionFromStore(currentOwnerUsername, newManagerUsername, storeId, p);
            }
            for (Permission p : toAdd) {
                userFacade.addPremssionToStore(currentOwnerUsername, newManagerUsername, storeId, p);
            }
            userFacade.notify(newManagerUsername, "Your permissions in store with ID " + storeId + " have been altered");
        }
    }

    public void removeManagerPermission(String currentOwnerUsername, String newManagerUsername, int storeId, Permission permission) {
        if(!getIsManager(currentOwnerUsername, storeId, newManagerUsername)){
            throw new IllegalArgumentException(Error.makeMemberUserHasNoRoleError());
        }
        if (!canAddPermissionToManager(storeId, currentOwnerUsername, newManagerUsername))
            throw new IllegalArgumentException(Error.makeStoreUserCannotAddManagerPermissionsError(currentOwnerUsername, newManagerUsername, storeId));

        synchronized (storeRepository) {
            if (!storeRepository.storeExists(storeId))
                throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
            if (!isStoreActive(storeId))
                throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));

            userFacade.removePremssionFromStore(currentOwnerUsername, newManagerUsername, storeId, permission);
        }
    }

    public boolean closeStore(String username, int storeId) {
        /*Store store = storeRepository.findStoreByID(storeId);
        if (!store.getFounderUsername().equals(username))
            throw new IllegalArgumentException(Error.makeStoreUserCannotCloseStoreError(username, storeId));

        store.closeStore();
        storeRepository.saveStore(store);*/

        storeRepository.changeStoreState(username, storeId, false, userFacade);
        return true;
    }

    public boolean reopenStore(String username, int storeId) {
        /*Store store = storeRepository.findStoreByID(storeId);
        if (!store.getFounderUsername().equals(username))
            throw new IllegalArgumentException(Error.makeStoreUserCannotCloseStoreError(username, storeId));

        store.reopenStore();
        storeRepository.saveStore(store);

        String msg = String.format("The store \"%s\" was reopened.", store.getStoreInfo().getStoreName());
        Set<String> ownerUsernames = store.getOwnerUsernames();
        Set<String> managerUsernames = store.getManagerUsernames();
        for (String ownerUsername : ownerUsernames) {
            userFacade.notify(ownerUsername, msg);
        }
        for (String managerUsername : managerUsernames) {
            userFacade.notify(managerUsername, msg);
        }*/
        storeRepository.changeStoreState(username, storeId, true, userFacade);
        return true;
    }

    public boolean isStoreActive(int storeId) {
        return storeRepository.findStoreByID(storeId).getIsActive();
    }

    public List<MemberDTO> getOwners(String username, int storeId) {
        Store store = storeRepository.findStoreByID(storeId);
        if (!store.isStoreOwner(username))
            throw new IllegalArgumentException(Error.makeStoreUserCannotGetRolesInfoError(username,storeId));
        // if(!isStoreActive(storeId))
        // throw new IllegalArgumentException(String.format("A store with id %d is not
        // active.", storeId));

        Set<String> ownerUsernames = store.getOwnerUsernames();
        List<MemberDTO> owners = new ArrayList<>();
        for (String ownerUsername : ownerUsernames) {
            owners.add(userFacade.getMemberDTO(ownerUsername));
        }
        return owners;
    }

    public List<MemberDTO> getManagers(String username, int storeId) {
        Store store = storeRepository.findStoreByID(storeId);
        if (!store.isStoreOwner(username))
            throw new IllegalArgumentException(Error.makeStoreUserCannotGetRolesInfoError(username, storeId));
        // if(!isStoreActive(storeId))
        // throw new IllegalArgumentException(String.format("A store with id %d is not
        // active.", storeId));

        Set<String> managerUsernames = store.getManagerUsernames();
        List<MemberDTO> managers = new ArrayList<>();
        for (String managerUsername : managerUsernames) {
            managers.add(userFacade.getMemberDTO(managerUsername));
        }
        return managers;
    }

    /*public List<MemberDTO> getSellers(String username, int storeId) {
        if (!storeRepository.findStoreByID(storeId).isStoreOwner(username))
            throw new IllegalArgumentException(String.format(
                    "A user %s is not an owner of store %d and can not request roles information.", username, storeId));
        // if(!isStoreActive(storeId))
        // throw new IllegalArgumentException(String.format("A store with id %d is not
        // active.", storeId));

        List<String> sellerUsernames = storeRepository.findStoreByID(storeId).getSellerUsernames();
        List<MemberDTO> sellers = new ArrayList<>();
        for (String sellerUsername : sellerUsernames) {
            sellers.add(userFacade.getMemberDTO(sellerUsername));
        }
        return sellers;
    }*/

    /*
     * public String getStoreOrderHisotry( String username, int storeId) throws
     * JsonProcessingException {
     * if(!storeRepository.storeExists(storeId))
     * throw new
     * IllegalArgumentException(String.format("A store with id %d does not exist.",
     * storeId));
     * if(!storeRepository.findStoreByID(storeId).isStoreOwner(username))
     * throw new IllegalArgumentException(String.
     * format("A user %s is not an owner of store %d and can not request order history."
     * , username, storeId));
     * 
     * List<OrderDTO> orders = OrderController.getInstance().getOrders(storeId);
     * String orderHistory = String.format("Order History of store %d:\n", storeId);
     * 
     * int orderIndex = 1;
     * 
     * for(OrderDTO order : orders) {
     * orderHistory +=
     * "------------------------------------------------------------\n";
     * orderHistory += getOrderInfo(order, orderIndex);
     * orderHistory += getProductsInfo(order);
     * orderHistory +=
     * "------------------------------------------------------------\n\n";
     * orderIndex++;
     * }
     * 
     * if(orderIndex == 1) {
     * orderHistory += "There are no orders.\n";
     * }
     * 
     * return orderHistory;
     * }
     */

    public List<ProductDataPrice> getStoreOrderHistory(String username, int storeId) throws JsonProcessingException {
        if (!storeRepository.findStoreByID(storeId).isStoreOwner(username) && !userFacade.isSystemManager(username))
            throw new IllegalArgumentException(Error.makeStoreUserCannotStoreHistoryError(username, storeId));
        List<ProductDataPrice> order = orderFacade.getOrders(storeId);
        return order;
    }

    public List<OrderDTO> getStoreOrderHistoryDTO(String username, int storeId) throws JsonProcessingException {
        if (!storeRepository.findStoreByID(storeId).isStoreOwner(username) && !userFacade.isSystemManager(username))
            throw new IllegalArgumentException(Error.makeStoreUserCannotStoreHistoryError(username, storeId));
        List<OrderDTO> orders = orderFacade.getOrderHistory(storeId);
        return orders;
    }

    public StoreDTO getStoreInfo(String username, int storeId) {
        Store store = storeRepository.findStoreByID(storeId);
        synchronized (store) {
            if (!store.getIsActive()) {
                if (!store.isStoreOwner(username) && !userFacade.isSystemManager(username))
                    throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));
            }

            return storeRepository.getStoreDTO(storeId);
        }
    }

    public List<PolicyDescriptionDTO> getStoreDiscountDescriptions(String username, int storeId) throws Exception {
        Store store = storeRepository.findStoreByID(storeId);
        synchronized (store) {
            if (!store.getIsActive()) {
                if (!store.isStoreOwner(username) && !userFacade.isSystemManager(username))
                    throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));
            }

            return discountPolicyFacade.getStoreDiscountDescriptions(storeId);
        }
    }

    public List<PolicyDescriptionDTO> getStoreBuyPolicyDescriptions(String username, int storeId) throws Exception {
        Store store = storeRepository.findStoreByID(storeId);
        synchronized (store) {
            if (!store.getIsActive()) {
                if (!store.isStoreOwner(username) && !userFacade.isSystemManager(username))
                    throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));
            }

            return buyPolicyFacade.getStorePolicyDescriptions(storeId);
        }
    }
  
  public ProductDTO getProductInfo(String username, int productId) {
        int storeId = getStoreOfProduct(productId);
        if(storeId < 0){
            throw new IllegalArgumentException(Error.makeProductDoesntExistError(productId));
        }
        Store store = storeRepository.findStoreByID(storeId);
        synchronized (store) {
            if (!store.getIsActive()) {
                if (!store.isStoreOwner(username) && !userFacade.isSystemManager(username))
                    throw new IllegalArgumentException(Error.makeStoreOfProductIsNotActiveError(productId));
            }
        }

        return productFacade.getProductDTO(productId);
    }

    private int getStoreOfProduct(int produceId){
        for(int storeId : storeRepository.getAllStoreIds()){
            if(hasProductInStock(storeId, produceId, 0)){
                return storeId;
            }
        }
        return -1;
    }

    public Map<ProductDTO, Integer> getProductsInfoAndFilter(String username, int storeId, String productName, String category,
                                                             double price, double minProductRank) throws JsonProcessingException {
        Store store = storeRepository.findStoreByID(storeId);

        if (!store.getIsActive()) {
            if (!store.isStoreOwner(username) && !store.isStoreManager(username)) {
                throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));
               }
        }

        return storeRepository.getProductsInfoAndFilter(productFacade, storeId, productName, category, price, minProductRank);
    }

    public void setStoreBankAccount(String ownerUsername, int storeId, BankAccountDTO bankAccount) {
        Store store = storeRepository.findStoreByID(storeId);
        synchronized (store) {
            if (!store.isStoreOwner(ownerUsername))
                throw new IllegalArgumentException(Error.makeStoreUserCannotSetBankAccountError(ownerUsername, storeId));

            bankAccount.setStore(store);
            storeRepository.setStoreBankAccount(storeId, bankAccount);
        }
    }

    public BankAccountDTO getStoreBankAccount(int storeId) {
        //Store store = storeRepository.findStoreByID(storeId);
        //return store.getBankAccount();
        return storeRepository.getStoreBankAccount(storeId);
    }

    public int getProductAmount(int storeId, int productId) {
        //Store store = storeRepository.findStoreByID(storeId);

        if (!isStoreActive(storeId))
            throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(storeId));

        return storeRepository.getProductAmountInStore(storeId, productId);
    }

    /*public String addSeller(int storeId, String adderUsername, String sellerUsername) {
        if (!canAddSellerToStore(storeId, adderUsername, sellerUsername))
            throw new IllegalArgumentException(
                    String.format("A user %s can not add sellers to store with id %d.", adderUsername, storeId));
        if (!userFacade.isExist(sellerUsername))
            throw new IllegalArgumentException(String.format("A user %s does not exist.", sellerUsername));

        storeRepository.findStoreByID(storeId).addSeller(sellerUsername);
        return sellerUsername;
    }*/

    public int addOrderId(int storeId, int orderId) {
        storeRepository.addOrderIdToStore(storeId, orderId);
        return orderId;
    }

    private Map<Integer, List<CartItemDTO>> getCartByStore(List<CartItemDTO> cart) {
        Map<Integer, List<CartItemDTO>> cartByStore = new HashMap<>();
        for (CartItemDTO item : cart) {
            List<CartItemDTO> storeItems = cartByStore.getOrDefault(item.getStoreId(), new ArrayList<>());
            storeItems.add(item);
            cartByStore.put(item.getStoreId(), storeItems);
        }
        return cartByStore;
    }

    public void checkCart(String username, List<CartItemDTO> cart) {
        Map<Integer, List<CartItemDTO>> cartByStore = getCartByStore(cart);
        Set<String> error = new HashSet<>();
        for (int storeId : cartByStore.keySet()) {
            //Store store = storeRepository.findStoreByID(storeId);
            //synchronized (store.getProductAmounts()) {
                //Set<String> newError1 = store.checkCart(cartByStore.get(storeId));
            Set<String> newError1 = storeRepository.checkCartInStore(storeId, cartByStore.get(storeId));
            if (newError1.size() != 0)
                error.addAll(newError1);
            Set<String> newError2 = buyPolicyFacade.canBuy(storeId, cartByStore.get(storeId), username);
            if (newError2.size() != 0)
                error.addAll(newError2);
            //}
        }

        if (error.size() != 0) {
            throw new IllegalArgumentException(String.join("\n", error));
        }

    }

    public boolean getIsOwner(String actorUsername, int storeId, String infoUsername){
        //if (!hasPermission(actorUsername, storeId, Permission.VIEW_ROLES) && !storeRepository.findStoreByID(storeId).isStoreOwner(actorUsername))
        //    throw new IllegalArgumentException(Error.makeStoreUserCannotGetRolesInfoError(actorUsername, storeId));
        Store store = storeRepository.findStoreByID(storeId);
        return store.isStoreOwner(infoUsername);
    }

    public boolean getIsFounder(String actorUsername, int storeId, String infoUsername){
        //if (!hasPermission(actorUsername, storeId, Permission.VIEW_ROLES) && !storeRepository.findStoreByID(storeId).isStoreOwner(actorUsername))
        //    throw new IllegalArgumentException(Error.makeStoreUserCannotGetRolesInfoError(actorUsername, storeId));
        Store store = storeRepository.findStoreByID(storeId);
        return store.isStoreOwner(infoUsername) && store.getFounderUsername().equals(actorUsername);
    }

    public boolean hasProductInStock(int storeId, int productId, int amount){
        if (!storeRepository.storeExists(storeId))
            throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
        /*Store store = storeRepository.findStoreByID(storeId);
        return store.hasProductInAmount(productId, amount);*/
        return storeRepository.hasProductInStock(storeId, productId, amount);
    }

    public boolean getIsManager(String actorUsername, int storeId, String infoUsername){
        //if (!storeRepository.storeExists(storeId))
        //    throw new IllegalArgumentException(Error.makeStoreNoStoreWithIdError(storeId));
        Store store = storeRepository.findStoreByID(storeId);
        return store.isStoreManager(infoUsername);
    }

    public synchronized void updateStock(String username, List<CartItemDTO> cart) {
        checkCart(username, cart);

        Map<Integer, List<CartItemDTO>> cartByStore = getCartByStore(cart);
        for (int storeId : cartByStore.keySet()) {
            Set<String> error = storeRepository.updateStockInStore(storeId, cartByStore.get(storeId));
            if(error.size() != 0) {
                throw new IllegalArgumentException(String.join("\n", error));
            }

            Store store = storeRepository.findStoreByID(storeId);
            for(String owner : store.getOwnerUsernames()){
                userFacade.notify(owner, "User " + (username != null ? username+" " : "") + "made a purchase in your store " + store.getStoreInfo().getStoreName());
            }
        }
    }

    // return a map from store id to a List that coontain object thats stores : id,
    // amount, original price and new price
    public List<ProductDataPrice> calculatePrice(String username, List<CartItemDTO> cart) throws Exception{
        List<ProductDataPrice> res = new ArrayList<>();
        Map<Integer, List<CartItemDTO>> cartByStore = getCartByStore(cart);
        for (int storeId : cartByStore.keySet()) {
            res.addAll(discountPolicyFacade.calculatePrice(storeId, cartByStore.get(storeId)));
        }
        return res;
        }

    public boolean hasPermission(String username, int storeId, Permission permission) {
        if (!userFacade.isLoggedIn(username))
            return false;

        Store store = storeRepository.findStoreByID(storeId);
        if (store.isStoreOwner(username))
            return true;

        if (store.isStoreManager(username))
            return userFacade.checkPremssionToStore(username, storeId, permission);

        return false;
    }

    public boolean hasPermission(String actorUsername, String username, int storeId, Permission permission) {
        Store store = storeRepository.findStoreByID(storeId);
        if (!actorUsername.equals(username) && !store.isStoreOwner(actorUsername))
            throw new IllegalArgumentException(Error.makeUserCanNotCheckPermissionOfUserError(actorUsername, username, storeId));

        return store.isStoreOwner(username) ||
                (store.isStoreManager(username) && userFacade.checkPremssionToStore(username, storeId, permission));
    }

    private boolean canAddOwnerToStore(int storeId, String currOwnerUsername, String newOwnerUsername) {
        return !storeRepository.findStoreByID(storeId).isStoreOwner(newOwnerUsername) &&
                hasPermission(currOwnerUsername, storeId, Permission.ADD_OWNER) &&
                userFacade.isExist(newOwnerUsername);
    }

    private boolean canAddManagerToStore(int storeId, String currOwnerUsername, String newManagerUsername) {
        return hasPermission(currOwnerUsername, storeId, Permission.ADD_MANAGER) &&
                userFacade.isExist(newManagerUsername) &&
                (!storeRepository.findStoreByID(storeId).isStoreManager(newManagerUsername));
    }

    private boolean canAddPermissionToManager(int storeId, String currOwnerUsername, String newManagerUsername) {
        return (hasPermission(currOwnerUsername, storeId, Permission.ADD_MANAGER) ||
                storeRepository.findStoreByID(storeId).isStoreOwner(currOwnerUsername)) &&
                userFacade.isExist(newManagerUsername) &&
                (storeRepository.findStoreByID(storeId).isStoreManager(newManagerUsername));
    }


    /*private boolean canAddSellerToStore(int storeId, String currOwnerUsername, String newSellerUsername) {
        return hasPermission(currOwnerUsername, storeId, Permission.ADD_SELLER) &&
                userFacade.isExist(newSellerUsername) &&
                (!storeRepository.findStoreByID(storeId).isSeller(newSellerUsername));
    }*/

    public StoreInfo getStoreInfo(int storeId) {
        return storeRepository.findStoreByID(storeId).getStoreInfo();
    }

    public Set<Integer> getAllStoreIds() {
        return storeRepository.getAllStoreIds();
    }

    public List<Permission> getUserPermissions(String actorUsername, String username, int storeId) {
        Store store = storeRepository.findStoreByID(storeId);
        if (!actorUsername.equals(username) && !store.isStoreOwner(actorUsername))
            throw new IllegalArgumentException(Error.makeUserCanNotCheckPermissionOfUserError(actorUsername, username, storeId));

        return userFacade.getMemberPermissionsEnum(username, storeId);
    }

    public boolean areProductsInStore(int storeId, Set<Integer> productIds) {
        return storeRepository.areProductsInStore(storeId, productIds);
    }

    public StoreDTO getStoreByName(String storeName, String username) {
        Store store = storeRepository.findStoreByName(storeName);
        synchronized (store) {
            if (!isStoreActive(store.getStoreId())) {
                if (!store.isStoreOwner(username) && !userFacade.isSystemManager(username))
                    throw new IllegalArgumentException(Error.makeStoreWithIdNotActiveError(store.getStoreId()));
            }

            return storeRepository.getStoreDTO(store.getStoreId());
        }
    }
}
