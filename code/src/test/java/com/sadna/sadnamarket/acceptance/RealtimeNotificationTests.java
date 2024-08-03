package com.sadna.sadnamarket.acceptance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.FaketimeService;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.payment.PaymentInterface;
import com.sadna.sadnamarket.domain.payment.PaymentService;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.supply.SupplyInterface;
import com.sadna.sadnamarket.domain.supply.SupplyService;
import com.sadna.sadnamarket.service.MarketServiceTestAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.LinkedList;

@SpringBootTest
@ActiveProfiles("test")
class RealtimeNotificationTests {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MarketServiceTestAdapter bridge;

    String username;
    String token;
    int storeId;
    int productId;
    FaketimeService fake;


    @BeforeEach
    void clean() throws JsonProcessingException {
        bridge.clear();
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);
        username = "StoreOwnerMan";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "storeowner@store.com", username, "imaginaryPassowrd");
        token = resp.getDataJson();
        resp = bridge.openStore(token, username, "Store's Store");
        storeId = Integer.parseInt(resp.getDataJson());
        resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "TestProduct", 100.3, "Product", 3.5, 2,true,storeId));
        productId = Integer.parseInt(resp.getDataJson());
        bridge.setStoreBankAccount(token, username, storeId, new BankAccountDTO("10", "392", "393013", "2131516175"));
        fake = new FaketimeService();
        bridge.injectRealtime(fake);
    }

    @Test
    void buyCartTest() {
        String uuid = bridge.guestEnterSystem().getDataJson();

        bridge.setStoreProductAmount(token, username, storeId, productId, 2);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertEquals("User made a purchase in your store Store's Store",fake.getMessages(username).get(0));
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartMemberTest() {
        String uuid = bridge.guestEnterSystem().getDataJson();

        bridge.setStoreProductAmount(token, username, storeId, productId, 2);
        try {
            bridge.addProductToBasketMember(token,username, storeId, productId, 1);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            Response resp = bridge.buyCartMember(token,username, cardDTO,addressDTO);
            Assertions.assertEquals("User StoreOwnerMan made a purchase in your store Store's Store",fake.getMessages(username).get(0));
        } catch (Exception e) {

        }
    }

    @Test
    void buyCartLoggedOutTest() {
        String uuid = bridge.guestEnterSystem().getDataJson();

        bridge.setStoreProductAmount(token, username, storeId, productId, 2);
        try {
            bridge.addProductToBasketGuest(uuid, storeId, productId, 1);
            CreditCardDTO cardDTO = new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782");
            AddressDTO addressDTO = new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                    "+97254-989-4939", "jimjimmy@gmail.com");
            bridge.logout(username);
            Response resp = bridge.buyCartGuest(uuid, cardDTO,addressDTO);
            Assertions.assertEquals(null,fake.getMessages(username));
        } catch (Exception e) {

        }
    }

    @Test
    void sendOwnerRequestAcceptTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();

        resp = bridge.appointOwner(token, username, storeId, appointeeUsername);
        Assertions.assertTrue(fake.getMessages(appointeeUsername).get(0).startsWith("You got a request from "));
        resp = bridge.acceptOwnerAppointment(apointeeToken, appointeeUsername, 1, bridge.getFirstNotification(appointeeUsername));
        Assertions.assertEquals("User " + appointeeUsername + " accepted request for Owner in " + storeId,fake.getMessages(username).get(0));
    }

    @Test
    void sendOwnerRequestRejectTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();

        resp = bridge.appointOwner(token, username, storeId, appointeeUsername);
        Assertions.assertTrue(fake.getMessages(appointeeUsername).get(0).startsWith("You got a request from "));
        resp = bridge.rejectOwnerAppointment(apointeeToken, appointeeUsername, bridge.getFirstNotification(appointeeUsername),"");
        Assertions.assertEquals("User " + appointeeUsername + " rejected request for Owner in " + storeId,fake.getMessages(username).get(0));
    }

    @Test
    void sendManagerRequestAcceptTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();

        resp = bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<>());
        Assertions.assertTrue(fake.getMessages(appointeeUsername).get(0).startsWith("You got a request from "));
        resp = bridge.acceptOwnerAppointment(apointeeToken, appointeeUsername, 1, bridge.getFirstNotification(appointeeUsername));
        Assertions.assertEquals("User " + appointeeUsername + " accepted request for Manager in " + storeId,fake.getMessages(username).get(0));
    }

    @Test
    void sendManagerRequestRejectTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();

        resp = bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<>());
        Assertions.assertTrue(fake.getMessages(appointeeUsername).get(0).startsWith("You got a request from "));
        resp = bridge.rejectOwnerAppointment(apointeeToken, appointeeUsername, bridge.getFirstNotification(appointeeUsername),"");
        Assertions.assertEquals("User " + appointeeUsername + " rejected request for Manager in " + storeId,fake.getMessages(username).get(0));
    }

    @Test
    void closeStoreTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();

        resp = bridge.appointOwner(token, username, storeId, appointeeUsername);
        resp = bridge.acceptOwnerAppointment(apointeeToken, appointeeUsername, 1, bridge.getFirstNotification(appointeeUsername));
        bridge.closeStore(token,username,storeId);
        Assertions.assertEquals(String.format("The store \"%s\" was closed.", "Store's Store"),fake.getMessages(appointeeUsername).get(1));
    }

    @Test
    void closeStoreLoggedOutTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();

        resp = bridge.appointOwner(token, username, storeId, appointeeUsername);
        resp = bridge.acceptOwnerAppointment(apointeeToken, appointeeUsername, 1, bridge.getFirstNotification(appointeeUsername));
        bridge.logout(appointeeUsername);
        bridge.closeStore(token,username,storeId);
        Assertions.assertTrue(fake.getMessages(appointeeUsername).size() == 1);
    }
}