package com.sadna.sadnamarket.acceptance;

import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.stores.StoreDTO;
import com.sadna.sadnamarket.service.Error;
import com.sadna.sadnamarket.service.MarketServiceTestAdapter;
import org.junit.jupiter.api.Assertions;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

@SpringBootTest
@ActiveProfiles("test")
class GuestTests {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MarketServiceTestAdapter bridge;

    @BeforeEach
    void clean() {
        bridge.clear();
    }

    @Test
    void guestEnterTest() {
        Response resp = bridge.guestEnterSystem();
        Assertions.assertNotEquals(resp, null);
        Assertions.assertFalse(resp.getError());
        Assertions.assertFalse(resp.getDataJson().isEmpty());
    }

    @Test
    void guestLeaveTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        resp = bridge.guestLeaveSystem(uuid);
        Assertions.assertNotEquals(resp, null);
        Assertions.assertFalse(resp.getError());
        resp = bridge.guestCartExists(uuid);
        Assertions.assertEquals(resp.getDataJson(), "false");
    }

    @Test
    void guestLeaveTwiceTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        bridge.guestLeaveSystem(uuid);
        resp = bridge.guestLeaveSystem(resp.getDataJson());
        Assertions.assertNotEquals(resp, null);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeMemberGuestDoesntExistError(Integer.parseInt(uuid)), resp.getErrorString());
    }

    @Test
    void guestSignUpTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String username = "JohnDoe";
        resp = bridge.signUp(uuid, "john@john.com", username, "thisIsn'tWhatAHashLooksLike");
        Assertions.assertNotEquals(resp, null);
        Assertions.assertFalse(resp.getError());
        String token = resp.getDataJson();
        resp = bridge.memberExists(username);
        Assertions.assertNotEquals(resp, null);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals(resp.getDataJson(), "true");
        resp = bridge.authenticate(token, username);
        Assertions.assertNotEquals(resp, null);
        Assertions.assertFalse(resp.getError());
        Assertions.assertEquals(resp.getDataJson(), "true");
    }

    @Test
    void guestSignupMemberAlreadyExistsTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String username = "JohnDoe";
        resp = bridge.signUp(uuid, "john@john.com", username, "thisIsn'tWhatAHashLooksLike");
        resp = bridge.signUp(uuid, "john@john.com", username, "thisIsn'tWhatAHashLooksLike");
        Assertions.assertNotEquals(resp, null);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeAuthUsernameExistsError(), resp.getErrorString());
        resp = bridge.signUp(uuid, "jane@john.com", username, "thisIsn'tWhatAHashLooksLikeEither");
        Assertions.assertNotEquals(resp, null);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeAuthUsernameExistsError(), resp.getErrorString());
    }

    @Test
    void guestLoginTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String username = "JohnDoe";
        resp = bridge.signUp(uuid, "john@john.com", username, "thisIsn'tWhatAHashLooksLike");
        bridge.logout(username);
        resp = bridge.login(username, "thisIsn'tWhatAHashLooksLike");
        Assertions.assertNotEquals(resp, null);
        Assertions.assertFalse(resp.getError());
        String token1 = resp.getDataJson();
        Assertions.assertNotEquals("", token1);
    }

    @Test
    void guestLoginAlreadyLoggedInTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String username = "JohnDoe";
        bridge.signUp(uuid, "john@john.com", username, "thisIsn'tWhatAHashLooksLike");
        resp = bridge.login(username, "thisIsn'tWhatAHashLooksLike");
        Assertions.assertNotEquals(resp, null);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeUserLoggedInError(), resp.getErrorString());
    }

    @Test
    void guestLoginWrongPasswordTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String username = "JohnDoe";
        resp = bridge.signUp(uuid, "john@john.com", username, "thisIsn'tWhatAHashLooksLike");
        bridge.logout(username);
        resp = bridge.login(username, "thisPasswordIsVeryWrong");
        Assertions.assertNotEquals(resp, null);
        Assertions.assertTrue(resp.getError());
        Assertions.assertEquals(Error.makeAuthPasswordIncorrectError(), resp.getErrorString());
    }

    @Test
    void getStoreDataTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String ownerUsername = "GuyStore";
        resp = bridge.signUp(uuid, "guywhoowns@store.com", ownerUsername, "password");
        String ownerToken = resp.getDataJson();
        resp = bridge.openStore(ownerToken, ownerUsername, "TestStore");
        int storeId = Integer.parseInt(resp.getDataJson());
        Assertions.assertDoesNotThrow(() -> bridge.getStoreData("", "", storeId));
        try {
            resp = bridge.getStoreData("", null, storeId);
            Assertions.assertFalse(resp.getError());
            String json = resp.getDataJson();
            Assertions.assertDoesNotThrow(() -> objectMapper.readValue(json, StoreDTO.class));
            StoreDTO storeDTO = objectMapper.readValue(json, StoreDTO.class);
            Assertions.assertEquals(storeId, storeDTO.getStoreId());
            Assertions.assertEquals("TestStore", storeDTO.getStoreName());
        } catch (Exception e) {

        }
    }

    @Test
    void getStoreDataDoesntExistTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String ownerUsername = "GuyStore";
        resp = bridge.signUp(uuid, "guywhoowns@store.com", ownerUsername, "password");
        String ownerToken = resp.getDataJson();
        resp = bridge.openStore(ownerToken, ownerUsername, "TestStore");
        try {
            resp = bridge.getStoreData("", null, Integer.MAX_VALUE);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void getClosedStoreDataNotOwnerTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String ownerUsername = "GuyStore";
        resp = bridge.signUp(uuid, "guywhoowns@store.com", ownerUsername, "password");
        String ownerToken = resp.getDataJson();
        resp = bridge.openStore(ownerToken, ownerUsername, "TestStore");
        int storeId = Integer.parseInt(resp.getDataJson());
        try {
            bridge.closeStore(ownerToken, ownerUsername, storeId);
            resp = bridge.getStoreData("", null, storeId);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreWithIdNotActiveError(storeId), resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void getClosedStoreDataOwnerTest() {
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        String ownerUsername = "GuyStore";
        resp = bridge.signUp(uuid, "guywhoowns@store.com", ownerUsername, "password");
        String ownerToken = resp.getDataJson();
        resp = bridge.openStore(ownerToken, ownerUsername, "TestStore");
        int storeId = Integer.parseInt(resp.getDataJson());
        Assertions.assertDoesNotThrow(() -> bridge.getStoreData(ownerToken, ownerUsername, storeId));
        try {
            bridge.closeStore(ownerToken, ownerUsername, storeId);
            resp = bridge.getStoreData("", null, storeId);
            Assertions.assertTrue(resp.getError());
            resp = bridge.getStoreData(ownerToken, ownerUsername, storeId);
            Assertions.assertFalse(resp.getError());
            String json1 = resp.getDataJson();
            Assertions.assertDoesNotThrow(() -> objectMapper.readValue(json1, StoreDTO.class));
            StoreDTO storeDTO = objectMapper.readValue(json1, StoreDTO.class);
            Assertions.assertEquals(storeId, storeDTO.getStoreId());
            Assertions.assertEquals("TestStore", storeDTO.getStoreName());
        } catch (Exception e) {

        }
    }
}
