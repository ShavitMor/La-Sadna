package com.sadna.sadnamarket.domain;

import static org.junit.Assert.*;
import static org.junit.jupiter.api.Assertions.assertDoesNotThrow;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.Mockito.doNothing;
import static org.mockito.Mockito.when;

import java.time.LocalDate;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.NoSuchElementException;
import java.util.concurrent.TimeoutException;

import com.sadna.sadnamarket.domain.buyPolicies.BuyPolicyFacade;
import com.sadna.sadnamarket.domain.buyPolicies.MemoryBuyPolicyRepository;
import com.sadna.sadnamarket.domain.discountPolicies.Conditions.MemoryConditionRepository;
import com.sadna.sadnamarket.domain.discountPolicies.DiscountPolicyFacade;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.MemoryDiscountPolicyRepository;
import com.sadna.sadnamarket.domain.orders.IOrderRepository;
import com.sadna.sadnamarket.domain.orders.MemoryOrderRepository;
import com.sadna.sadnamarket.domain.payment.*;
import com.sadna.sadnamarket.domain.products.IProductRepository;
import com.sadna.sadnamarket.domain.products.MemoryProductRepository;
import com.sadna.sadnamarket.domain.products.ProductFacade;
import com.sadna.sadnamarket.domain.stores.IStoreRepository;
import com.sadna.sadnamarket.domain.stores.MemoryStoreRepository;
import com.sadna.sadnamarket.domain.supply.*;
import com.sadna.sadnamarket.domain.users.*;
import com.sadna.sadnamarket.service.Error;

import org.junit.Before;
import org.junit.Test;

import com.sadna.sadnamarket.domain.auth.AuthFacade;
import com.sadna.sadnamarket.domain.auth.AuthRepositoryMemoryImpl;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.orders.OrderFacade;
import com.sadna.sadnamarket.domain.stores.StoreDTO;
import com.sadna.sadnamarket.domain.stores.StoreFacade;

import javax.transaction.Transactional;

@Transactional
public class UserFacadeIntegrationTest {

    private MemoryRepo iUserRepo;
    private AuthRepositoryMemoryImpl iAuthRepo;

    private ProductFacade productFacade;
    private BuyPolicyFacade buyPolicyFacade;
    private UserFacade userFacade;
    private AuthFacade authFacade;
    private StoreFacade storeFacade;
    private OrderFacade orderFacade;

    private PaymentService paymentService;

    private SupplyService supplyService;

    private String testUsername1 = "idanasis";
    private String testUsername2 = "shavitmor";
    private String testUsername3 = "Nir";
    private String testPassword = "12";
    private int testStoreId;
    private int testStoreId2;
    private int testProductId1;
    private int testProductId2;
    private LocalDate testDate = LocalDate.of(1990, 11, 11);
    private DiscountPolicyFacade discountPolicyFacade;

    private void setUpFacades() {
        IOrderRepository orderRepo = new MemoryOrderRepository();
        OrderFacade orderFacade = new OrderFacade(orderRepo);
        this.orderFacade = orderFacade;

        IStoreRepository storeRepo = new MemoryStoreRepository();
        this.storeFacade = new StoreFacade(storeRepo);

        MemoryRepo userRepo = new MemoryRepo();
        this.iUserRepo = userRepo;
        this.userFacade = new UserFacade(userRepo, storeFacade, orderFacade);

        IProductRepository productRepo = new MemoryProductRepository();
        this.productFacade = new ProductFacade(productRepo);

        this.buyPolicyFacade = new BuyPolicyFacade(new MemoryBuyPolicyRepository());
        this.discountPolicyFacade = new DiscountPolicyFacade(new MemoryConditionRepository(),
                new MemoryDiscountPolicyRepository());
        this.discountPolicyFacade.setProductFacade(productFacade);
        this.discountPolicyFacade.setStoreFacade(storeFacade);

        this.storeFacade.setUserFacade(userFacade);
        this.storeFacade.setOrderFacade(orderFacade);
        this.storeFacade.setProductFacade(productFacade);
        this.storeFacade.setBuyPolicyFacade(buyPolicyFacade);
        this.storeFacade.setDiscountPolicyFacade(discountPolicyFacade);

        this.orderFacade.setStoreFacade(storeFacade);

        AuthRepositoryMemoryImpl authRepo = new AuthRepositoryMemoryImpl();
        this.iAuthRepo = authRepo;
        this.authFacade = new AuthFacade(authRepo, userFacade);
        this.buyPolicyFacade.setStoreFacade(storeFacade);
        this.buyPolicyFacade.setUserFacade(userFacade);
        this.buyPolicyFacade.setProductFacade(productFacade);

        this.paymentService = PaymentService.getInstance();
        this.supplyService = SupplyService.getInstance();
        turnOn_externalServices();

    }

    @Before
    public void setUp()  {
        setUpFacades();

        authFacade.register(testUsername1, testPassword, "Idan", "Idan", "idan@gmail.com", "0501118121", testDate);
        authFacade.login(testUsername1, testPassword);
        authFacade.register(testUsername2, testPassword, "Shavit", "Mor", "shavit@gmail.com", "05033303030", testDate);
        authFacade.login(testUsername2, testPassword);
        authFacade.register(testUsername3, testPassword, "Nir", "Mor", "nir@gmail.com", "05033303030", testDate);
        authFacade.login(testUsername3, testPassword);

        this.testStoreId = storeFacade.createStore(testUsername1, "store name", "address", "email@gmail.com",
                "0588888888");
        this.testProductId1 = storeFacade.addProductToStore(testUsername1, testStoreId, "productName1", 10, 12.0,
                "cat1", 3.8, 4.0);
        this.testProductId2 = storeFacade.addProductToStore(testUsername1, testStoreId, "productName2", 5, 10.0, "cat2",
                3.8, 4.0);

    }

    public void turnOn_externalServices() {
        this.paymentService.setController(new PaymentInterface() {
            @Override
            public boolean creditCardValid(CreditCardDTO creditDetails) {
                return true;
            }

            @Override
            public boolean pay(double amount, CreditCardDTO payerCard, BankAccountDTO receiverAccount) {
                return true;
            }
        });
        this.supplyService.setController(new SupplyInterface() {
            @Override
            public boolean canMakeOrder(OrderDetailsDTO orderDetails, AddressDTO address) {
                return true;
            }

            @Override
            public String makeOrder(OrderDetailsDTO orderDetails, AddressDTO address) {
                return "null";
            }

            @Override
            public boolean cancelOrder(String orderCode) {
                return true;
            }
        });
    }

    @Test
    public void testEnterAsGuest() {
        int guestId = userFacade.enterAsGuest();
        assertEquals(1, guestId);
    }

    @Test
    public void testExitGuest() {
        assertThrows(NoSuchElementException.class, () -> userFacade.exitGuest(5));
        int guestId = userFacade.enterAsGuest();
        assertDoesNotThrow(() -> userFacade.exitGuest(guestId));
    }

    @Test
    public void testNotify() {
        userFacade.notify(testUsername1, "hi");
        List<NotificationDTO> ans = userFacade.getNotifications(testUsername1);
        assertEquals(1, ans.size());
        assertEquals("hi", ans.get(0).getMessage());
    }

    @Test
    public void testLogin() {
        authFacade.register("yosi", testPassword, "sami", "hatuka", "sami@gmail.com", "0501118121", testDate);
        authFacade.login("yosi", testPassword);
        assertTrue(userFacade.isLoggedIn("yosi"));
    }

    @Test
    public void testLogout() {
        userFacade.logout(testUsername1);
        assertFalse(userFacade.isLoggedIn(testUsername1));
    }

    @Test(expected = NoSuchElementException.class)
    public void testLogoutUserNotFound() {
        userFacade.logout("testUser");
    }

    @Test
    public void testRegister() {
        authFacade.register("Jimi", testPassword, "Jimi", "hatuka", "Jimi@gmail.com", "0501118121", testDate);
        assertDoesNotThrow(() -> iUserRepo.getMember("Jimi"));
    }

    @Test
    public void testRegisterWithSameUsername() {
        assertThrows(IllegalArgumentException.class, () -> authFacade.register(testUsername1, "12", "Idan", "Idan",
                "idan@gmail.com", "0501118121", testDate));
    }

    @Test
    public void testAddStoreManager() {
        userFacade.addStoreFounder(testUsername1, testStoreId);
        userFacade.addManagerRequest(testUsername1, testUsername2, testStoreId);
        assertTrue(userFacade.getNotifications(testUsername2).size() > 0);
        assertDoesNotThrow(() -> userFacade.accept(testUsername2, 1));
        assertTrue(userFacade.getMemberRoles(testUsername2).size() > 0);
    }

    @Test
    public void testAddStoreOwner() {
        userFacade.addStoreFounder(testUsername1, testStoreId2);
        userFacade.addOwnerRequest(testUsername1, testUsername2, testStoreId2);
        assertTrue(userFacade.getNotifications(testUsername2).size() > 0);
        assertDoesNotThrow(() -> userFacade.accept(testUsername2, 1));
        assertTrue(userFacade.getMemberRoles(testUsername2).size() > 0);
    }

    @Test
    public void testAddStoreOwnerFailWhichIsntRelatedToStore()  {
        int testStoreId3 = storeFacade.createStore(testUsername2, "store name 2", "address", "email@gmail.com",
                "0588888888");
        assertThrows(IllegalArgumentException.class,
                () -> userFacade.addOwnerRequest(testUsername1, testUsername2, testStoreId3));
        assertTrue(userFacade.getNotifications(testUsername2).size() == 0);
    }

    @Test
    public void testAddStoreOwnerFailWhoAppointedHim() {
        userFacade.addStoreFounder(testUsername1, testStoreId2);
        userFacade.addOwnerRequest(testUsername1, testUsername2, testStoreId2);
        assertTrue(userFacade.getNotifications(testUsername2).size() > 0);
        assertDoesNotThrow(() -> userFacade.accept(testUsername2, 1));
        assertThrows(IllegalStateException.class,
                () -> userFacade.addOwnerRequest(testUsername2, testUsername1, testStoreId));
    }

    @Test
    public void testAddPermission() {
        userFacade.addStoreFounder(testUsername1, testStoreId2);
        userFacade.addManagerRequest(testUsername1, testUsername2, testStoreId2);
        assertTrue(userFacade.getNotifications(testUsername2).size() > 0);
        assertDoesNotThrow(() -> userFacade.accept(testUsername2, 1));
        assertDoesNotThrow(() -> userFacade.addPremssionToStore(testUsername1, testUsername2, testStoreId2,
                Permission.ADD_BUY_POLICY));
        assertTrue(userFacade.checkPremssionToStore(testUsername2, testStoreId2, Permission.ADD_BUY_POLICY));
    }

    @Test
    public void testFounderLeaveRoleFail() {
        userFacade.addStoreFounder(testUsername1, testStoreId2);
        assertThrows(IllegalStateException.class, () -> userFacade.leaveRole(testUsername1, testStoreId2));
    }

    @Test
    public void testManagerLeaveRole() {
        userFacade.addStoreFounder(testUsername1, testStoreId2);
        userFacade.addManagerRequest(testUsername1, testUsername2, testStoreId2);
        userFacade.accept(testUsername2, 1);
        assertTrue(userFacade.getMemberRoles(testUsername2).size() > 0);
        userFacade.leaveRole(testUsername2, testStoreId);
        assertTrue(userFacade.getMemberRoles(testUsername2).size() == 0);
    }

    @Test
    public void testOwnerLeaveRole() {
        userFacade.addStoreFounder(testUsername1, testStoreId2);
        userFacade.addOwnerRequest(testUsername1, testUsername2, testStoreId);
        userFacade.accept(testUsername2, 1);
        userFacade.addOwnerRequest(testUsername2, testUsername3, testStoreId);
        userFacade.accept(testUsername3, 1);
        assertEquals(true, userFacade.isApointee(testUsername3, testUsername2, testStoreId));
        userFacade.leaveRole(testUsername2, testStoreId);
        assertTrue(userFacade.getMemberRoles(testUsername2).size() == 0);
        assertTrue(userFacade.getMemberRoles(testUsername3).size() == 0);
    }

    @Test
    public void testUserAddProduct() {
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);

        List<CartItemDTO> items = userFacade.getMemberCart(testUsername1);
        assertEquals(1, items.size());
        assertEquals(testProductId1, items.get(testStoreId).getProductId());
        assertEquals(2, items.get(testStoreId).getAmount());

        userFacade.addProductToCart(testUsername1, testStoreId, testProductId2, 3);
        items = userFacade.getMemberCart(testUsername1);
        assertEquals(2, items.size());
    }

    @Test
    public void testUserRemoveProduct() {
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.removeProductFromCart(testUsername1, testStoreId, testProductId1);
        List<CartItemDTO> items = userFacade.getMemberCart(testUsername1);;
        assertEquals(0, items.size());
    }

    @Test
    public void testUserChangeAmountOfProduct() {
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.changeQuantityCart(testUsername1, testStoreId, testProductId1, 3);
        List<CartItemDTO> items = userFacade.getMemberCart(testUsername1);;
        assertEquals(1, items.size());
        assertEquals(0, items.get(testStoreId).getProductId());
        assertEquals(3, items.get(testStoreId).getAmount());
    }

    @Test
    public void testGuestAddProduct() {
        int guestId = userFacade.enterAsGuest();
        userFacade.addProductToCart(guestId, testStoreId, testProductId1, 2);
        List<CartItemDTO> items = userFacade.getCartItems(guestId);
        assertEquals(1, items.size());
        assertEquals(0, items.get(testStoreId).getProductId());
        assertEquals(2, items.get(testStoreId).getAmount());
        userFacade.addProductToCart(guestId, testStoreId, testProductId2, 3);
        items = userFacade.getCartItems(guestId);
        assertEquals(2, items.size());
    }

    @Test
    public void testGuestRemoveProduct() {
        int guestId = userFacade.enterAsGuest();
        userFacade.addProductToCart(guestId, testStoreId, testProductId1, 2);
        userFacade.removeProductFromCart(guestId, testStoreId, testProductId1);
        List<CartItemDTO> items = userFacade.getCartItems(guestId);
        assertEquals(0, items.size());
    }

    @Test
    public void testGuestChangeAmountOfProduct() {
        int guestId = userFacade.enterAsGuest();
        userFacade.addProductToCart(guestId, testStoreId, testProductId1, 2);
        userFacade.changeQuantityCart(guestId, testStoreId, testProductId1, 3);
        List<CartItemDTO> items = userFacade.getCartItems(guestId);
        assertEquals(1, items.size());
        assertEquals(0, items.get(testStoreId).getProductId());
        assertEquals(3, items.get(testStoreId).getAmount());
    }

    @Test
    public void validateCartMoveWithGuestWhenLogin() {
        userFacade.logout(testUsername2);
        int guestId = userFacade.enterAsGuest();
        userFacade.addProductToCart(guestId, testStoreId, testProductId1, 2);
        authFacade.login(testUsername2, testPassword, guestId);
        List<CartItemDTO> items = userFacade.getMemberCart(testUsername2);;
        assertEquals(1, items.size());
    }

    @Test
    public void validateCartDoesntMoveWithGuestWhenLoginIfNotEmpty() {
        userFacade.addProductToCart(testUsername2, testStoreId, testProductId1, 2);
        userFacade.addProductToCart(testUsername2, testStoreId, testProductId2, 3);
        userFacade.logout(testUsername2);
        int guestId = userFacade.enterAsGuest();
        userFacade.addProductToCart(guestId, testStoreId, 1, 2);
        authFacade.login(testUsername2, testPassword, guestId);
        List<CartItemDTO> items = userFacade.getMemberCart(testUsername2);;
        assertEquals(2, items.size());
    }

    @Test
    public void viewCart() throws Exception {
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId2, 3);

        UserOrderDTO res = userFacade.viewCart(testUsername1);
        assertEquals(2, res.getProductsData().size());
        assertEquals(54.0, res.getOldPrice(), 0);
        assertEquals(54.0, res.getNewPrice(), 0);
    }

    @Test
    public void viewCartWith2stores() throws Exception {
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.addProductToCart(testUsername1, testStoreId2, testProductId2, 3);

        UserOrderDTO res = userFacade.viewCart(testUsername1);
        assertEquals(2, res.getProductsData().size());
        assertEquals(54.0, res.getOldPrice(), 0);
        assertEquals(54.0, res.getNewPrice(), 0);
    }

    @Test
    public void purchaseCart() throws Exception {
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId2, 3);

        CreditCardDTO creditCardDTO = new CreditCardDTO(testUsername1, testUsername1, new Date(), testPassword);
        AddressDTO addressDTO = new AddressDTO(testPassword, testPassword, testPassword, testPassword, testUsername3,
                testUsername2, testUsername1, testPassword);

        assertDoesNotThrow(() -> userFacade.purchaseCart(testUsername1, creditCardDTO, addressDTO));
    }

    @Test
    public void purchaseCart_failCreditCard() throws Exception {
        turnOn_externalServices();
        this.paymentService.setController(new PaymentInterface() {
            @Override
            public boolean creditCardValid(CreditCardDTO creditDetails) {
                return false;
            }

            @Override
            public boolean pay(double amount, CreditCardDTO payerCard, BankAccountDTO receiverAccount) {
                return false;
            }
        });
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId2, 3);

        CreditCardDTO creditCardDTO = new CreditCardDTO(testUsername1, testUsername1, new Date(), testPassword);
        AddressDTO addressDTO = new AddressDTO(testPassword, testPassword, testPassword, testPassword, testUsername3,
                testUsername2, testUsername1, testPassword);

        try {
            userFacade.purchaseCart(testUsername1, creditCardDTO, addressDTO);
        } catch (Exception e) {
            String expected = Error.makePurchaseInvalidCardError();
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void purchaseCart_failPayment() throws Exception {
        turnOn_externalServices();
        this.paymentService.setController(new PaymentInterface() {
            @Override
            public boolean creditCardValid(CreditCardDTO creditDetails) {
                return true;
            }

            @Override
            public boolean pay(double amount, CreditCardDTO payerCard, BankAccountDTO receiverAccount) {
                return false;
            }
        });
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId2, 3);

        CreditCardDTO creditCardDTO = new CreditCardDTO(testUsername1, testUsername1, new Date(), testPassword);
        AddressDTO addressDTO = new AddressDTO(testPassword, testPassword, testPassword, testPassword, testUsername3,
                testUsername2, testUsername1, testPassword);

        try {
            userFacade.purchaseCart(testUsername1, creditCardDTO, addressDTO);
        } catch (Exception e) {
            String expected = Error.makePurchasePaymentCannotBeCompletedForStoreError(testStoreId);
            assertEquals(expected, e.getMessage());
        }
    }

    @Test
    public void purchaseCart_failMakeOrder() throws Exception {
        turnOn_externalServices();

        this.supplyService.setController(new SupplyInterface() {
            @Override
            public boolean canMakeOrder(OrderDetailsDTO orderDetails, AddressDTO address) {
                return false;
            }

            @Override
            public String makeOrder(OrderDetailsDTO orderDetails, AddressDTO address) {
                return "null";
            }

            @Override
            public boolean cancelOrder(String orderCode) {
                return true;
            }
        });
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId1, 2);
        userFacade.addProductToCart(testUsername1, testStoreId, testProductId2, 3);

        CreditCardDTO creditCardDTO = new CreditCardDTO(testUsername1, testUsername1, new Date(), testPassword);
        AddressDTO addressDTO = new AddressDTO(testPassword, testPassword, testPassword, testPassword, testUsername3,
                testUsername2, testUsername1, testPassword);

        try {
            userFacade.purchaseCart(testUsername1, creditCardDTO, addressDTO);
        } catch (Exception e) {
            String expected = Error.makePurchaseOrderCannotBeSuppliedError();
            assertEquals(expected, e.getMessage());
        }
    }

}
