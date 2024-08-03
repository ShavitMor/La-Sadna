package com.sadna.sadnamarket.acceptance;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.orders.OrderDTO;
import com.sadna.sadnamarket.domain.payment.CreditCardDTO;
import com.sadna.sadnamarket.domain.payment.PaymentInterface;
import com.sadna.sadnamarket.domain.payment.PaymentService;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.supply.AddressDTO;
import com.sadna.sadnamarket.domain.supply.SupplyInterface;
import com.sadna.sadnamarket.domain.supply.SupplyService;
import com.sadna.sadnamarket.service.Error;
import com.sadna.sadnamarket.service.MarketServiceTestAdapter;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
import org.junit.jupiter.api.*;
import org.mockito.Mockito;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.Date;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
class SystemManagerTests {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MarketServiceTestAdapter bridge;

    String buyerUsername;
    String username;
    String token;
    int storeId;
    String maliciousUsername;
    String maliciousToken;

    @BeforeAll
    void clean() throws JsonProcessingException {
        bridge.clear();
        PaymentInterface paymentMock = Mockito.mock(PaymentInterface.class);
        PaymentService.getInstance().setController(paymentMock);
        Mockito.when(paymentMock.creditCardValid(Mockito.any())).thenReturn(true);
        Mockito.when(paymentMock.pay(Mockito.anyDouble(), Mockito.any(), Mockito.any())).thenReturn(true);
        String storeOwnerUsername = "StoreOwnerMan";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "storeowner@store.com", storeOwnerUsername, "imaginaryPassowrd");
        String storeOwnerToken = resp.getDataJson();
        resp = bridge.openStore(storeOwnerToken, storeOwnerUsername, "Store's Store");
        storeId = Integer.parseInt(resp.getDataJson());
        bridge.setStoreBankAccount(storeOwnerToken, storeOwnerUsername, storeId,
                new BankAccountDTO("10", "392", "393013", "2131516175"));
        resp = bridge.guestEnterSystem();
        uuid = resp.getDataJson();
        buyerUsername = "Billy";
        resp = bridge.signUp(uuid, "bill@buyer.com", buyerUsername, "imaginaryPassowrd");
        String buyerToken = resp.getDataJson();

        resp = bridge.addProductToStore(storeOwnerToken, storeOwnerUsername, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 5,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        bridge.setStoreProductAmount(storeOwnerToken, storeOwnerUsername, storeId, productId, 10);
        bridge.addProductToBasketMember(buyerToken, buyerUsername, storeId, productId, 5);
        bridge.memberSetAddress(buyerToken, buyerUsername,
                new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                        "+97254-989-4939", "jimjimmy@gmail.com"));
        bridge.buyCartMember(buyerToken, buyerUsername,
                new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782"),
                new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                        "+97254-989-4939", "jimjimmy@gmail.com"));

        resp = bridge.guestEnterSystem();
        uuid = resp.getDataJson();
        maliciousUsername = "Mallory";
        resp = bridge.signUp(uuid, "mal@mal.com", maliciousUsername, "stolenPasswordBecauseImEvil");
        maliciousToken = resp.getDataJson();

        resp = bridge.guestEnterSystem();
        uuid = resp.getDataJson();
        username = "IAmAboutToBeGivenGreatPowers";
        resp = bridge.signUp(uuid, "paul@sadna.com", username, "noOneCanSeeMyPassword");
        token = resp.getDataJson();

        bridge.makeSystemManager(username);
    }

    @Test
    void seeStoreOrderHistoryTest() {
        try {
            Response resp = bridge.getStorePurchaseHistory(token, username, storeId);
            Assertions.assertFalse(resp.getError());
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(1, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void seeStoreOrderHistoryDoesntExistTest() {
        try {
            Response resp = bridge.getStorePurchaseHistory(token, username, Integer.MAX_VALUE);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void seeUserOrderHistoryTest() {
        try {
            Response resp = bridge.getUserPurchaseHistory(token, username, buyerUsername);
            Assertions.assertFalse(resp.getError());
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(1, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void seeUserOrderHistoryDoesntExistTest() {
        try {
            Response resp = bridge.getUserPurchaseHistory(token, username, "Username that nobody has");
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeMemberUserDoesntExistError("Username that nobody has"), resp.getErrorString());
        } catch (Exception e) {

        }
    }
}