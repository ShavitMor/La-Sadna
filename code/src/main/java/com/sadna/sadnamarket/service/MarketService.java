package com.sadna.sadnamarket.service;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.fasterxml.jackson.databind.ser.impl.SimpleBeanPropertyFilter;
import com.fasterxml.jackson.databind.ser.impl.SimpleFilterProvider;
import com.sadna.sadnamarket.Config;
import com.sadna.sadnamarket.SetupRunner;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryHibernateImpl;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
import com.sadna.sadnamarket.domain.auth.IAuthRepository;
import com.sadna.sadnamarket.domain.buyPolicies.*;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.HibernateConditionRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.IConditionRespository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.MemoryConditionRepository;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.HibernateDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.IDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.MemoryDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.orders.*;
import com.sadna.sadnamarket.domain.products.*;
import com.sadna.sadnamarket.domain.stores.*;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.users.*;
import org.hibernate.HibernateException;
import org.slf4j.Logger;
import org.slf4j.LoggerFactory;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Profile;
import org.springframework.stereotype.Component;

import javax.transaction.Transactional;
import java.time.LocalDate;
import java.time.LocalTime;
import java.util.*;

@Component
@Profile("default")
public class MarketService {
    private static MarketService instance;
    private UserFacade userFacade;
    private ProductFacade productFacade;
    private OrderFacade orderFacade;
    private StoreFacade storeFacade;
    private BuyPolicyFacade buyPolicyFacade;
    private DiscountPolicyFacade discountPolicyFacade;
    private AuthFacade authFacade;
    private static ObjectMapper objectMapper = new ObjectMapper();
    Logger logger = LoggerFactory.getLogger(MarketService.class);


    RealtimeService realtimeService;

    @Autowired
    public MarketService(RealtimeService realtimeService) {
        this(realtimeService,
                new HibernateStoreRepository(),
                new HibernateOrderRepository(),
                new HibernateProductRepository(),
                new HibernateBuyPolicyRepository(),
                new HibernateDiscountPolicyRepository(),
                new HibernateConditionRepository(),
                new AuthRepositoryHibernateImpl(),
                new UserHibernateRepo());
    }

    public MarketService(RealtimeService realtimeService,
                         IStoreRepository storeRepo,
                         IOrderRepository orderRepo,
                         IProductRepository productRepo,
                         IBuyPolicyRepository policyRepo,
                         IDiscountPolicyRepository discountRepo,
                         IConditionRespository conditionRepo,
                         IAuthRepository authRepo,
                         IUserRepository userRepo) {
        this.realtimeService = realtimeService;
        this.productFacade = new ProductFacade(productRepo);
        this.orderFacade = new OrderFacade(orderRepo);
        this.storeFacade = new StoreFacade(storeRepo);
        this.buyPolicyFacade = new BuyPolicyFacade(policyRepo);
        this.discountPolicyFacade = new DiscountPolicyFacade(conditionRepo, discountRepo);
        this.userFacade = new UserFacade(realtimeService, userRepo,storeFacade, orderFacade);
        this.authFacade = new AuthFacade(authRepo, userFacade);
        this.orderFacade.setStoreFacade(storeFacade);
        this.storeFacade.setUserFacade(userFacade);
        this.storeFacade.setProductFacade(productFacade);
        this.storeFacade.setOrderFacade(orderFacade);
        this.storeFacade.setBuyPolicyFacade(buyPolicyFacade);
        this.storeFacade.setDiscountPolicyFacade(discountPolicyFacade);
        this.buyPolicyFacade.setProductFacade(productFacade);
        this.buyPolicyFacade.setStoreFacade(storeFacade);
        this.buyPolicyFacade.setUserFacade(userFacade);
        this.discountPolicyFacade.setProductFacade(productFacade);
        this.discountPolicyFacade.setStoreFacade(storeFacade);

        if(Config.CLEAR){
            clear();
        }
        if(Config.HAS_STATE){
            SetupRunner runner = new SetupRunner(this);
            runner.setupFromJson(Config.BEGIN);
        }
        userFacade.logoutMembers();
        userFacade.removeGuests();

        if(!Config.TESTING_MODE && !userFacade.hasSystemManager()){
            throw new UnsupportedOperationException("System cannot start without a System Manager");
        }
    }

    public static MarketService getNewMemoryInstance() {
        instance =new MarketService(null,
                new MemoryStoreRepository(),
                new MemoryOrderRepository(),
                new MemoryProductRepository(),
                new MemoryBuyPolicyRepository(),
                new MemoryDiscountPolicyRepository(),
                new MemoryConditionRepository(),
                new AuthRepositoryMemoryImpl(),
                new MemoryRepo());
        return instance;
    }

    public void injectRealtime(RealtimeService realtimeService){
        userFacade.setRealtime(realtimeService);
    }

    // ----------------------- Store -----------------------

    @Transactional
    public Response loginUsingToken(String token, String username) {
        try{
            if(!authFacade.login(token).equals(username)) {
                userFacade.logout(username);
                logger.error(String.format("failed to verify token for user %s", username));
                return Response.createResponse(true, "Failed to verify token");
            }
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            logger.error("checkToken: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }
    public void checkToken(String token, String username) {
        if(!authFacade.login(token).equals(username)) {
            logger.error(String.format("failed to verify token for user %s", username));
            throw new IllegalArgumentException(Error.makeTokenInvalidError(username));
        }
    }
    public void checkTokenSystemManager(String token, String username) {
        if(!authFacade.login(token).equals(userFacade.getSystemManagerUserName())&&!authFacade.login(token).equals(username)){
            logger.error(String.format("failed to verify token for user %s", username));
            throw new IllegalArgumentException(Error.makeTokenInvalidError(username));
        }
    }

    private Response handleException(Exception e) {
        String functionName = Thread.currentThread().getStackTrace()[2].getMethodName();
        String log = String.format("Error in function %s: %s", functionName, e.getMessage());
        if(e.getMessage().equals(Error.makeDBError())) {
            logger.error(log);
        }
        else {
            logger.info(log);
        }
        return Response.createResponse(true, e.getMessage());
    }

    @Transactional
    public Response createStore(String token, String founderUsername, String storeName, String address, String email, String phoneNumber) {
        try {
            checkToken(token, founderUsername);
            int newStoreId = storeFacade.createStore(founderUsername, storeName, address, email, phoneNumber); // will throw an exception if the store already exists
            //addBuyPolicy(token, founderUsername, newStoreId,"");
            logger.info(String.format("User %s created a store with id %d.", founderUsername, newStoreId));
            return Response.createResponse(false, objectMapper.writeValueAsString(newStoreId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From "My Stores" page, redirects to new store


    @Transactional
    public Response addProductToStore(String token, String username, int storeId, String productName, int productQuantity, double productPrice, String category, double rank, double productWeight, String description) {
        try {
            checkToken(token, username);
            int newProductId = storeFacade.addProductToStore(username, storeId, productName, productQuantity, productPrice, category, rank, productWeight, description);
            logger.info(String.format("User %s added product %d to store %d.", username, newProductId, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(newProductId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, only for permission, new page

    @Transactional
    public Response setStoreBankAccount(String token, String username, int storeId, BankAccountDTO bankAccount) {
        try {
            checkToken(token, username);
            storeFacade.setStoreBankAccount(username, storeId, bankAccount);
            logger.info(String.format("User %s changed store %d bank account.", username, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, only for owner, new page

    @Transactional
    public Response deleteProductFromStore(String token, String username, int storeId, int productId) {
        try {
            checkToken(token, username);
            int deletedProductId = storeFacade.deleteProduct(username, storeId, productId);
            logger.info(String.format("User %s deleted product %d from store %d.", username, productId, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(deletedProductId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, X on products from products list, only for permission

    @Transactional
    public Response updateProductInStore(String token, String username, int storeId, int productId, String newProductName, int newQuantity, double newPrice, String newCategory, double newRank, String newDesc) {
        try {
            checkToken(token, username);
            int updateProductId = storeFacade.updateProduct(username, storeId, productId, newProductName, newQuantity, newPrice, newCategory, newRank, newDesc);
            logger.info(String.format("User %s updated product %d in store %d.", username, productId, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(updateProductId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Product page, button, only for permission, new page

    @Transactional
    public Response updateProductAmountInStore(String token, String username, int storeId, int productId, int newQuantity) {
        try {
            checkToken(token, username);
            int updateProductId = storeFacade.updateProductAmount(username, storeId, productId, newQuantity);
            logger.info(String.format("User %s updated product amount %d in store %d.", username, productId, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(updateProductId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response closeStore(String token, String username, int storeId) {
        try {
            checkToken(token, username);
            boolean storeClosed = storeFacade.closeStore(username, storeId);
            logger.info(String.format("User %s closed store %d.", username, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(storeClosed));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, only for owner, popup

    @Transactional
    public Response reopenStore(String token, String username, int storeId) {
        try {
            checkToken(token, username);
            boolean storeClosed = storeFacade.reopenStore(username, storeId);
            logger.info(String.format("User %s reopened store %d.", username, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(storeClosed));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }


    public Response getOwners(String token, String username, int storeId) {
        try {
            checkToken(token, username);
            List<MemberDTO> owners = storeFacade.getOwners(username, storeId);
            logger.info(String.format("User %s got owners of store %d.", username, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(owners));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, only for owner, new page with list of owners, for each owner button that shows details (in squares like my stores)

    public Response getManagers(String token, String username, int storeId) {
        try {
            checkToken(token, username);
            List<MemberDTO> managers = storeFacade.getManagers(username, storeId);
            logger.info(String.format("User %s got managers of store %d.", username, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(managers));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, only for owner, new page with list of managers, for each manager button that shows details and permissions (in squares like my stores)

    /*public Response getSellers(String token, String username, int storeId) {
        try {
            checkToken(token, username);
            List<MemberDTO> sellers = storeFacade.getSellers(username, storeId);
            logger.info(String.format("User %s got sellers of store %d.", username, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(sellers));
        }
        catch (Exception e) {
            logger.error("getSellers: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }*/

    @Transactional
    public Response sendStoreOwnerRequest(String token, String currentOwnerUsername, String newOwnerUsername, int storeId) {
        try {
            checkToken(token, currentOwnerUsername);
            storeFacade.sendStoreOwnerRequest(currentOwnerUsername, newOwnerUsername, storeId);
            logger.info(String.format("User %s nominated User %s as owner of store %d.", currentOwnerUsername, newOwnerUsername, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, new page, enter username and click send

    @Transactional
    public Response sendStoreManagerRequest(String token, String currentOwnerUsername, String newManagerUsername, int storeId) {
        try {
            checkToken(token, currentOwnerUsername);
            storeFacade.sendStoreManagerRequest(currentOwnerUsername, newManagerUsername, storeId);
            logger.info(String.format("User %s nominated User %s as manager of store %d.", currentOwnerUsername, newManagerUsername, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, new page, enter username and click send

    @Transactional
    public Response acceptRequest(String token, String newUsername, int storeId) {
        try {
            checkToken(token, newUsername);
            userFacade.accept(newUsername, storeId);
            logger.info(String.format("User %s accepted nomination in store %d.", newUsername, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(newUsername));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From notifications, choose "Request" notification and click "accept"

    public Response getStoreOrderHistory(String token, String username, int storeId) {
        try {
            checkToken(token, username);
            List<OrderDTO> history = storeFacade.getStoreOrderHistoryDTO(username, storeId);
            logger.info(String.format("User %s got order history from store %d.", username, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(history));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From Store page, Actions menu, only for persmission, new page

    public Response getStoreInfo(String token, String username, int storeId) {
        try {
            if(username != null)
                checkToken(token, username);
            Set<String> fields = Set.of("storeId", "storeName");
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("filter", SimpleBeanPropertyFilter.filterOutAllExcept(fields));

            StoreDTO storeDTO = storeFacade.getStoreInfo(username, storeId);
            String json = objectMapper.writer(filterProvider).writeValueAsString(storeDTO);
            logger.info(String.format("A user got store info of store %d.", storeId));
            return Response.createResponse(false, json);
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From "my stores" page OR from search store (from main search)

    public Response getStoreByName(String token, String username, String storeName) {
        try {
            if(username != null)
                checkToken(token, username);
            Set<String> fields = Set.of("storeId", "storeName");
            SimpleFilterProvider filterProvider = new SimpleFilterProvider();
            filterProvider.addFilter("filter", SimpleBeanPropertyFilter.filterOutAllExcept(fields));

            StoreDTO storeDTO = storeFacade.getStoreByName(storeName, username);
            String json = objectMapper.writer(filterProvider).writeValueAsString(storeDTO);
            logger.info(String.format("A user got store info of store %s.", storeName));
            return Response.createResponse(false, json);
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

  public Response getProductInfo(String token, String username, int productId) {
        try {
            if(username != null)
                checkToken(token, username);

            ProductDTO productDTO = storeFacade.getProductInfo(username, productId);
            String json = objectMapper.writeValueAsString(productDTO);
            logger.info(String.format("A user got product info of product %d.", productId));
            return Response.createResponse(false, json);
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //From boxes in store or from main search

    public Response getStoreProductsInfo(String token, String username, int storeId, String productName, String category, double price, double minProductRank) {
        try {
            if(username != null)
                checkToken(token, username);

            Map<ProductDTO, Integer> productDTOsAmounts = storeFacade.getProductsInfoAndFilter(username, storeId, productName, category, price, minProductRank);
            Map<String, Integer> jsonMap = new HashMap<>();
            for(ProductDTO productDTO : productDTOsAmounts.keySet()){
                jsonMap.put(objectMapper.writeValueAsString(productDTO),productDTOsAmounts.get(productDTO));
            }
            logger.info(String.format("A user got products info of store %d.", storeId));
            if(productDTOsAmounts.isEmpty()){
                logger.error("getProductsInfo: No products found");
                return Response.createResponse(true, "No products found");
            }
            return Response.createResponse(false, objectMapper.writeValueAsString(jsonMap));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //In store info window

    public Response getStoreProductAmount(String token, String username, int storeId, int productId) {
        try {
            if(username != null)
                checkToken(token, username);

            int amount = storeFacade.getProductAmount(storeId, productId);
            logger.info(String.format("A user got product %d amount in store %d.", productId, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(amount));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    /*public Response addSellerToStore(String token, int storeId, String adderUsername, String sellerUsername) {
    /*public Response addSellerToStore(String token, int storeId, String adderUsername, String sellerUsername) {
        try {
            checkToken(token, adderUsername);
            storeFacade.addSeller(storeId, adderUsername, sellerUsername);
            logger.info(String.format("User %s added User %s as a seller to store %d.", adderUsername, sellerUsername, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(sellerUsername));
        }
        catch (Exception e) {
            logger.error("addSellerToStore: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }*/

    @Transactional
    public Response createProductKgBuyPolicy(String token, String username, int productId, List<BuyType> buytypes, double min, double max) {
        try {
            checkToken(token, username);
            int policyId = buyPolicyFacade.createProductKgBuyPolicy(productId, buytypes, min, max, username);
            logger.info(String.format("User %s added product kg limit buy policy: product %d, weight range %f - %f.", username, productId, min, max));
            return Response.createResponse(false, objectMapper.writeValueAsString(policyId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createProductAmountBuyPolicy(String token, String username, int productId, List<BuyType> buytypes, int min, int max) {
        try {
            checkToken(token, username);
            int policyId = buyPolicyFacade.createProductAmountBuyPolicy(productId, buytypes, min, max, username);
            logger.info(String.format("User %s added product amount limit buy policy: product %d, amount %d - %d.", username, productId, min, max));
            return Response.createResponse(false, objectMapper.writeValueAsString(policyId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //from Store window, Actions menu, new page

    @Transactional
    public Response createCategoryAgeLimitBuyPolicy(String token, String username, String category, List<BuyType> buytypes, int min, int max) {
        try {
            checkToken(token, username);
            int policyId = buyPolicyFacade.createCategoryAgeLimitBuyPolicy(category, buytypes, min, max, username);
            logger.info(String.format("User %s added category age limit buy policy : category %s, age %d - %d.", username, category, min, max));
            return Response.createResponse(false, objectMapper.writeValueAsString(policyId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createCategoryHourLimitBuyPolicy(String token, String username, String category, List<BuyType> buytypes, LocalTime from, LocalTime to) {
        try {
            checkToken(token, username);
            int policyId = buyPolicyFacade.createCategoryHourLimitBuyPolicy(category, buytypes, from, to, username);
            logger.info(String.format("User %s added category hour limit buy policy : category %s, hour %s - %s.", username, category, from.toString(), to.toString()));
            return Response.createResponse(false, objectMapper.writeValueAsString(policyId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createCategoryRoshChodeshBuyPolicy(String token, String username, String category, List<BuyType> buytypes) {
        try {
            checkToken(token, username);
            int policyId = buyPolicyFacade.createCategoryRoshChodeshBuyPolicy(category, buytypes, username);
            logger.info(String.format("User %s added category rosh chodesh limit buy policy: category %s.", username, category));
            return Response.createResponse(false, objectMapper.writeValueAsString(policyId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createCategoryHolidayBuyPolicy(String token, String username,  String category, List<BuyType> buytypes) {
        try {
            checkToken(token, username);
            int policyId = buyPolicyFacade.createCategoryHolidayBuyPolicy(category, buytypes, username);
            logger.info(String.format("User %s added category holiday limit buy policy: category %s.", username, category));
            return Response.createResponse(false, objectMapper.writeValueAsString(policyId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createCategorySpecificDatePolicy(String token, String username, String category, List<BuyType> buytypes, int day, int month, int year) {
        try {
            checkToken(token, username);
            int policyId = buyPolicyFacade.createSpecificDateBuyPolicy(category, buytypes, day, month, year, username);
            logger.info(String.format("User %s added category specific date buy policy: category %s, day %d, month %d, year %d.", username, category, day, month, year));
            return Response.createResponse(false, objectMapper.writeValueAsString(policyId));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createAndBuyPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = buyPolicyFacade.createAndBuyPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added composition buy policy: AND(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createOrBuyPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = buyPolicyFacade.createOrBuyPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added composition buy policy: OR(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createConditioningBuyPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = buyPolicyFacade.createConditioningBuyPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added composition buy policy: CONDITIONING(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response addBuyPolicyToStore(String token, String username, int storeId, int policyId) {
        try {
            checkToken(token, username);
            buyPolicyFacade.addBuyPolicyToStore(username, storeId, policyId);
            logger.info(String.format("User %s added policy %d to store %d.", username, storeId, policyId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response addLawBuyPolicyToStore(String token, String username, int storeId, int policyId) {
        try {
            checkToken(token, username);
            buyPolicyFacade.addLawBuyPolicyToStore(username, storeId, policyId);
            logger.info(String.format("User %s added law buy policy %d to store %d.", username, storeId, policyId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response removeBuyPolicyFromStore(String token, String username, int storeId, int policyId) {
        try {
            checkToken(token, username);
            buyPolicyFacade.removePolicyFromStore(username, storeId, policyId);
            logger.info(String.format("User %s removed policy %d from store %d.", username, storeId, policyId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //from Store window, Actions menu, new page

    public Response getDiscountDescription(int discountID) {
        try {
            String desc = discountPolicyFacade.getDiscountDescription(discountID);
            logger.info(String.format("got description for discount %d.", discountID));
            return Response.createResponse(false, desc);
        }
        catch (Exception e) {
            logger.error("failed to get description" + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    public Response getDiscountConditionDescription(int condId) {
        try {
            String desc = discountPolicyFacade.getConditionDescription(condId);
            logger.info(String.format("got description for condition %d.", condId));
            return Response.createResponse(false, desc);
        }
        catch (Exception e) {
            logger.error("failed to get description" + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    public Response getBuyPolicyDescription(int policy) {
        try {
            String desc = buyPolicyFacade.getPolicyDescription(policy);
            logger.info(String.format("got description for policy %d.", policy));
            return Response.createResponse(false, desc);
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    public Response getStoreDiscountDescriptions(String username, int storeId) {
        try {
            List<PolicyDescriptionDTO> descs = storeFacade.getStoreDiscountDescriptions(username, storeId);
            logger.info(String.format("got discount descriptions for store %d.", storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(descs));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    public Response getStorePolicyDescriptions(String username, int storeId) {
        try {
            List<PolicyDescriptionDTO> descs = storeFacade.getStoreBuyPolicyDescriptions(username, storeId);
            logger.info(String.format("got buy policy descriptions for store %d.", storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(descs));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response createMinProductOnStoreCondition(String token, int minAmount, String username) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createMinProductOnStoreCondition(minAmount, username);
            logger.info(String.format("User %s added MinProduct On Category Condition", username));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createMinProductOnStoreCondition: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createMinProductOnCategoryCondition(String token, int minAmount, String categoryName, String username) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createMinProductOnCategoryCondition(minAmount, categoryName, username);
            logger.info(String.format("User %s added MinProduct On Category Condition", username));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createMinProductOnCategoryCondition: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createMinProductCondition(String token, int minAmount, int productID, String username) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createMinProductCondition(minAmount, productID, username);
            logger.info(String.format("User %s added MinProduct Condition", username));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createMinProductCondition: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createMinBuyCondition(String token, String username, int minBuy) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createMinBuyCondition(minBuy, username);
            logger.info(String.format("User %s added minBuy Condition", username));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createMinBuyCondition: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createXorCondition(String token, String username, int conditionAID, int conditionBID) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createXorCondition(conditionAID, conditionBID, username);
            logger.info(String.format("User %s added Xor Condition: XorCondition(%d, %d)", username, conditionAID, conditionBID));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createXorCondition: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOrCondition(String token, String username, int conditionAID, int conditionBID) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOrCondition(conditionAID, conditionBID, username);
            logger.info(String.format("User %s added Or Condition: OrCondition(%d, %d)", username, conditionAID, conditionBID));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOrCondition: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createAndCondition(String token, String username, int conditionAID, int conditionBID) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createAndCondition(conditionAID, conditionBID, username);
            logger.info(String.format("User %s added And Condition: AndCondition(%d, %d)", username, conditionAID, conditionBID));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createAndCondition: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOnProductSimpleDiscountPolicy(String token, String username, double percentage, int ProductID) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOnProductSimpleDiscountPolicy(percentage, ProductID, username);
            logger.info(String.format("User %s added OnProductSimple Discount policy: On product with ID <%s?", username, ProductID));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOnProductSimpleDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOnCategorySimpleDiscountPolicy(String token, String username, double percentage, String CategoryName) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOnCategorySimpleDiscountPolicy(percentage, CategoryName, username);
            logger.info(String.format("User %s added OnCategorySimple Discount policy: On category %s", username, CategoryName));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOnCategorySimpleDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOnStoreSimpleDiscountPolicy(String token, String username, double percentage) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOnStoreSimpleDiscountPolicy(percentage, username);
            logger.info(String.format("User %s added OnStoreSimple Discount policy: On store", username));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOnStoreSimpleDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOnProductConditionDiscountPolicy(String token, String username, double percentage, int ProductID, int conditionID) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOnProductConditionDiscountPolicy(percentage, ProductID, conditionID, username);
            logger.info(String.format("User %s added OnProductCondition Discount policy: On product with ID <%s?", username, ProductID));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOnProductConditionDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOnCategoryConditionDiscountPolicy(String token, String username, double percentage, String CategoryName, int conditionID) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOnCategoryConditionDiscountPolicy(percentage, CategoryName, conditionID, username);
            logger.info(String.format("User %s added OnCategoryCondition Discount policy: On category %s", username, CategoryName));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOnCategoryConditionDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOnStoreConditionDiscountPolicy(String token, String username, double percentage, int conditionID) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOnStoreConditionDiscountPolicy(percentage, conditionID, username);
            logger.info(String.format("User %s added OnStoreCondition Discount policy: On store", username));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOnStoreConditionDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createTakeMaxXorDiscountPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createTakeMaxXorDiscountPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added TakeMaxXor Discount policy: TakeMaxXor(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createTakeMaxXorDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createTakeMinXorDiscountPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createTakeMinXorDiscountPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added TakeMinXor Discount policy: TakeMinXor(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createTakeMinXorDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createAdditionDiscountPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createAdditionDiscountPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added Addition Discount policy: Addition(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createAdditionDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createMaximumDiscountPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createMaximumDiscountPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added Maximum Discount policy: Maximum(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createMaximumDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createAndDiscountPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createAndDiscountPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added And Discount policy: And(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createAndDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response createOrDiscountPolicy(String token, String username, int policyId1, int policyId2) {
        try {
            checkToken(token, username);
            int id = discountPolicyFacade.createOrDiscountPolicy(policyId1, policyId2, username);
            logger.info(String.format("User %s added Or Discount policy: Or(%d, %d)", username, policyId1, policyId2));
            return Response.createResponse(false, objectMapper.writeValueAsString(id));
        }
        catch (Exception e) {
            logger.error("createOeDiscountPolicy: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response addDiscountPolicyToStore(String token, String username, int storeId, int policyId) {
        try {
            checkToken(token, username);
            discountPolicyFacade.addDiscountPolicyToStore(storeId, policyId, username);
            logger.info(String.format("User %s added discount policy %d to store %d.", username, storeId, policyId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            logger.error("addDiscountPolicyToStore: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response removeDiscountPolicyToStore(String token, String username, int storeId, int policyId) {
        try {
            checkToken(token, username);
            discountPolicyFacade.removeDiscountPolicyFromStore(storeId, policyId,username);
            logger.info(String.format("User %s added discount policy %d to store %d.", username, storeId, policyId));
            return Response.createResponse(false, objectMapper.writeValueAsString(true));
        }
        catch (Exception e) {
            logger.error("addDiscountPolicyToStore: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //from Store window, Actions menu, new page

    @Transactional
    public Response changeManagerPermission(String token, String currentOwnerUsername, String newManagerUsername, int storeId, Set<Permission> permission) {
        try {
            checkToken(token, currentOwnerUsername);
            storeFacade.addManagerPermission(currentOwnerUsername, newManagerUsername, storeId, permission);
            logger.info(String.format("User %s added permission to user %s in store %d", currentOwnerUsername, newManagerUsername, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(newManagerUsername));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //Store window->Actions menu->get Managers->choose a manager->shows details and permissions->edit permissions

    @Transactional
    public Response changeManagerPermission(String token, String currentOwnerUsername, String newManagerUsername, int storeId, HashSet<Permission> permission) {
        try {
            checkToken(token, currentOwnerUsername);
            storeFacade.addManagerPermission(currentOwnerUsername, newManagerUsername, storeId, permission);
            logger.info(String.format("User %s added permission to user %s in store %d", currentOwnerUsername, newManagerUsername, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(newManagerUsername));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //Store window->Actions menu->get Managers->choose a manager->shows details and permissions->edit permissions

    public Response getManagerPermissions(String token, String currentOwnerUsername, String managerUsername, int storeId) {
        try {
            checkToken(token, currentOwnerUsername);
            if(!storeFacade.getIsManager(currentOwnerUsername, storeId, managerUsername)){
                logger.error("getManagerPermissions: User " + managerUsername + " isn't a manager");
                return Response.createResponse(true, "User isn't a manager");
            }
            logger.info(String.format("User %s got permission of user %s in store %d", currentOwnerUsername, managerUsername, storeId));
            return Response.createResponse(false, objectMapper.writeValueAsString(userFacade.getManagerPermissions(currentOwnerUsername, managerUsername, storeId)));
        }
        catch (Exception e) {
            return handleException(e);
        }
    } //Store window->Actions menu->get Managers->choose a manager->shows details and permissions

    public Response getManagerPermissionsInt(String token, String currentOwnerUsername, String managerUsername, int storeId) {
        try {
            checkToken(token, currentOwnerUsername);
            if(!storeFacade.getIsManager(currentOwnerUsername, storeId, managerUsername)){
                logger.error("getManagerPermissions: User " + managerUsername + " isn't a manager");
                return Response.createResponse(true, "User isn't a manager");
            }
            logger.info(String.format("User %s got permission of user %s in store %d", currentOwnerUsername, managerUsername, storeId));
            List<Integer> enumInts = new LinkedList<>();
            List<Permission> perms = userFacade.getManagerPermissions(currentOwnerUsername, managerUsername, storeId);
            for(Permission perm : perms){
                enumInts.add(perm.getValue());
            }
            return Response.createResponse(false, objectMapper.writeValueAsString(enumInts));
        }
        catch (Exception e) {
            return handleException(e);
        }
    }

    @Transactional
    public Response login(String username, String password){
        try{
            logger.info("user {} tries to login", username);
            String token= authFacade.login(username, password);
            logger.info("user {} logged in", username);
            return Response.createResponse(false, token);

        }catch(Exception e){
            logger.error("error in login: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //Top right, choose "Login", new page

    @Transactional
    public Response login(String username, String password,int guestId){
        try{
            logger.info("user {} tries to login from guestId={}", username,guestId);
            String token= authFacade.login(username, password,guestId);
            logger.info("user {} logged in", username);
            return Response.createResponse(false, token);

        }catch(Exception e){
            logger.error("error in login: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //Top right, choose "Login", new page

    @Transactional
    public Response logout(String username){
        try{
            logger.info(username, username);
            int guestId=userFacade.logout(username);
            logger.info("{} logged out",username);
            return Response.createResponse(guestId);

        }catch(Exception e){
            logger.error("error in logout: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //dropdown menu as Member, choose "Logout"
    @Transactional
        public Response exitGuest(int guestId){
            try{
                logger.info("guest {} tries to exit", guestId);
                userFacade.exitGuest(guestId);
                logger.info("guest {} exited", guestId);
                return Response.createResponse();

            }catch(Exception e){
                logger.error("error in exitGuest: "+e.getMessage());
                return Response.createResponse(true, e.getMessage());
            }
        } //exit page

    @Transactional
    public Response enterAsGuest(){
        try{
            logger.info("guest tries to enter");
            int guestId=userFacade.enterAsGuest();
            logger.info("guest entered with id {}", guestId);
            return Response.createResponse(guestId);
        }catch(Exception e){
            logger.error("error in enterAsGuest: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //enter page

    @Transactional
    public Response register(String username, String password,String firstName, String lastName,String emailAddress,String phoneNumber, LocalDate birthDate){
        try{
            logger.info("user {} tries to register", username);
            authFacade.register(username,password,firstName, lastName, emailAddress, phoneNumber,birthDate);
            logger.info("user {} registered", username);
            return Response.createResponse();

        }catch(Exception e){
            logger.error("error in register: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //Top right, choose "Register", new page

    public Response memberExists(String username){
        try{
            logger.info("user {} tries to check if exists", username);
            boolean res = userFacade.isExist(username);
            logger.info("user {} exists: {}", username, res);
            return Response.createResponse(false, String.valueOf(res));
        }catch(Exception e){
            logger.error("error in memberExists: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    public Response authenticate(String token, String username){
        try{
            logger.info("user {} tries to authenticate", username);
            checkToken(token, username);
            logger.info("user {} authenticated", username);
            return Response.createResponse(false, "true");

        }catch(Exception e){
            logger.error("error in authenticate: "+e.getMessage());
            return Response.createResponse(false, "false");
        }
    }

    @Transactional
    public Response setFirstName(String username, String firstName) {
        try {
            logger.info("user {} tries to set first name= {}", username,firstName);
            userFacade.setFirstName(username, firstName);
            logger.info("user {} set first name {}", username,firstName);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("error in setFirstName: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From profile page, textbox

    @Transactional
    public Response setLastName(String username, String lastName) {
        try {
            logger.info("user {} tries to set last name= {}", username,lastName);
            userFacade.setLastName(username, lastName);
            logger.info("user {} set last name= {}", username,lastName);
            return Response.createResponse();
        } catch (Exception e) {
            return Response.createResponse(true, e.getMessage());
        }
    } //From profile page, textbox

    @Transactional
    public Response setEmailAddress(String username, String emailAddress) {
        try {
            logger.info("user {} tries to set email address= {}", username,emailAddress);
            userFacade.setEmailAddress(username, emailAddress);
            logger.info("user {} set email address= {}", username,emailAddress);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("error in setEmailAddress: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From profile page, textbox

    @Transactional
    public Response setPhoneNumber(String username, String phoneNumber) {
        logger.info("Setting phoneNumber for username: {}", username);
        try {
            userFacade.setPhoneNumber(username, phoneNumber);
            logger.info("Set phoneNumber successful for username: {}", username);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Set phoneNumber failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From profile page, textbox

    @Transactional
    public Response setBirthDate(String username, LocalDate birthDate) {
        try {
            logger.info("user {} tries to set birth date= {}", username,birthDate);
            userFacade.setBirthDate(username, birthDate);
            logger.info("user {} set birth date= {}", username,birthDate);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("error in setBirthDate: "+e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From profile page, textbox

    @Transactional
    public Response addProductToCart(String username, int storeId, int productId, int amount) {
        logger.info("Adding product to cart for username: {}, storeId: {}, productId: {}, amount: {}", username, storeId, productId, amount);
        try {
            if (amount <= 0) {
                logger.error("Amount should be above 0 for username: {}, storeId: {}, productId: {}, amount: {}", username, storeId, productId, amount);
                throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
            }
            userFacade.addProductToCart(username, storeId, productId, amount);
            logger.info("Add product to cart successful for username: {}, storeId: {}, productId: {}, amount: {}", username, storeId, productId, amount);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Add product to cart failed for username: {}, storeId: {}, productId: {}, amount: {}. Error: {}", username, storeId, productId, amount, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //from Product Page, choose quantity and then "Add to Cart"

    @Transactional
    public Response addProductToCart(int guestId, int storeId, int productId, int amount) {
        logger.info("Adding product to cart for guestId: {}, storeId: {}, productId: {}, amount: {}", guestId, storeId, productId, amount);
        try {
            if (amount <= 0) {
                logger.error("Amount should be above 0 for guestId: {}, storeId: {}, productId: {}, amount: {}", guestId, storeId, productId, amount);
                throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
            }
            userFacade.addProductToCart(guestId, storeId, productId, amount);
            logger.info("Add product to cart successful for guestId: {}, storeId: {}, productId: {}, amount: {}", guestId, storeId, productId, amount);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Add product to cart failed for guestId: {}, storeId: {}, productId: {}, amount: {}. Error: {}", guestId, storeId, productId, amount, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //from Product Page, choose quantity and then "Add to Cart"

    @Transactional
    public Response removeProductFromCart(String username, int storeId, int productId) {
        logger.info("Removing product from cart for username: {}, storeId: {}, productId: {}", username, storeId, productId);
        try {
            userFacade.removeProductFromCart(username, storeId, productId);
            logger.info("Remove product from cart successful for username: {}, storeId: {}, productId: {}", username, storeId, productId);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Remove product from cart failed for username: {}, storeId: {}, productId: {}. Error: {}", username, storeId, productId, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //from "Cart" page, click on X/Trash next to product

    @Transactional
    public Response removeProductFromCart(int guestId, int storeId, int productId) {
        logger.info("Removing product from cart for guestId: {}, storeId: {}, productId: {}", guestId, storeId, productId);
        try {
            userFacade.removeProductFromCart(guestId, storeId, productId);
            logger.info("Remove product from cart successful for guestId: {}, storeId: {}, productId: {}", guestId, storeId, productId);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Remove product from cart failed for guestId: {}, storeId: {}, productId: {}. Error: {}", guestId, storeId, productId, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //from "Cart" page, click on X/Trash next to product

    @Transactional
    public Response changeQuantityCart(String username, int storeId, int productId, int amount) {
        logger.info("Changing quantity in cart for username: {}, storeId: {}, productId: {}, amount: {}", username, storeId, productId, amount);
        try {
            if (amount <= 0) {
                logger.error("Amount should be above 0 for username: {}, storeId: {}, productId: {}, amount: {}", username, storeId, productId, amount);
                throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
            }
            userFacade.changeQuantityCart(username, storeId, productId, amount);
            logger.info("Change quantity in cart successful for username: {}, storeId: {}, productId: {}, amount: {}", username, storeId, productId, amount);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Change quantity in cart failed for username: {}, storeId: {}, productId: {}, amount: {}. Error: {}", username, storeId, productId, amount, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //from "Cart" page, edit Textbox next to product

    @Transactional
    public Response changeQuantityCart(int guestId, int storeId, int productId, int amount) {
        logger.info("Changing quantity in cart for guestId: {}, storeId: {}, productId: {}, amount: {}", guestId, storeId, productId, amount);
        try {
            if (amount <= 0) {
                logger.error("Amount should be above 0 for guestId: {}, storeId: {}, productId: {}, amount: {}", guestId, storeId, productId, amount);
                throw new IllegalArgumentException(Error.makeCartAmountAboveZeroError());
            }
            userFacade.changeQuantityCart(guestId, storeId, productId, amount);
            logger.info("Change quantity in cart successful for guestId: {}, storeId: {}, productId: {}, amount: {}", guestId, storeId, productId, amount);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Change quantity in cart failed for guestId: {}, storeId: {}, productId: {}, amount: {}. Error: {}", guestId, storeId, productId, amount, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //from "Cart" page, edit Textbox next to product


    public Response getUserCart(int guestId) {
        logger.info("Getting user cart for guestId: {}", guestId);
        try {
            List<CartItemDTO> items = userFacade.getCartItems(guestId);
            logger.info("Get user cart successful for guestId: {}", guestId);
            return Response.createResponse(false, objectMapper.writeValueAsString(items));
        } catch (Exception e) {
            logger.error("Get user cart failed for guestId: {}. Error: {}", guestId, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //Cart page, for each product show amount and price before and after discount

    @Transactional
    public Response acceptRequest(String acceptingName, int requestID) {
        logger.info("Accepting request for acceptingName: {}, requestID: {}", acceptingName, requestID);
        try {
            userFacade.accept(acceptingName, requestID);
            logger.info("Accept request successful for acceptingName: {}, requestID: {}", acceptingName, requestID);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Accept request failed for acceptingName: {}, requestID: {}. Error: {}", acceptingName, requestID, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From notifications, choose "Request" notification and click "accept", "Accept" and "Reject" on the request

    @Transactional
    public Response okNotification(String username, int notifID) {
        logger.info("Okaying notification for username: {}, notifID: {}", username, notifID);
        try {
            userFacade.ok(username, notifID);
            logger.info("Ok notification successful for username: {}, notifID: {}", username, notifID);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Ok notification failed for username: {}, notifID: {}. Error: {}", username, notifID, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response rejectRequest(String rejectingName, int requestID) {
        logger.info("Rejecting request for rejectingName: {}, requestID: {}", rejectingName, requestID);
        try {
            userFacade.reject(rejectingName, requestID);
            logger.info("Reject request successful for rejectingName: {}, requestID: {}", rejectingName, requestID);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Reject request failed for rejectingName: {}, requestID: {}. Error: {}", rejectingName, requestID, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response setSystemAdminstor(String username) {
        logger.info("Setting system administrator for username: {}", username);
        try {
            userFacade.setSystemManagerUserName(username);
            logger.info("Set system administrator successful for username: {}", username);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Set system administrator failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    @Transactional
    public Response leaveRole(String username, int storeId) {
        logger.info("Leaving role for username: {}, storeId: {}", username, storeId);
        try {
            userFacade.leaveRole(username, storeId);
            logger.info("Leave role successful for username: {}, storeId: {}", username, storeId);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Leave role failed for username: {}, storeId: {}. Error: {}", username, storeId, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From store page, Actions menu, popup

    public Response getOrderHistory(String username) {
        logger.info("Getting order history for username: {}", username);
        try {
            Map<Integer, OrderDetails> orders = orderFacade.getProductDataPriceByMember(username);
            logger.info("Get order history successful for username: {}", username);
            return Response.createResponse(false, objectMapper.writeValueAsString(orders));
        } catch (Exception e) {
            logger.error("Get order history failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From profile page or system manager menu->choose username

    public Response getOrderDTOHistory(String username) {
        logger.info("Getting order DTO history for username: {}", username);
        try {
            List<OrderDTO> orders = userFacade.getUserOrderDTOs(username);
            logger.info("Get order DTO history successful for username: {}", username);
            return Response.createResponse(false, objectMapper.writeValueAsString(orders));
        } catch (Exception e) {
            logger.error("Get order DTO history failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From profile page or system manager menu->choose username

    public Response getAllOrderDTOHistory(String username) {
        logger.info("Getting all order DTO history for username: {}", username);
        try {
            List<OrderDTO> orders = userFacade.getAllOrders(username);
            logger.info("Get all order DTO history successful for username: {}", username);
            return Response.createResponse(false, objectMapper.writeValueAsString(orders));
        } catch (Exception e) {
            logger.error("Get all order DTO history failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }//From profile page or system manager menu

    public Response viewCart(String username) {
        logger.info("Viewing cart for username: {}", username);
        try {
            UserOrderDTO userOrderDTO=userFacade.viewCart(username);
            logger.info("View cart successful for username: {}", username);
            return Response.createResponse(false,objectMapper.writeValueAsString(userOrderDTO));
        } catch (Exception e) {
            logger.error("View cart failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //Cart page, for each product show amount and price before and after discount

    @Transactional
    public Response purchaseCart(String username, CreditCardDTO creditCard, AddressDTO addressDTO) {
        logger.info("Purchasing cart for username: {}, creditCard: {}, addressDTO: {}", username, creditCard, addressDTO);
        try {
            userFacade.purchaseCart(username, creditCard, addressDTO);
            logger.info("Purchase cart successful for username: {}", username);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Purchase cart failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //button in Cart view, "Buy", begin procedure in new pages

    public Response viewCart(int guestId) {
        logger.info("Viewing cart for guestId: {}", guestId);
        try {
            UserOrderDTO userOrderDTO=userFacade.viewCart(guestId);
            logger.info("View cart successful for guestId: {}", guestId);
            return Response.createResponse(false,objectMapper.writeValueAsString(userOrderDTO));
        } catch (Exception e) {
            logger.error("View cart failed for guestId: {}. Error: {}", guestId, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //Cart page, for each product show amount and price before and after discount

    @Transactional
    public Response purchaseCart(int guestId, CreditCardDTO creditCard, AddressDTO addressDTO) {
        logger.info("Purchasing cart for guestId: {}, creditCard: {}, addressDTO: {}", guestId, creditCard, addressDTO);
        try {
            userFacade.purchaseCart(guestId, creditCard, addressDTO);
            logger.info("Purchase cart successful for guestId: {}", guestId);
            return Response.createResponse();
        } catch (Exception e) {
            logger.error("Purchase cart failed for guestId: {}. Error: {}", guestId, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //button in Cart view, "Buy", begin procedure in new pages
    

    public Response getIsOwner(String token, String username, int storeId, String ownerUsername) {
        try {
            checkToken(token, username);
            logger.info(String.format("%s checked if %s is an owner of store %d.", username, ownerUsername, storeId));
            return Response.createResponse(false, String.valueOf(storeFacade.getIsOwner(username, storeId, ownerUsername)));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    public Response getIsActive(int storeId) {
        try {
            logger.info(String.format("Checked if store %d is active.", storeId));
            return Response.createResponse(false, String.valueOf(storeFacade.isStoreActive(storeId)));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    public Response getIsFounder(String token, String username, int storeId, String founderUsername) {
        try {
            checkToken(token, username);
            logger.info(String.format("%s checked if %s is a founder of store %d.", username, founderUsername, storeId));
            return Response.createResponse(false, String.valueOf(storeFacade.getIsFounder(username, storeId, founderUsername)));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    public Response getIsManager(String token, String username, int storeId, String managerUsername) {
        try {
            checkToken(token, username);
            logger.info(String.format("%s checked if %s is a manager of store %d.", username, managerUsername, storeId));
            return Response.createResponse(false, String.valueOf(storeFacade.getIsManager(username, storeId, managerUsername)));
        } catch (Exception e) {
            return handleException(e);
        }
    }

    public Response getAllProducts(String username) {
        try {
            List<ProductDTO> productDTOs = productFacade.getAllProducts();
            logger.info(String.format("User %s got all market products", username));
            return Response.createResponse(false, objectMapper.writeValueAsString(productDTOs));
        } catch (Exception e) {
            logger.error("getAllProducts: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    public Response getFilteredProducts(String username, String productName, double minProductPrice,
            double maxProductPrice,
            String productCategory,
            double minProductRank) {
        try {

            List<ProductDTO> productDTOs = productFacade.getAllFilteredProducts(productName, minProductPrice,
                    maxProductPrice, productCategory, minProductRank);
            logger.info(String.format("User %s got all market products filtered", username));
            if(productDTOs.isEmpty()){
                return Response.createResponse(true, "No results for products were found");
            }
            return Response.createResponse(false, objectMapper.writeValueAsString(productDTOs));
        } catch (Exception e) {
            logger.error("getFilteredProducts: " + e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    } //From main search or search in store
    public Response getMemberDto(String username){
        try{
            logger.info("get member dto for {}", username);
            MemberDTO memberDTO = userFacade.getMemberDTO(username);
            logger.info("finished get member dto {}", memberDTO);
            return Response.createResponse(false, objectMapper.writeValueAsString(memberDTO));
        }catch(Exception e){
            logger.error("get member dto failed for username: {}. Error: {}", username, e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }

    }

    public Response hasPermission(String token, String actorUsername, int storeId, String actionUsername, int permission){
        try{
            checkToken(token, actorUsername);
            Permission permissionEnum = Permission.getEnumByInt(permission);
            logger.info(String.format("%s checked if %s has %s permission in store %d.", actorUsername, actionUsername, permissionEnum.toString(), storeId));
            boolean res = storeFacade.hasPermission(actorUsername, actionUsername, storeId, permissionEnum);
            return Response.createResponse(false, objectMapper.writeValueAsString(res));
        }catch(Exception e){
            return handleException(e);
        }
    }

    public Response getUserRoles(String username){
        try{
            logger.info("get user roles for {}", username);
            List<UserRoleDTO> res = userFacade.getMemberRoles(username);
            logger.info("finished get user roles {}", res);
            return Response.createResponse(false, objectMapper.writeValueAsString(res));
        }catch(Exception e){
            logger.error("error get member roles {}", e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    public Response getUserNotifications(String username){
        try{
            logger.info("get user notifications for {}", username);
            List<NotificationDTO> res = userFacade.getNotifications(username);
            logger.info("finished get user notifications {}", res);
            return Response.createResponse(false, objectMapper.writeValueAsString(res));
        }catch(Exception e){
            logger.error("error get member notifications {}", e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }

    public Response sendMessage(String username, String message){
        try{
            userFacade.notify(username, message);
            return Response.createResponse(false, "cool");
        }catch(Exception e){
            logger.error("error get member notifications {}", e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }
    public Response checkMemberCart(String username){
        try{
            logger.info("check member cart for {}", username);
            userFacade.checkCart(username);
            logger.info("finished check member cart {} without errors",username);
            return Response.createResponse();    
    }catch(Exception e){
        logger.error("error check member {} cart {}",username ,e.getMessage());
        return Response.createResponse(true, e.getMessage());
    }
    }
    public Response checkGuestCart(int guestId){
        try{
            logger.info("check guest cart for {}", guestId);
            userFacade.checkCart(guestId);
            logger.info("finished check guest cart {} without errors",guestId);
            return Response.createResponse();
        }catch(Exception e){
            logger.error("error check guest {} cart {}",guestId ,e.getMessage());
            return Response.createResponse(true, e.getMessage());
        }
    }
    public Response checkIfSystemManager(String username){
        try{
            logger.info("check if {} is system manager", username);
            boolean res = userFacade.isSystemManager(username);
            logger.info("finished check if {} is system manager", username);
            return Response.createResponse(false, objectMapper.writeValueAsString(res));
        }catch(Exception e){
            logger.error("error check if {} is system manager", username);
            return Response.createResponse(true, e.getMessage());
        }
    }
    public Response getTopProducts(){
        try{
            logger.info("get top products");
            List<ProductDTO> res = productFacade.getTopProducts();
            logger.info("finished get top products");
            return Response.createResponse(false, objectMapper.writeValueAsString(res));
        }catch(Exception e){
            logger.error("error get top products");
            return Response.createResponse(true, e.getMessage());
        }
    }
    public Response isGuestExist(int guestId){
        try{
            logger.info("check if guest {} exists", guestId);
            boolean res = userFacade.isGuestExist(guestId);
            logger.info("finished check if guest {} exists", guestId);
            return Response.createResponse(false, objectMapper.writeValueAsString(res));
        }catch(Exception e){
            logger.error("error check if guest {} exists", guestId);
            return Response.createResponse(true, e.getMessage());
        }
    }

    public void clear(){
        logger.info("clearing all data");
        IStoreRepository.cleanDB();
        discountPolicyFacade.clear();
        buyPolicyFacade.clear();
        authFacade.clear();
        userFacade.clear();
        productFacade.clear();
        orderFacade.clear();
        logger.info("finished clearing all data");
    }
}
