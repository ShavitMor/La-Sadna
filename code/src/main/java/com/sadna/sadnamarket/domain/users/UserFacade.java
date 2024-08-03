package com.sadna.sadnamarket.domain.users;
import java.time.LocalDate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.NoSuchElementException;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.sadna.sadnamarket.Config;
import com.sadna.sadnamarket.service.Error;
import com.sadna.sadnamarket.service.RealtimeService;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.orders.OrderDTO;
import com.sadna.sadnamarket.domain.orders.OrderFacade;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.payment.PaymentService;
import com.sadna.sadnamarket.domain.stores.StoreFacade;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.supply.OrderDetailsDTO;
import com.sadna.sadnamarket.domain.supply.SupplyService;



public class UserFacade {
    private IUserRepository iUserRepo; 
    private String systemManagerUserName;
    private StoreFacade storeFacade;
    private OrderFacade orderFacade;
    private static final Logger logger = LogManager.getLogger(UserFacade.class);
    private RealtimeService realtime;

    public UserFacade(RealtimeService realtime, IUserRepository userRepo, StoreFacade storeFacadeInstance,OrderFacade orderFacadeInsance) {
        logger.info("initilize user fascade");
        this.iUserRepo=userRepo;
        this.realtime = realtime;
        systemManagerUserName=null;
        storeFacade=storeFacadeInstance;
        orderFacade=orderFacadeInsance;
        logger.info("finish initilize user fascade");
    }

    public UserFacade(IUserRepository userRepo, StoreFacade storeFacadeInstance,OrderFacade orderFacadeInsance) {
        logger.info("initilize user fascade");
        this.iUserRepo=userRepo;
        this.realtime = null;
        systemManagerUserName=null;
        storeFacade=storeFacadeInstance;
        orderFacade=orderFacadeInsance;
        logger.info("finish initilize user fascade");
    }
    
    public synchronized int enterAsGuest(){
        logger.info("enter as guest");
       return iUserRepo.addGuest();
    }

    public synchronized void exitGuest(int guestId){
        logger.info("remove guest {}",guestId);
        iUserRepo.deleteGuest(guestId);
        logger.info("removed guest {}",guestId);
    }

    public boolean hasSystemManager(){
        if(systemManagerUserName != null){
            return true;
        }
        if(iUserRepo.hasMember(Config.SYSMAN)){
            setSystemManagerUserName(Config.SYSMAN);
            return true;
        }
        return false;
    }

    public boolean checkPremssionToStore(String userName, int storeId,Permission permission){
        logger.info("check permission to {} for user {} for store {}",permission.getValue(),userName,storeId);
        boolean isAuthrized=iUserRepo.hasPermissionToRole(userName,permission,storeId);
        logger.info("checked permission to {} for user {} for store {} and answer is: {}",permission.getValue(),userName,storeId,isAuthrized);
        return isAuthrized ;
    }

    public void notify(String userName, String msg) {
        logger.info("{} got notification {}",userName,msg);
        NotificationDTO notificationDTO = iUserRepo.addNotification(userName,msg);
        if(isLoggedIn(userName) && realtime != null){
            realtime.sendNotification(userName, notificationDTO);
        }
        if(isLoggedIn(userName) && realtime == null){
            logger.info("Did not send realtime notification as realtime service is null");
        }
    }

    public List<NotificationDTO> getNotifications(String username){
        logger.info("getting notifications for {}",username);
        List<NotificationDTO> notifes = new ArrayList<>(iUserRepo.getNotifications(username));
        logger.info("got notifications for {}",username);
        return notifes;
    }

    public boolean isLoggedIn(String username){
        logger.info("check for login for member {}",username);
        if(isExist(username)){
            logger.info("checked for login for member {}",username);
            return iUserRepo.isLoggedIn(username);
        }
        logger.info("check for login for member but member doesnt exist {}",username);
        return false;
    }

    public boolean isExist(String userName){
        logger.info("check if member exist {}",userName);
        boolean res= iUserRepo.hasMember(userName);
        logger.info("checked if member exist {} and got {}",userName,res);
        return res;
    }

    public void setSystemManagerUserName(String username){
        logger.info("set system username {}",username);
        if(!isExist(username)){
            logger.error("user doesnt exist",username);
            throw new IllegalStateException(Error.makeUserSystemManagerError());
        }
        if(systemManagerUserName!=null){
            logger.error("system manager already exist",username);
            throw new IllegalStateException(Error.makeUserSystemManagerError());
        }
        if(!username.equals(Config.SYSMAN)){
            logger.error("wrong system manager",username);
            throw new IllegalStateException(Error.makeUserSystemManagerError());
        }
        systemManagerUserName=username;
        logger.info("done set system username {}",username);
    }
    public String getSystemManagerUserName(){
        logger.info("get system username {}",systemManagerUserName);
        return systemManagerUserName;
    }

    public boolean isSystemManager(String username){
        logger.info("check if username is system manager {}",username);
        return username != null && username.equals(systemManagerUserName);
    }

    public void login(String userName,String password){
        logger.info("{} tries to login",userName);
        isValid(userName);
        if(iUserRepo.isLoggedIn(userName)){
            logger.info("user {} already logged in",userName);
            throw new IllegalStateException(Error.makeUserLoggedInError());
        }
        iUserRepo.setLogin(userName,true);
        logger.info("{} done login",userName);
    }
    public void addProductToCart(String username,int storeId, int productId,int amount){
        logger.info("{} add prooduct {} from store id {} amount: {}",username,productId,storeId,amount);
        if(amount<=0)
            throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
        if(!storeFacade.hasProductInStock(storeId, productId, amount))
            throw new IllegalArgumentException(Error.makeCartAmountDoesntExistError());
        iUserRepo.addProductToCart(username,storeId, productId, amount);
        logger.info("{} added prooduct {} from store id {} amount: {}",username,productId,storeId,amount);
    }
    public void addProductToCart(int guestId,int storeId, int productId,int amount){
        logger.info("guest: {} add prooduct {} from store id {} amount: {}",guestId,productId,storeId,amount);
        if(amount<=0)
            throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
        if(!storeFacade.hasProductInStock(storeId, productId, amount))
            throw new IllegalArgumentException(Error.makeCartAmountDoesntExistError());
        iUserRepo.addProductToCart(guestId,storeId, productId, amount);
        logger.info("guest: {} add prooduct {} from store id {} amount: {}",guestId,productId,storeId,amount);

    }
    public void removeProductFromCart(String username,int storeId, int productId){
        logger.info("{} remove prooduct {} from store id {}",username,productId,storeId);
        iUserRepo.removeProductFromCart(username,storeId, productId);
        logger.info("{} removed prooduct {} from store id {}",username,productId,storeId);
    }
    public void removeProductFromCart(int guestId,int storeId, int productId){
        logger.info("guest {} removed prooduct {} from store id {}",guestId,productId,storeId);
        iUserRepo.removeProductFromGuestCart(guestId,storeId, productId);
        logger.info("guest {} removed prooduct {} from store id {}",guestId,productId,storeId);
    }
    public void changeQuantityCart(String username,int storeId, int productId,int amount){
        logger.info("{} try to change amount of prooduct {} from store id {} amount: {}",username,productId,storeId,amount);
        if(amount<=0)
            throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
        if(!storeFacade.hasProductInStock(storeId, productId, amount))
            throw new IllegalArgumentException(Error.makeCartAmountDoesntExistError());
        iUserRepo.changeQuantityCart(username,storeId, productId, amount);
        logger.info("{} changed amount of prooduct {} from store id {} amount: {}",username,productId,storeId,amount);

    }
    public void changeQuantityCart(int guestId,int storeId, int productId,int amount){
        logger.info("guest {} try to change amount of prooduct {} from store id {} amount: {}",guestId,productId,storeId,amount);
        if(amount<=0)
            throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
        if(!storeFacade.hasProductInStock(storeId, productId, amount))
            throw new IllegalArgumentException(Error.makeCartAmountDoesntExistError());
        iUserRepo.guestChangeQuantityCart(guestId,storeId, productId, amount);
        logger.info("guest: {} try to changed amount of prooduct {} from store id {} amount: {}",guestId,productId,storeId,amount);

    }
    public void addOwnerRequest(String senderName,String userName,int store_id){
        logger.info("{} try to add owner request to {} for store {}",senderName,userName,store_id);
        RequestDTO request = iUserRepo.addOwnerRequest(senderName,this,userName, store_id);
        logger.info("{} added owner request to {} for store {}",senderName,userName,store_id);
        if(isLoggedIn(userName) && realtime != null){
            realtime.sendNotification(userName, request);
        }
        if(isLoggedIn(userName) && realtime == null){
            logger.info("Did not send realtime notification as realtime service is null");
        }
    }
    public void addManagerRequest(String senderName,String userName,int store_id){
        logger.info("{} try to add manager request to {} for store {}",senderName,userName,store_id);
        RequestDTO request = iUserRepo.addManagerRequest(senderName,this,userName, store_id);
        logger.info("{} added manager request to {} for store {}",senderName,userName,store_id);
        if(isLoggedIn(userName) && realtime != null){
            realtime.sendNotification(userName, request);
        }
        if(isLoggedIn(userName) && realtime == null){
            logger.info("Did not send realtime notification as realtime service is null");
        }
    }

    public void accept(String acceptingName,int requestID){
        logger.info("{} accept request id: {}",acceptingName,requestID);
        if(!iUserRepo.isLoggedIn(acceptingName))
            throw new IllegalStateException(Error.makeMemberUserIsNotLoggedInError());
        Request request=iUserRepo.getRequest(acceptingName,requestID);
        int storeId=request.getStoreId();
        iUserRepo.accept(acceptingName,requestID,this);
        String role=request.getRole();
        String apointer=request.getSender();
        iUserRepo.addApointer(apointer,acceptingName, storeId);
        notify(apointer, "User " + acceptingName + " accepted request for " + role + " in " + storeId);
        if(role.equals("Manager"))
            storeFacade.addStoreManager(acceptingName, storeId);
        else
            storeFacade.addStoreOwner(acceptingName,storeId);
        logger.info("{} accepted request id: {}",acceptingName,requestID);
    }

    public void reject(String rejectingName,int requestID){
        logger.info("{} reject request id: {}",rejectingName,requestID);
        Request request=iUserRepo.getRequest(rejectingName,requestID);
        int storeId=request.getStoreId();
        String role=request.getRole();
        String apointer=request.getSender();
        notify(apointer, "User " + rejectingName + " rejected request for " + role + " in " + storeId);
        iUserRepo.reject(rejectingName,requestID);
        logger.info("{} rejected request id: {}",rejectingName,requestID);

    }

    public void ok(String okayingName,int notifId){
        logger.info("{} ok notification id: {}",okayingName,notifId);
        iUserRepo.reject(okayingName,notifId);
        logger.info("{} okayed notification id: {}",okayingName,notifId);
    }

    public void login(String userName,String password, int guestId){//the cart of the guest
        logger.info("{} login from guest {}",userName,guestId);
        isValid(userName);
        if(iUserRepo.isLoggedIn(userName)){
            logger.info("user {} already logged in",userName);
            throw new IllegalStateException(Error.makeUserLoggedInError());
        }
        if(iUserRepo.getUserCart(userName).isEmpty()){
            iUserRepo.setCart(userName,iUserRepo.getGuestCart(guestId));
        }
        iUserRepo.setLogin(userName,true);
        exitGuest(guestId);
        logger.info("{} done login from guest {}",userName,guestId);
    }
    
    public int logout(String userName){
        logger.info("{} logout ",userName);
        if(!iUserRepo.hasMember(userName))
            throw new NoSuchElementException(Error.makeMemberUserDoesntExistError(userName));

        iUserRepo.logout(userName);
        logger.info("{} done logout",userName);
        return enterAsGuest();
    }
    public void setCart(List<CartItemDTO> cart,String userName){
        logger.info("{} set cart ",userName);
        iUserRepo.setCart(userName,cart);
        logger.info("{} done set cart ",userName);
    }
  

    public void register(String username,String firstName, String lastName,String emailAddress,String phoneNumber, LocalDate birthDate){
        logger.info("{} try to register ",username);
        iUserRepo.store(username,firstName,lastName,emailAddress,phoneNumber,birthDate);
        logger.info("{} done register ",username);
    }

   
    public void addStoreManager(String username,int storeId){
        logger.info("add Store Manager to {} in {} ",username,storeId);
        iUserRepo.addRole(username,iUserRepo.createStoreManagerRole(storeId));
        logger.info("done add Store Manager to {} in {} ",username,storeId);

    }
    public void addStoreOwner(String username,String asignee, int storeId){
        logger.info("add Store owner to {} in {} ",username,storeId);
        iUserRepo.addRole(username,iUserRepo.createStoreOwnerRole(storeId,asignee));
        logger.info("done add Store owner to {} in {} ",username,storeId);

    }
    public void addStoreFounder(String username,int storeId){
        logger.info("add Store founder to {} in {} ",username,storeId);
        iUserRepo.addRole(username,iUserRepo.createStoreFounderRole(storeId,username));
        logger.info("done add Store founder to {} in {} ",username,storeId);
    }
    public void addPremssionToStore(String giverUserName,String userName, int storeId,Permission permission){
        logger.info("{} get permission to store {} to {}",userName,storeId,permission);
        if(!iUserRepo.isApointee(giverUserName, userName,storeId))
            throw new IllegalStateException(Error.makeUserCanOnlyEditPermissionsToApointeesError());
        iUserRepo.addPermissionToRole(userName,permission, storeId);
        logger.info("{} got permission to store {} to {}",userName,storeId,permission);

    }
    public void removePremssionFromStore(String removerUsername,String userName, int storeId,Permission permission){
        logger.info("{} remove permission to store {} to {}",userName,storeId,permission);
        if(!iUserRepo.isApointee(removerUsername, userName, storeId))
            throw new IllegalStateException(Error.makeUserCanOnlyEditPermissionsToApointeesError());
       
        iUserRepo.removePermissionFromRole(userName,permission, storeId);
        logger.info("{} remove permission to store {} to {}",userName,storeId,permission);
    }

    public List<Permission> getManagerPermissions(String actorUsername,String userName, int storeId){
        logger.info("{} got permissions of {} in store {}",actorUsername, userName, storeId);
        return iUserRepo.getPermissions(userName,storeId);
    }

    public void leaveRole(String username,int storeId){
        logger.info("{} try leave role in store {}",username,storeId);
        iUserRepo.leaveRole(username,storeId,this);
        logger.info("{} try left role in store {}",username,storeId);
    }
    public void removeRoleFromMember(String username,String remover,int storeId){
        logger.info("{} try remove role in store {}",username,storeId);
        iUserRepo.removeRoleFromMember(username, remover, storeId, this);
        logger.info("{} removed role in store {}",username,storeId);
    }
    public void setFirstName(String userName, String firstName) {
        logger.info("set first name for {}", firstName);
        isValid(firstName);
        iUserRepo.setFirstName(userName, firstName);
        logger.info("done set first name for {}", firstName);
    }

    public void setLastName(String userName, String lastName) {
        logger.info("set last name for {}", userName);
        isValid(lastName);
        iUserRepo.setLastName(userName,lastName);
        logger.info("done set last name for {}", userName);

    }

    public void setEmailAddress(String userName, String emailAddress) {
        logger.info("set email for {}", userName);
        isValid(emailAddress);
       iUserRepo.setEmailAddress(userName,emailAddress);
        logger.info("done set email for {}", userName);
    }

    public void setPhoneNumber(String userName, String phoneNumber) {
        logger.info("set phone number for {}", userName);
        isValid(phoneNumber);
        iUserRepo.setPhoneNumber(userName,phoneNumber);
        logger.info("done set phone number for {}", userName);
    }
    public void setBirthDate(String userName, LocalDate birthDate) {
        logger.info("set birth date for {}={}", userName,birthDate);
        iUserRepo.setBirthday(userName,birthDate);
        logger.info("done set birth date for {}", userName);
    }
    private void isValid(String detail){
        logger.info("check if field is valid");
        if(detail==null||detail.trim().equals("")){
            throw new IllegalArgumentException(Error.makeValidStringError());
        }
        logger.info("field {} is valid",detail);

    }
    public MemberDTO getMemberDTO(String userName){
        logger.info("get memberDTO for {}",userName);
        isValid(userName);
        MemberDTO memberDTO= iUserRepo.getMemberDTO(userName);
        logger.info("finished get memberDTO for {}",userName);
        return memberDTO;
    }
    public List<Integer> getMemberPermissions(String userName, int storeId){
        logger.info("get permissions for {} in {}",userName,storeId);
        isValid(userName);
        UserRole role=iUserRepo.getRoleOfStore(userName,storeId);
        List<Integer> permissionsInRole=new ArrayList<>();
        for(Permission permission: Permission.values()){
            if (role.hasPermission(permission))
                permissionsInRole.add(permission.getValue());

        }
        logger.info("got permissions for {} in {}",userName,storeId);
        return permissionsInRole;

    }

    public List<Permission> getMemberPermissionsEnum(String userName, int storeId){
        List<Integer> permissionNums = getMemberPermissions(userName, storeId);
        List<Permission> permissions = new ArrayList<>();
        for(int permission : permissionNums) {
            permissions.add(Permission.getEnumByInt(permission));
        }
        return permissions;
    }

    public List<UserRoleDTO> getMemberRoles(String userName){
        logger.info("get user roles for {}",userName);
        List<UserRoleDTO> userRoles=iUserRepo.getUserRolesString(userName);
        for (UserRoleDTO userRoleDTO : userRoles) {
            String storeName=storeFacade.getStoreInfo(userName,userRoleDTO.getStoreId()).getStoreName();
            userRoleDTO.setStoreName(storeName);
        }
        logger.info("got user roles for {}: {}",userName, userRoles);
        return userRoles;
    }

    public List<String> getUserOrders(String username){
        List<Integer> ordersIds=iUserRepo.getOrdersHistory(username);
        List <String> ordersString=new ArrayList<>();
        for (Integer orderId : ordersIds) {
            Map<Integer,OrderDTO> orders=orderFacade.getOrderByOrderId(orderId);
            for (Integer store_id : orders.keySet()) {
                Map<Integer,String> ordersProducts=orders.get(store_id).getOrderProductsJsons();
                for (Integer product_id : ordersProducts.keySet()) {
                    ordersString.add(ordersProducts.get(product_id));
                }
            }
        }
        return ordersString;
    }

     public List<String> getUserOrdersV2(String username){
        List<Integer> ordersIds=iUserRepo.getOrdersHistory(username);
        List <String> ordersString=new ArrayList<>();
        for (Integer orderId : ordersIds) {
            Map<Integer,OrderDTO> orders=orderFacade.getOrderByOrderId(orderId);
            for (Integer store_id : orders.keySet()) {
                Map<Integer,String> ordersProducts=orders.get(store_id).getOrderProductsJsons();
                for (Integer product_id : ordersProducts.keySet()) {
                    ordersString.add(ordersProducts.get(product_id));
                }
            }
        }
        return ordersString;
    }

    public List<OrderDTO> getUserOrderDTOs(String username){
        logger.info("get orders for {}",username);
        List<Integer> ordersIds=iUserRepo.getOrdersHistory(username);
        List <OrderDTO> orders =new ArrayList<>();
        for (Integer orderId : ordersIds) {
            Map<Integer,OrderDTO> ordersMap =orderFacade.getOrderByOrderId(orderId);
            for (Integer store_id : ordersMap.keySet()) {
                orders.add(ordersMap.get(store_id));
            }
        }
        logger.info("finished get orders for {}",username);
        return orders;
    }


    private double calculateFinalPrice(String username,List<ProductDataPrice> storePriceData){
        logger.info("calculate final price for user {} with items {}",username,storePriceData);
        double sum=0;
        for(ProductDataPrice productDataPrice: storePriceData){
            sum=sum+productDataPrice.getAmount()* productDataPrice.getNewPrice();
        }
        logger.info("finished calculate final price for user {} with items {} and got {}",username,storePriceData, sum);
        return sum;
    }
    private double calculateOldPrice(String username,List<ProductDataPrice> storePriceData){
        logger.info("calculate old price for user {} with items {}",username,storePriceData);
        double sum=0;
        for(ProductDataPrice productDataPrice: storePriceData){
                sum=sum+productDataPrice.getAmount()*productDataPrice.getOldPrice();
        }
        logger.info("finished calculate old price for user {} with items {} and got {}",username,storePriceData, sum);
        return sum;
    }

    public UserOrderDTO viewCart(String username) throws Exception {
        logger.info("view cart for user {}",username);
        List<CartItemDTO> items=iUserRepo.getUserCart(username);
        List<ProductDataPrice> storePriceData=storeFacade.calculatePrice(username, items);
        logger.info("finished view cart for user {}",username);
        return new UserOrderDTO(storePriceData,calculateOldPrice(username, storePriceData),calculateFinalPrice(username, storePriceData)); 
    }

    public UserOrderDTO viewCart(int guestId) throws Exception {
        logger.info("view cart for guest {}",guestId);
        List<CartItemDTO> items=iUserRepo.getGuestCart(guestId);
        List<ProductDataPrice> storeApriceData=storeFacade.calculatePrice(null, items);
        logger.info("finished view cart for guest {}",guestId);
        return new UserOrderDTO(storeApriceData,calculateOldPrice(null, storeApriceData),calculateFinalPrice(null, storeApriceData));    
    }

    public void checkCart(String username){
        logger.info("check cart for user {}",username);
        List<CartItemDTO> items=iUserRepo.getUserCart(username);
        storeFacade.checkCart(username, items);
        logger.info("finished check cart for user {} without errors",username);
    }
    public void checkCart(int guestId){
        logger.info("check cart for guest {}",guestId);
        List<CartItemDTO> items=iUserRepo.getGuestCart(guestId);
        storeFacade.checkCart(null, items);
        logger.info("finished check cart for guest {} without errors",guestId);
    }

    public void purchaseCart(String username,CreditCardDTO creditCard,AddressDTO addressDTO) throws Exception {
        logger.info("purchase cart for user {} with credit card {} and address {}",username,creditCard,addressDTO);
        List<CartItemDTO> items=iUserRepo.getUserCart(username);
        validateCreditCard(creditCard);
        validateAddress(addressDTO);
        storeFacade.checkCart(username, items);
        List<ProductDataPrice> productList=storeFacade.calculatePrice(username, items);
        Map<Integer,Integer> productAmount=new HashMap<>();
        String supplyString = makeSuplyment(productAmount,addressDTO);
        createUserOrders(productList,creditCard,supplyString,username);
        storeFacade.updateStock(username, items);
        clearCart(username);
        logger.info("finish purchase cart for user {} with credit card {} and address {}",username,creditCard,addressDTO);
    }

    public void clearCart(String username){
        logger.info("clear cart for user {}",username);
        iUserRepo.clearCart(username);
        logger.info("done clear cart for user {}",username);
    }

    public void purchaseCart(int guestId,CreditCardDTO creditCard,AddressDTO addressDTO) throws Exception {
        logger.info("purchase cart for guest {} with credit card {} and address {}",guestId,creditCard,addressDTO);
        List<CartItemDTO> items=iUserRepo.getGuestCart(guestId);
        validateCreditCard(creditCard);
        validateAddress(addressDTO);
        storeFacade.checkCart(null, items);
        List<ProductDataPrice> productList=storeFacade.calculatePrice(null, items);
        Map<Integer,Integer> productAmount=new HashMap<>();
        String supplyString = makeSuplyment(productAmount,addressDTO);
        createUserOrders(productList,creditCard,supplyString,null);
        storeFacade.updateStock(null, items);
        iUserRepo.clearGuestCart(guestId);;
        logger.info("finish purchase cart for guest {} with credit card {} and address {}",guestId,creditCard,addressDTO);
    }
    private void validateAddress(AddressDTO address){
        logger.info("validate address {}",address);
        if(address.getCountry().isEmpty() || address.getCity().isEmpty() || address.getAddressLine1().isEmpty()){
            throw new IllegalArgumentException(Error.makePurchaseMissingAddressError());
        }
        logger.info("address is valid {}",address);
    }
    private void validateCreditCard(CreditCardDTO creditCard){
        logger.info("validate credit card {}",creditCard);
        if(creditCard.getDigitsOnTheBack().isEmpty() || creditCard.getCreditCardNumber().isEmpty() || creditCard.getOwnerId().isEmpty()){
            throw new IllegalArgumentException(Error.makePurchaseMissingCardError());
        }
        PaymentService payment = PaymentService.getInstance();
        if(!payment.checkCardValid(creditCard)){
            throw new IllegalArgumentException(Error.makePurchaseInvalidCardError());
        }
        logger.info("credit card is valid {}",creditCard);
    }
    private void createUserOrders(List<ProductDataPrice> storeBag, CreditCardDTO creditCard, String supplyString,String username) throws JsonProcessingException {
        logger.info("create user orders");
        SupplyService supply=SupplyService.getInstance();
        PaymentService payment = PaymentService.getInstance();
        Map<Integer,Double> payAmount = getStorePrice(storeBag);
        for(int storeId : payAmount.keySet()){       
            if(!payment.pay(payAmount.get(storeId), creditCard, storeFacade.getStoreBankAccount(storeId))){
                supply.cancelOrder(supplyString);
                throw new IllegalStateException(Error.makePurchasePaymentCannotBeCompletedForStoreError(storeId));
            }
            Map<Integer,List<ProductDataPrice>> productAmounts=new HashMap<>();
            List<ProductDataPrice> productList=getStoreOrder(storeBag, storeId);
            productAmounts.put(storeId, productList);      
            int orderId=orderFacade.createOrder(productAmounts,username);
            if(username!=null)
                iUserRepo.addOrder(username,orderId);
        }
        logger.info("done create user orders");
    }
    public List<OrderDTO> getAllOrders(String username){
        logger.info("get all orders for {}",username);
        if(!isExist(username))
            throw new NoSuchElementException(Error.makeUserDoesntExistError());
        if(!iUserRepo.isLoggedIn(username))
            throw new IllegalStateException(Error.makeUserLoggedInError());
        if(!username.equals(systemManagerUserName))
            throw new IllegalStateException(Error.makeSystemManagerCanOnlyViewOrdersError());
        logger.info("got all orders for {}",username);
        return orderFacade.getAllOrders();
    }
    private String makeSuplyment(Map<Integer,Integer> productAmount,AddressDTO addressDTO) throws JsonProcessingException {
        logger.info("make supplyment {} with address {}",productAmount,addressDTO);
        SupplyService supply=SupplyService.getInstance();
        OrderDetailsDTO orderDetailsDTO=new OrderDetailsDTO(productAmount);
        if(!supply.canMakeOrder(orderDetailsDTO, addressDTO)){
            throw new IllegalStateException(Error.makePurchaseOrderCannotBeSuppliedError());
        }
        String supplyString = supply.makeOrder(orderDetailsDTO, addressDTO);
        logger.info("done make supplyment {}",supplyString);
        return supplyString;
    }
    private Map<Integer,Double> getStorePrice(List<ProductDataPrice> storeBag){
        logger.info("get store price");
        Map<Integer,Double> productAmounts=new HashMap<>();
        for(ProductDataPrice productDataPrice: storeBag){
            if(productAmounts.containsKey(productDataPrice.getStoreId())){
                productAmounts.put(productDataPrice.getStoreId(), productAmounts.get(productDataPrice.getStoreId())+productDataPrice.getAmount()*productDataPrice.getNewPrice());
            }else{
                productAmounts.put(productDataPrice.getStoreId(), productDataPrice.getAmount()*productDataPrice.getNewPrice());
            }
        }
        logger.info("finish get store price");
        return productAmounts;
    }
    private List<ProductDataPrice> getStoreOrder(List<ProductDataPrice> allOrder,int store_id){
        List<ProductDataPrice> storeOrder=new ArrayList<>();
        for(ProductDataPrice productDataPrice: allOrder){
            if(productDataPrice.getStoreId()==store_id){
                storeOrder.add(productDataPrice);
            }
        }
        return storeOrder;

    }
    //only for tests
    public List<CartItemDTO> getCartItems(int guest_id){
        logger.info("get guest cart");
        return iUserRepo.getGuestCart(guest_id);
    }
    public boolean checkIfSystemManager(String username){
        logger.info("check if user is system manager {}",username);
        boolean res= systemManagerUserName.equals(username);
        logger.info("checked if user is system manager {} and got {}",username,res);
        return res;
    }
    public RequestDTO addRequest(String senderName, String sentName,int storeId, String reqType){
        logger.info("add request from {} to {} for store {} with type {}",senderName,sentName,storeId,reqType);
        RequestDTO requestDTO=iUserRepo.addRequest(senderName, sentName, storeId, reqType);
        logger.info("added request from {} to {} for store {} with type {}",senderName,sentName,storeId,reqType);
        return requestDTO;
    }
    public List<CartItemDTO> getMemberCart(String username){
        logger.info("get member cart for {}",username);
        List<CartItemDTO> cart=iUserRepo.getUserCart(username);
        logger.info("got member cart for {}",username);
        return cart;
    }
    public boolean isApointee(String apointee,String apointer,int store_id){
        return iUserRepo.isApointee(apointer, apointee,store_id);
    }

    public void clear(){
        iUserRepo.clear();
    }

    public void setRealtime(RealtimeService realtime) {
        logger.info("set realtime service");
        this.realtime = realtime;
        logger.info("done set realtime service");
    }
    public boolean isGuestExist(int guestID){
        logger.info("check if guest exist {}",guestID);
        boolean res= iUserRepo.isGuestExist(guestID);
        logger.info("checked if guest exist {} and got {}",guestID,res);
        return res;
    }
    public void logoutMembers(){
        logger.info("logout all members");
        iUserRepo.logoutMembers();
        logger.info("done logout all members");
    }
    public void removeGuests(){
        logger.info("remove all guests");
        iUserRepo.removeGuests();
        logger.info("done remove all guests");
    }
}
