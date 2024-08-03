package com.sadna.sadnamarket.acceptance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.orders.OrderDTO;
import com.sadna.sadnamarket.domain.payment.*;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.supply.SupplyInterface;
import com.sadna.sadnamarket.domain.supply.SupplyProxy;
import com.sadna.sadnamarket.domain.supply.SupplyService;
import com.sadna.sadnamarket.domain.users.NotificationDTO;
import com.sadna.sadnamarket.service.Error;
import com.sadna.sadnamarket.service.MarketServiceTestAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
public class CartTests {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MarketServiceTestAdapter bridge;

    String uuid;
    int storeId;
    int productId;
    String ownerToken;
    String ownerUsername;

    @BeforeEach
    void clean() {
        PaymentService.getInstance().setController(new PaymentProxy());
        SupplyService.getInstance().setController(new SupplyProxy());
        bridge.clear();
        Response resp = bridge.guestEnterSystem();
        uuid = resp.getDataJson();
        ownerUsername = "GuyStore";
        resp = bridge.signUp(uuid, "guywhoowns@store.com", ownerUsername, "password");
        ownerToken = resp.getDataJson();
        resp = bridge.openStore(ownerToken, ownerUsername, "TestStore");
        storeId = Integer.parseInt(resp.getDataJson());
        resp = bridge.addProductToStore(ownerToken, ownerUsername, storeId,
                new ProductDTO(-1, "TestProduct", 100.3, "Product", 3.5, 2,true,storeId));
        productId = Integer.parseInt(resp.getDataJson());
        BankAccountDTO bankAccountDTO = new BankAccountDTO("10", "392", "393013", "2131516175");
        bridge.setStoreBankAccount(ownerToken, ownerUsername, storeId, bankAccountDTO);
    }

    @Test
    void addToBasketTest() {
        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 2);
        try {
            Response resp = bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            Assertions.assertFalse(resp.getError());
            Assertions.assertEquals("true", resp.getDataJson());
            resp = bridge.getGuestBasket(uuid, storeId);
            String json = resp.getDataJson();
            Assertions
                    .assertDoesNotThrow(() -> objectMapper.readValue(json, new TypeReference<Map<Integer, Integer>>() {
                    }));
            Map<Integer, Integer> basket = objectMapper.readValue(json, new TypeReference<Map<Integer, Integer>>() {
            });
            Assertions.assertTrue(basket.containsKey(productId));
            Assertions.assertEquals(1, basket.get(productId));
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            resp = bridge.getGuestBasket(uuid, storeId);
            String json1 = resp.getDataJson();
            Assertions
                    .assertDoesNotThrow(() -> objectMapper.readValue(json1, new TypeReference<Map<Integer, Integer>>() {
                    }));
            basket = objectMapper.readValue(json1, new TypeReference<Map<Integer, Integer>>() {
            });
            Assertions.assertEquals(2, basket.get(productId));
        } catch (Exception e) {

        }
    }

    @Test
    void addToBasketNotEnoughAmountTest() {
        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 2);
        try {
            Response resp = bridge.addProductToBasketGuest(uuid, storeId, productId, 3);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeCartAmountDoesntExistError(), resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void addToBasketProductDoesntExistTest() {
        try {
            Response resp = bridge.addProductToBasketGuest(uuid, storeId, Integer.MAX_VALUE, 3);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeCartAmountDoesntExistError(), resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void editBasketTest() {
        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 2);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            Response resp = bridge.setGuestBasketProductAmount(uuid, storeId, productId, 2);
            Assertions.assertFalse(resp.getError());
            Assertions.assertEquals("true", resp.getDataJson());
            resp = bridge.getGuestBasket(uuid, storeId);
            String json = resp.getDataJson();
            Map<Integer, Integer> basket = objectMapper.readValue(json, new TypeReference<Map<Integer, Integer>>() {
            });
            Assertions.assertTrue(basket.containsKey(productId));
            Assertions.assertEquals(2, basket.get(productId));
        } catch (Exception e) {

        }
    }

    @Test
    void editBasketIllegalTest() {
        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 2);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            Response resp = bridge.setGuestBasketProductAmount(uuid, storeId, productId, 3);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeCartAmountDoesntExistError(), resp.getErrorString());
            resp = bridge.getGuestBasket(uuid, storeId);
            String json = resp.getDataJson();
            Map<Integer, Integer> basket = objectMapper.readValue(json, new TypeReference<Map<Integer, Integer>>() {
            });
            Assertions.assertTrue(basket.containsKey(productId));
            Assertions.assertEquals(1, basket.get(productId));
        } catch (Exception e) {

        }
    }

    @Test
    void editBasketProductRemovedTest() {
        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 2);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            bridge.removeProductFromStore(ownerToken, ownerUsername, storeId, productId);
            Response resp = bridge.setGuestBasketProductAmount(uuid, storeId, productId, 2);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeCartAmountDoesntExistError(), resp.getErrorString());
            resp = bridge.getGuestBasket(uuid, storeId);
            String json = resp.getDataJson();
            Map<Integer, Integer> basket = objectMapper.readValue(json, new TypeReference<Map<Integer, Integer>>() {
            });
            Assertions.assertTrue(basket.containsKey(productId));
            Assertions.assertEquals(1, basket.get(productId));
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartTest() throws JsonProcessingException {
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);


        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 2);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertFalse(resp.getError());
            Assertions.assertEquals(bridge.getStoreProductAmount(storeId, productId).getDataJson(), "1");
            resp = bridge.getNotifications(ownerUsername);
            List<NotificationDTO> notifs = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<NotificationDTO>>() {
            });
            Assertions.assertTrue(notifs.size() >= 1);
            NotificationDTO notif = notifs.get(0);
            Assertions.assertEquals("User made a purchase in your store TestStore", notif.getMessage());
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartIncorrectDetailsTest() throws JsonProcessingException {
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);

        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 2);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            CreditCardDTO cardDTO = new CreditCardDTO("", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makePurchaseMissingCardError(), resp.getErrorString());
            Assertions.assertEquals(bridge.getStoreProductAmount(storeId, productId).getDataJson(), "2");
            resp = bridge.getUserPurchaseHistory("", "", uuid);
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(0, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartStorePolicyForbidTest() throws JsonProcessingException {
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);


        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 5);
        bridge.addPolicyAgainst(ownerToken, ownerUsername, storeId, productId);

        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 4);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeAmountBuyPolicyError(""+productId,0,0), resp.getErrorString());
            Assertions.assertEquals(bridge.getStoreProductAmount(storeId, productId).getDataJson(), "4");
            resp = bridge.getUserPurchaseHistory("", "", uuid);
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(0, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartStoreNotEnoughProductTest() throws JsonProcessingException {
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);


        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 5);

        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 5);
            bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 4);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeNotEnoughInStcokError(storeId, productId, 5, 4), resp.getErrorString());
            Assertions.assertEquals(bridge.getStoreProductAmount(storeId, productId).getDataJson(), "4");
            resp = bridge.getUserPurchaseHistory("", "", uuid);
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(0, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartStoreCannotSupplyTest() throws JsonProcessingException {
        SupplyInterface supplyMock = Mockito.mock(SupplyInterface.class);
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        SupplyService.getInstance().setController(supplyMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);
        Mockito.when(supplyMock.canMakeOrder(Mockito.any(), Mockito.any())).thenReturn(false);

        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 5);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 2);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makePurchaseOrderCannotBeSuppliedError(), resp.getErrorString());
            Assertions.assertEquals(bridge.getStoreProductAmount(storeId, productId).getDataJson(), "5");
            resp = bridge.getUserPurchaseHistory("", "", uuid);
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(0, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartStoreCannotPayTest() throws JsonProcessingException {
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(false);

        bridge.setStoreProductAmount(ownerToken, ownerUsername, storeId, productId, 5);

        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 2);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makePurchasePaymentCannotBeCompletedForStoreError(storeId), resp.getErrorString());
            Assertions.assertEquals(bridge.getStoreProductAmount(storeId, productId).getDataJson(), "5");
            resp = bridge.getUserPurchaseHistory("", "", uuid);
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(0, history.size());
        } catch (Exception e) {

        }
    }


}
