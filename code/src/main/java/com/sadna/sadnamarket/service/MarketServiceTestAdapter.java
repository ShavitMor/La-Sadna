package com.sadna.sadnamarket.service;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.DeserializationFeature;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import com.sadna.sadnamarket.domain.users.NotificationDTO;
import com.sadna.sadnamarket.domain.users.Permission;
import org.springframework.beans.factory.annotation.Autowired;

import java.time.LocalDate;
import java.util.*;

public class MarketServiceTestAdapter {
    // this is an adapter for the tests
    // please add the true configuration here when you make additions to the actual
    // service
    // thanks
    @Autowired
    MarketService testingService;

    ObjectMapper objectMapper = new ObjectMapper();

    // attention
    // String userId is actually username
    // i am lazy

    public boolean reset() {
        this.testingService = MarketService.getNewMemoryInstance();
        return true;
    }


    public void injectRealtime(RealtimeService realtimeService){
        this.testingService.injectRealtime(realtimeService);
    }

    public Response guestEnterSystem() {
        return testingService.enterAsGuest();
    }

    public Response guestLeaveSystem(String uuid) {
        return testingService.exitGuest(Integer.parseInt(uuid));
    }

    public Response guestCartExists(String uuid) {
        return Response.createResponse(false, "false");
    }

    public Response makeSystemManager(String username) {
        return testingService.setSystemAdminstor(username);
    }

    public Response signUp(String uuid, String email, String username, String passwordHash) {
        Response resp = testingService.register(username, passwordHash, "John", "Doe", email, "052-052-0520",LocalDate.of(1988, 11, 7));
        if (resp.getError()) {
            return resp;
        }
        return testingService.login(username, passwordHash);
    }

    public Response memberExists(String username) {
        return testingService.memberExists(username);
    }

    public Response authenticate(String token, String username) {
        return testingService.authenticate(token, username);
    }

    public Response login(String username, String passwordHash) {
        return testingService.login(username, passwordHash);
    }

    public Response logout(String userName) {
        return testingService.logout(userName);
    }

    public Response getStoreData(String token, String userId, int storeId) throws JsonProcessingException {
        // empty token and -1 userId if guest
        /*
         * StoreDTO dto = new StoreDTO(4, true, "TestStore", 4, "Lehavim",
         * "stam@gmail.com", "0542106532", null, null, new HashMap<Integer,
         * Integer>(),"Founder", new LinkedList<>(), new LinkedList<>(), new
         * LinkedList<>(), new LinkedList<>(), new LinkedList<>(), new LinkedList<>());
         * return Response.createResponse(false, objectMapper.writeValueAsString(dto));
         */

        return testingService.getStoreInfo(token, userId, storeId);
    }

    public Response getProductData(String token, String userId, int productId) throws JsonProcessingException {
        //empty token and -1 userId if guest //THIS ONE IS MISSING
        return testingService.getProductInfo(token, userId, productId);
    }

    public Response searchProduct(String productName, double productPriceMin, double productPriceMax,
            String productCategory, int storeRating, int productRating) throws JsonProcessingException {
        return testingService.getFilteredProducts( null, productName, productPriceMin, productPriceMax, productCategory, productRating);
    }

    public Response searchProductInStore(int storeId, String productName, double productPriceMin,
            double productPriceMax, String productCategory, int productRating) throws JsonProcessingException {
        /*
         * ProductDTO dto = new ProductDTO(5, productName, productPriceMin,
         * productCategory);
         * List<ProductDTO> dtoList = new LinkedList<>();
         * dtoList.add(dto);
         * return Response.createResponse(false,
         * objectMapper.writeValueAsString(dtoList));
         */

        return testingService.getStoreProductsInfo(null, null, storeId, null, productCategory, productPriceMax, productRating);
    }

    public Response addProductToBasketGuest(String uuid, int storeId, int productId, int amount) {
        return testingService.addProductToCart(Integer.parseInt(uuid), storeId, productId, amount);
    }

    public Response addProductToBasketMember(String token, String userId, int storeId, int productId, int amount) {
        return testingService.addProductToCart(userId, storeId, productId, amount);
    }

    public Response getGuestBasket(String uuid, int storeId) throws JsonProcessingException {
        Response resp = testingService.getUserCart(Integer.parseInt(uuid));
        List<CartItemDTO> items = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<CartItemDTO>>() {
        });
        Map<Integer, Integer> productToAmount = new HashMap<>();
        for (CartItemDTO item : items) {
            if (item.getStoreId() == storeId) {
                productToAmount.put(item.getProductId(), item.getAmount());
            }
        }
        return Response.createResponse(false, objectMapper.writeValueAsString(productToAmount));
    }

    public Response setGuestBasketProductAmount(String uuid, int storeId, int productId, int amount)
            throws JsonProcessingException {
        return testingService.changeQuantityCart(Integer.parseInt(uuid), storeId, productId, amount);
    }

    public Response buyCartGuest(String uuid, CreditCardDTO creditDetails, AddressDTO address) {
        return testingService.purchaseCart(Integer.parseInt(uuid), creditDetails, address);
    }

    public Response buyCartMember(String token, String userId, CreditCardDTO creditDetails, AddressDTO address) {
        return testingService.purchaseCart(userId, creditDetails, address);
    }

    public Response openStore(String token, String userId, String storeName) {
        // return Response.createResponse(false, "4"); //returns store id
        return testingService.createStore(token, userId, storeName, "Beer Sheva", "coolio@gmail.com", "0546102344");
    }

    public Response addProductToStore(String token, String userId, int storeId, ProductDTO productDetails) {
        // return Response.createResponse(false, "5"); //returns product id
        return testingService.addProductToStore(token, userId, storeId, productDetails.getProductName(), 100,
                productDetails.getProductPrice(), productDetails.getProductCategory(), productDetails.getProductRank(), productDetails.getProductWeight(),productDetails.getDescription());
    }

    public Response removeProductFromStore(String token, String userId, int storeId, int productId) {
        // return Response.createResponse(false, "true");
        return testingService.deleteProductFromStore(token, userId, storeId, productId);
    }

    public Response editStoreProduct(String token, String userId, int storeId, int productId,
            ProductDTO productDetails) {
        // return Response.createResponse(false, "true");
        return testingService.updateProductInStore(token, userId, storeId, productId, productDetails.getProductName(), 200,
                productDetails.getProductPrice(), productDetails.getProductCategory(), 3,productDetails.getDescription());
    }

    public Response getStoreProductAmount(int storeId, int productId) {
        // return Response.createResponse(false, "1");
        return testingService.getStoreProductAmount(null, null, storeId, productId);
    }

    public Response setStoreProductAmount(String token, String username, int storeId, int productId, int amount) {
        // return Response.createResponse(false, "true");
        return testingService.updateProductAmountInStore(token, username, storeId, productId, amount);
    }

    public Response appointOwner(String token, String userId, int storeId, String appointedUserId) {
        // return Response.createResponse(false, "true");
        return testingService.sendStoreOwnerRequest(token, userId, appointedUserId, storeId);
    }

    public Response appointManager(String token, String userId, int storeId, String appointedUserId,
            List<Integer> permissions) {
        // return Response.createResponse(false, "true");
        return testingService.sendStoreManagerRequest(token, userId, appointedUserId, storeId);
    }

    public Response acceptOwnerAppointment(String token, String appointedUserId, int storeId, int requestId) {
        // return Response.createResponse(false, "true");
        return testingService.acceptRequest(appointedUserId, requestId);
    }

    public Response acceptManagerAppointment(String token, String appointedUserId, int storeId, int requestId) {
        // return Response.createResponse(false, "true");
        return testingService.acceptRequest(appointedUserId, requestId);
    }

    public Response rejectOwnerAppointment(String token, String appointedUserId, int storeId, String appointerid) {
        return testingService.rejectRequest(appointedUserId, storeId);
    }

    public Response rejectManagerAppointment(String token, String appointedUserId, int storeId, String appointerid) {
        return testingService.rejectRequest(appointedUserId, storeId);
    }

    public Response changeManagerPermissions(String token, String userId, String managerId, int storeId,
            List<Integer> newPermissions) {
        // return Response.createResponse(false, "true");
        Set<Permission> perms = new HashSet<>();
        for (Integer i : newPermissions) {
            for (Permission type : Permission.values()) {
                if (type.getValue() == i) {
                    perms.add(type);
                }
            }
        }
        return testingService.changeManagerPermission(token, userId, managerId, storeId, perms);
    }

    public Response closeStore(String token, String userId, int storeId) {
        // return Response.createResponse(false, "true");
        return testingService.closeStore(token, userId, storeId);
    }

    public Response reopenStore(String token, String userId, int storeId) {
        // return Response.createResponse(false, "true");
        return testingService.reopenStore(token, userId, storeId);
    }

    public Response getIsOwner(String token, String actorId, int storeId, String ownerId) {
        return testingService.getIsOwner(token, actorId, storeId, ownerId);
    }

    public Response getIsManager(String token, String actorId, int storeId, String managerId) {
        return testingService.getIsManager(token, actorId, storeId, managerId);
    }

    public Response getStoreOwners(String token, String actorId, int storeId) throws JsonProcessingException {
        // List<MemberDTO> users = new LinkedList<>();
        // return Response.createResponse(false,
        // objectMapper.writeValueAsString(users));
        return testingService.getOwners(token, actorId, storeId);
    }

    public Response getStoreManagers(String token, String actorId, int storeId) throws JsonProcessingException {
        // List<MemberDTO> users = new LinkedList<>();
        // return Response.createResponse(false,
        // objectMapper.writeValueAsString(users));
        return testingService.getManagers(token, actorId, storeId);
    }

    public Response getManagerPermissions(String token, String actorId, int storeId, String managerId)
            throws JsonProcessingException {
        Response resp = testingService.getManagerPermissions(token, actorId, managerId, storeId);
        if (resp.getError()) {
            return resp;
        }
        List<Permission> permissions = objectMapper.readValue(resp.getDataJson(),
                new TypeReference<List<Permission>>() {
                });
        List<Integer> required = new LinkedList<>();
        for (Permission permission : permissions) {
            required.add(permission.getValue());
        }
        return Response.createResponse(false, objectMapper.writeValueAsString(required));
    }

    public Response getStorePurchaseHistory(String token, String actorId, int storeId) throws JsonProcessingException {
        // List<OrderDTO> history = new LinkedList<>();
        // return Response.createResponse(false,
        // objectMapper.writeValueAsString(history));
        return testingService.getStoreOrderHistory(token, actorId, storeId);
    }

    public Response getUserPurchaseHistory(String token, String actorId, String userId) throws JsonProcessingException {
        return testingService.getOrderDTOHistory(userId);
    }

    public Response memberSetAddress(String token, String username, AddressDTO addressDTO) {
        return Response.createResponse(false, "true"); // THIS ONE IS MISSING
    }

    public Response setStoreBankAccount(String token, String username, int storeId, BankAccountDTO bankAccount) {
        return testingService.setStoreBankAccount(token, username, storeId, bankAccount);
    }

    public Response addPolicyAgainst(String token, String username, int storeId, int productId){
        Response resp = testingService.createProductAmountBuyPolicy(token, username, productId, new LinkedList<>(), 0, 0);
        int id = Integer.parseInt(resp.getDataJson());
        return testingService.addBuyPolicyToStore(token, username, storeId, id);
    }

    public Response getNotifications(String username){
        return testingService.getUserNotifications(username);
    }

    public int getFirstNotification(String username) throws JsonProcessingException {
        objectMapper.configure(DeserializationFeature.FAIL_ON_UNKNOWN_PROPERTIES, false);
        Response resp = getNotifications(username);
        if (resp.getError()) {
            return -1;
        }
        List<NotificationDTO> notifications = objectMapper.readValue(resp.getDataJson(),
                new TypeReference<List<NotificationDTO>>() {
                });
        return notifications.get(0).getId();
    }
    public Response checkIfSystemManager(String userName){
        return testingService.checkIfSystemManager(userName);
    }



    public void clear(){
        testingService.clear();
    }
}
