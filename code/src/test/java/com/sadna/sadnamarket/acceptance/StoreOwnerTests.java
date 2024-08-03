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
import com.sadna.sadnamarket.domain.users.MemberDTO;
import com.sadna.sadnamarket.domain.payment.BankAccountDTO;
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
import java.util.LinkedList;
import java.util.List;

@SpringBootTest
@ActiveProfiles("test")
class StoreOwnerTests {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MarketServiceTestAdapter bridge;

    String username;
    String token;
    int storeId;
    String maliciousUsername;
    String maliciousToken;

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
        bridge.setStoreBankAccount(token, username, storeId, new BankAccountDTO("10", "392", "393013", "2131516175"));
        resp = bridge.guestEnterSystem();
        uuid = resp.getDataJson();
        maliciousUsername = "Mallory";
        resp = bridge.signUp(uuid, "mal@mal.com", maliciousUsername, "stolenPasswordBecauseImEvil");
        maliciousToken = resp.getDataJson();
    }

    @Test
    void addProductTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        Assertions.assertFalse(resp.getError());
        String productIdString = resp.getDataJson();
        Assertions.assertDoesNotThrow(() -> Integer.parseInt(productIdString));
        int productId = Integer.parseInt(productIdString);
        try {
            resp = bridge.getProductData(token, username, productId);
            Assertions.assertFalse(resp.getError());
        } catch (Exception e) {

        }
    }

    @Test
    void addProductBadInfoTest() {
        Response resp = bridge.addProductToStore(token, username, storeId, new ProductDTO(-1, "", -100.0, "cat", -10, 8,true,storeId));
        Assertions.assertTrue(resp.getError());
        Assertions.assertTrue(resp.getErrorString().contains(Error.makeProductAspectCannotBeNullOrEmptyError("name")));
        Assertions.assertTrue(resp.getErrorString().contains(Error.makeProductAspectCannotBeNullOrEmptyError("name")));
    }

    @Test
    void addProductNoPermissionTest() {
        Response resp = bridge.addProductToStore(maliciousToken, maliciousUsername, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserCannotAddProductError(maliciousUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserCannotAddProductError(maliciousUsername, storeId), resp.getErrorString());
    }

    @Test
    void editProductTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        resp = bridge.editStoreProduct(token, username, storeId, productId, new ProductDTO(-1, "product", 200.0, "cat", 3.5, 4,true,storeId));
        Assertions.assertFalse(resp.getError());
        try {
            resp = bridge.getProductData(token, username, productId);
            Assertions.assertFalse(resp.getError());
            ProductDTO productDTO = objectMapper.readValue(resp.getDataJson(), ProductDTO.class);
            Assertions.assertEquals(200.0, productDTO.getProductPrice());
            resp = bridge.setStoreProductAmount(token, username, storeId, productId, 1);
            Assertions.assertFalse(resp.getError());
            resp = bridge.getStoreProductAmount(storeId, productId);
            Assertions.assertEquals("1", resp.getDataJson());
        } catch (Exception e) {
        }
    }

    @Test
    void editProductBadInfoTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        resp = bridge.editStoreProduct(token, username, storeId, productId,
                new ProductDTO(-1, null, -200.0, null, -10, 3,true,storeId));
        Assertions.assertTrue(resp.getError());
        Assertions.assertTrue(resp.getErrorString().contains(Error.makeProductAspectCannotBeNullOrEmptyError("name")));
        Assertions.assertTrue(resp.getErrorString().contains(Error.makeProductAspectCannotBeNullOrEmptyError("name")));
    }

    @Test
    void editProductDoesntExistTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        resp = bridge.editStoreProduct(token, username, storeId, Integer.MAX_VALUE,
                new ProductDTO(-1, null, -200.0, null, -10, 3,true,storeId));
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, Integer.MAX_VALUE), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, Integer.MAX_VALUE), resp.getErrorString());
    }

    @Test
    void editProductWrongStoreTest() {
        Response resp = bridge.openStore(token, username, "New Store");
        int newStoreId = Integer.parseInt(resp.getDataJson());
        resp = bridge.addProductToStore(token, username, newStoreId, new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        resp = bridge.editStoreProduct(token, username, storeId, productId, new ProductDTO(-1, null, 200.0, null, -10, 4,true,storeId));
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, productId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, productId), resp.getErrorString());
    }

    @Test
    void editProductNoPermissionTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        resp = bridge.editStoreProduct(maliciousToken, maliciousUsername, storeId, productId,
                new ProductDTO(-1, null, 200.0, null, -10, 5,true,storeId));
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserCannotUpdateProductError(maliciousUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserCannotUpdateProductError(maliciousUsername, storeId), resp.getErrorString());
    }

    @Test
    void removeProductTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        resp = bridge.removeProductFromStore(token, username, storeId, productId);
        Assertions.assertFalse(resp.getError());
        try {
            resp = bridge.getProductData(token, username, productId);
            Assertions.assertTrue(resp.getError());
        } catch (Exception e) {
        }
    }

    @Test
    void removeProductDoesntExistTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        resp = bridge.removeProductFromStore(token, username, storeId, Integer.MAX_VALUE);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, Integer.MAX_VALUE), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, Integer.MAX_VALUE), resp.getErrorString());
    }

    @Test
    void removeProductWrongStoreTest() {
        Response resp = bridge.openStore(token, username, "New Store");
        int newStoreId = Integer.parseInt(resp.getDataJson());
        resp = bridge.addProductToStore(token, username, newStoreId, new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        resp = bridge.removeProductFromStore(token, username, storeId, productId);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, productId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreProductDoesntExistError(storeId, productId), resp.getErrorString());
    }

    @Test
    void removeProductNoPermissionTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        resp = bridge.removeProductFromStore(maliciousToken, maliciousUsername, storeId, productId);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserCannotDeleteProductError(maliciousUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserCannotDeleteProductError(maliciousUsername, storeId), resp.getErrorString());
    }

    @Test
    void appointOwnerTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.logout(appointeeUsername);

        resp = bridge.appointOwner(token, username, storeId, appointeeUsername);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("true", resp.getDataJson());

        resp = bridge.login(appointeeUsername, "password");
        apointeeToken = resp.getDataJson();
        resp = bridge.acceptOwnerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));
        Assertions.assertFalse(resp.getError());

        resp = bridge.getIsOwner(token, username, storeId, appointeeUsername);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("true", resp.getDataJson());
    }

    @Test
    void appointOwnerRejectTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.logout(appointeeUsername);

        resp = bridge.appointOwner(token, username, storeId, appointeeUsername);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("true", resp.getDataJson());

        resp = bridge.login(appointeeUsername, "password");
        apointeeToken = resp.getDataJson();
        resp = bridge.rejectOwnerAppointment(apointeeToken, appointeeUsername, bridge.getFirstNotification(appointeeUsername), username);
        Assertions.assertFalse(resp.getError());

        resp = bridge.getIsOwner(token, username, storeId, appointeeUsername);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("false", resp.getDataJson());
    }

    @Test
    void appointOwnerAlreadyOwnerTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointOwner(token, username, storeId, appointeeUsername);
        bridge.acceptOwnerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));

        resp = bridge.appointOwner(token, username, storeId, appointeeUsername);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserAlreadyOwnerError(appointeeUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserAlreadyOwnerError(appointeeUsername, storeId), resp.getErrorString());
    }

    @Test
    void appointOwnerDoesntExistTest() {
        Response resp = bridge.appointOwner(token, username, storeId, "Eric");
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeMemberUserDoesntExistError("Eric"), resp.getErrorString());
    }

    @Test
    void appointOwnerNoPermissionTest() {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        resp = bridge.appointOwner(maliciousToken, maliciousUsername, storeId, appointeeUsername);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserCannotAddOwnerError(maliciousUsername, appointeeUsername, storeId), resp.getErrorString());
    }

    @Test
    void appointOwnerOriginalOwnerDoesntExistTest() {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        resp = bridge.appointOwner("token that isn't real", "username that nobody has", storeId, appointeeUsername);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeAuthInvalidJWTError(), resp.getErrorString());
    }

    @Test
    void appointManagerTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.logout(appointeeUsername);

        resp = bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("true", resp.getDataJson());

        resp = bridge.login(appointeeUsername, "password");
        apointeeToken = resp.getDataJson();
        resp = bridge.acceptManagerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));
        Assertions.assertFalse(resp.getError());

        resp = bridge.getIsManager(token, username, storeId, appointeeUsername);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("true", resp.getDataJson());
    }

    @Test
    void appointManagerRejectTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.logout(appointeeUsername);

        resp = bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("true", resp.getDataJson());

        resp = bridge.login(appointeeUsername, "password");
        apointeeToken = resp.getDataJson();
        resp = bridge.rejectManagerAppointment(apointeeToken, appointeeUsername, bridge.getFirstNotification(appointeeUsername), username);
        Assertions.assertFalse(resp.getError());

        resp = bridge.getIsManager(token, username, storeId, appointeeUsername);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals("false", resp.getDataJson());
    }

    @Test
    void appointManagerAlreadyManagerTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        bridge.acceptManagerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));

        resp = bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserAlreadyManagerError(appointeeUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserAlreadyManagerError(appointeeUsername, storeId), resp.getErrorString());
    }

    @Test
    void appointManagerAlreadyOwnerTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointOwner(token, username, storeId, appointeeUsername);
        bridge.acceptOwnerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));

        resp = bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserAlreadyOwnerError(appointeeUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserAlreadyOwnerError(appointeeUsername, storeId), resp.getErrorString());
    }

    @Test
    void appointManagerDoesntExistTest() {
        Response resp = bridge.appointManager(token, username, storeId, "Eric", new LinkedList<>());
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeMemberUserDoesntExistError("Eric"), resp.getErrorString());
        Assertions.assertEquals(Error.makeMemberUserDoesntExistError("Eric"), resp.getErrorString());
    }

    @Test
    void appointManagerNoPermissionTest() {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        resp = bridge.appointManager(maliciousToken, maliciousUsername, storeId, appointeeUsername, new LinkedList<>());
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserCannotAddManagerError(maliciousUsername, appointeeUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserCannotAddManagerError(maliciousUsername, appointeeUsername, storeId), resp.getErrorString());
    }

    @Test
    void appointManagerOwnerDoesntExistTest() {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        resp = bridge.appointManager("token that isn't real", "username that nobody has", storeId, appointeeUsername,
                new LinkedList<>());
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeAuthInvalidJWTError(), resp.getErrorString());
        Assertions.assertEquals(Error.makeAuthInvalidJWTError(), resp.getErrorString());
    }

    @Test
    void changeManagerPermissionsTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        bridge.acceptManagerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));
        List<Integer> newPerms = new LinkedList<>();
        newPerms.add(1);
        resp = bridge.changeManagerPermissions(token, username, appointeeUsername, storeId, newPerms);
        Assertions.assertFalse(resp.getError());
        try {
            resp = bridge.getManagerPermissions(token, username, storeId, appointeeUsername);
            List<Integer> perms = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<Integer>>() {
            });
            Assertions.assertEquals(1, perms.size());
            Assertions.assertEquals(1, perms.get(0));
        } catch (Exception e) {

        }
    }

    @Test
    void changeManagerPermissionsNotOwnerTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        bridge.acceptManagerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));
        List<Integer> newPerms = new LinkedList<>();
        newPerms.add(1);
        resp = bridge.changeManagerPermissions(maliciousToken, maliciousUsername, appointeeUsername, storeId, newPerms);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserCannotAddManagerPermissionsError(maliciousUsername, appointeeUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserCannotAddManagerPermissionsError(maliciousUsername, appointeeUsername, storeId), resp.getErrorString());
    }

    @Test
    void changeManagerPermissionsNotManagerTest() {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        List<Integer> newPerms = new LinkedList<>();
        newPerms.add(1);
        resp = bridge.changeManagerPermissions(token, username, appointeeUsername, storeId, newPerms);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeMemberUserHasNoRoleError(), resp.getErrorString());
        Assertions.assertEquals(Error.makeMemberUserHasNoRoleError(), resp.getErrorString());
    }

    @Test
    void closeStoreTest() throws JsonProcessingException {
        Response resp = bridge.closeStore(token, username, storeId);
        Assertions.assertFalse(resp.getError());

        resp = bridge.getNotifications(username);
        List<NotificationDTO> notifs = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<NotificationDTO>>() {
        });
        Assertions.assertTrue(notifs.size() >= 1);
        NotificationDTO notif = notifs.get(0);
        Assertions.assertEquals(String.format("The store \"%s\" was closed.", "Store's Store"), notif.getMessage());
    }

    @Test
    void closeStoreDoesntExistTest() {
        Response resp = bridge.closeStore(token, username, Integer.MAX_VALUE);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), resp.getErrorString());
    }

    @Test
    void closeStoreNotOwnerTest() {
        Response resp = bridge.closeStore(maliciousToken, maliciousUsername, storeId);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeStoreUserCannotCloseStoreError(maliciousUsername, storeId), resp.getErrorString());
        Assertions.assertEquals(Error.makeStoreUserCannotCloseStoreError(maliciousUsername, storeId), resp.getErrorString());
    }

    @Test
    void getStoreRoleTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        bridge.acceptManagerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));

        try {
            resp = bridge.getStoreOwners(token, username, storeId);
            List<MemberDTO> owners = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<MemberDTO>>() {
            });
            Assertions.assertEquals(1, owners.size());
            Assertions.assertEquals(username, owners.get(0).getUsername());

            resp = bridge.getStoreManagers(token, username, storeId);
            List<MemberDTO> managers = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<MemberDTO>>() {
            });
            Assertions.assertEquals(1, managers.size());
            Assertions.assertEquals(appointeeUsername, managers.get(0).getUsername());
        } catch (Exception e) {

        }
    }

    @Test
    void getStoreRoleDoesntExistTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        bridge.acceptManagerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));

        try {
            resp = bridge.getStoreOwners(token, username, Integer.MAX_VALUE);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE),resp.getErrorString());
            Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE),resp.getErrorString());

            resp = bridge.getStoreManagers(token, username, Integer.MAX_VALUE);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE),resp.getErrorString());
            Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE),resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void getStoreRoleNotOwnerTest() throws JsonProcessingException {
        String appointeeUsername = "Eric";
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.signUp(uuid, "eric@excited.com", appointeeUsername, "password");
        String apointeeToken = resp.getDataJson();
        bridge.appointManager(token, username, storeId, appointeeUsername, new LinkedList<Integer>());
        bridge.acceptManagerAppointment(apointeeToken, appointeeUsername, storeId, bridge.getFirstNotification(appointeeUsername));

        try {
            resp = bridge.getStoreOwners(maliciousToken, maliciousUsername, storeId);
            resp = bridge.getStoreOwners(maliciousToken, maliciousUsername, storeId);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreUserCannotGetRolesInfoError(maliciousUsername, storeId),resp.getErrorString());
            Assertions.assertEquals(Error.makeStoreUserCannotGetRolesInfoError(maliciousUsername, storeId),resp.getErrorString());

            resp = bridge.getStoreManagers(maliciousToken, maliciousUsername, storeId);
            resp = bridge.getStoreManagers(maliciousToken, maliciousUsername, storeId);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreUserCannotGetRolesInfoError(maliciousUsername, storeId),resp.getErrorString());
            Assertions.assertEquals(Error.makeStoreUserCannotGetRolesInfoError(maliciousUsername, storeId),resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void seeStoreOrderHistoryTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        bridge.setStoreProductAmount(token, username, storeId, productId, 10);
        resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.addProductToBasketGuest(uuid, storeId, productId, 5);
        bridge.buyCartGuest(uuid, new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782"),
                new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                        "+97254-989-4939", "jimjimmy@gmail.com"));
        try {
            resp = bridge.getStorePurchaseHistory(token, username, storeId);
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(1, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void seeStoreOrderHistoryNeverPurchaseTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        bridge.setStoreProductAmount(token, username, storeId, productId, 10);
        try {
            resp = bridge.getStorePurchaseHistory(token, username, storeId);
            List<OrderDTO> history = objectMapper.readValue(resp.getDataJson(), new TypeReference<List<OrderDTO>>() {
            });
            Assertions.assertEquals(0, history.size());
        } catch (Exception e) {

        }
    }

    @Test
    void seeStoreOrderHistoryNoOwnerTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        bridge.setStoreProductAmount(token, username, storeId, productId, 10);
        resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.addProductToBasketGuest(uuid, storeId, productId, 5);
        bridge.buyCartGuest(uuid, new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782"),
                new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                        "+97254-989-4939", "jimjimmy@gmail.com"));
        try {
            resp = bridge.getStorePurchaseHistory(maliciousToken, maliciousUsername, storeId);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreUserCannotStoreHistoryError(maliciousUsername, storeId), resp.getErrorString());
            Assertions.assertEquals(Error.makeStoreUserCannotStoreHistoryError(maliciousUsername, storeId), resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void seeStoreOrderHistoryDoesntExistTest() {
        Response resp = bridge.addProductToStore(token, username, storeId,
                new ProductDTO(-1, "product", 100.0, "cat", 3.5, 2,true,storeId));
        int productId = Integer.parseInt(resp.getDataJson());
        bridge.setStoreProductAmount(token, username, storeId, productId, 10);
        resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.addProductToBasketGuest(uuid, storeId, productId, 5);
        bridge.buyCartGuest(uuid, new CreditCardDTO("4722310696661323", "103", new Date(1830297600), "123456782"),
                new AddressDTO("Israel", "Yerukham", "Benyamin 12", "Apartment 12", "8053624", "Jim Jimmy",
                        "+97254-989-4939", "jimjimmy@gmail.com"));
        try {
            resp = bridge.getStorePurchaseHistory("nopety nope nope", "doesnt exist", storeId);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeAuthInvalidJWTError(), resp.getErrorString());
            Assertions.assertEquals(Error.makeAuthInvalidJWTError(), resp.getErrorString());
        } catch (Exception e) {

        }
    }
}
