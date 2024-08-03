package com.sadna.sadnamarket.acceptance;

import com.fasterxml.jackson.core.type.TypeReference;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.api.Response;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.service.Error;
import com.sadna.sadnamarket.service.MarketServiceTestAdapter;
import org.junit.jupiter.api.*;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.test.context.SpringBootTest;
import org.springframework.test.context.ActiveProfiles;

import java.util.List;
import java.util.Map;

@SpringBootTest
@ActiveProfiles("test")
@TestInstance(TestInstance.Lifecycle.PER_CLASS)
public class ProductInfoTests {
    ObjectMapper objectMapper = new ObjectMapper();

    @Autowired
    MarketServiceTestAdapter bridge;

    int productId;
    String ownerToken;
    String ownerUsername;
    int storeId;

    @BeforeAll
    void clean() {
        bridge.clear();
        Response resp = bridge.guestEnterSystem();
        String uuid = resp.getDataJson();
        ownerUsername = "GuyStore";
        resp = bridge.signUp(uuid, "guywhoowns@store.com", ownerUsername, "password");
        ownerToken = resp.getDataJson();
        resp = bridge.openStore(ownerToken, ownerUsername, "TestStore");
        storeId = Integer.parseInt(resp.getDataJson());
        resp = bridge.addProductToStore(ownerToken, ownerUsername, storeId,
                new ProductDTO(-1, "TestProduct", 100.3, "Product", 3.5, 2,true,storeId));
        productId = Integer.parseInt(resp.getDataJson());
        bridge.addProductToStore(ownerToken, ownerUsername, storeId,
                new ProductDTO(-1, "TestProduct", 200, "NotProduct", 3.5, 2,true,storeId));
        resp = bridge.openStore(ownerToken, ownerUsername, "TestStore2 Boogaloo");
        int storeId2 = Integer.parseInt(resp.getDataJson());
        bridge.addProductToStore(ownerToken, ownerUsername, storeId2,
                new ProductDTO(-1, "TestProduct", 500.5, "Product", 3.5, 2,true,storeId2));
    }

    @Test
    void getProductDataTest() {
        Assertions.assertDoesNotThrow(() -> bridge.getProductData("", "", productId));
        try {
            Response resp = bridge.getProductData("", null, productId);
            Assertions.assertFalse(resp.getError());
            String json = resp.getDataJson();
            Assertions.assertDoesNotThrow(() -> objectMapper.readValue(json, ProductDTO.class));
            ProductDTO productDTO = objectMapper.readValue(json, ProductDTO.class);
            Assertions.assertEquals(productId, productDTO.getProductID());
            Assertions.assertEquals("TestProduct", productDTO.getProductName());
        } catch (Exception e) {

        }
    }

    @Test
    void getProductDataDoesntExistTest() {
        try {
            Response resp = bridge.getProductData(null, null, Integer.MAX_VALUE);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeProductDoesntExistError(Integer.MAX_VALUE), resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void getProductDataClosedStoreNotOwnerTest() {
        try {
            bridge.closeStore(ownerToken, ownerUsername, storeId);
            Response resp = bridge.getProductData(null, null, productId);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreOfProductIsNotActiveError(productId), resp.getErrorString());
            bridge.reopenStore(ownerToken,ownerUsername,storeId);
        } catch (Exception e) {

        }
    }

    @Test
    void getProductDataClosedStoreOwnerTest() {
        Assertions.assertDoesNotThrow(() -> bridge.getProductData(ownerToken, ownerUsername, productId));
        try {
            bridge.closeStore(ownerToken, ownerUsername, storeId);
            Response resp = bridge.getProductData(ownerToken, ownerUsername, productId);
            Assertions.assertFalse(resp.getError());
            String json1 = resp.getDataJson();
            Assertions.assertDoesNotThrow(() -> objectMapper.readValue(json1, ProductDTO.class));
            ProductDTO productDTO = objectMapper.readValue(json1, ProductDTO.class);
            Assertions.assertEquals(productId, productDTO.getProductID());
            Assertions.assertEquals("TestProduct", productDTO.getProductName());
            bridge.reopenStore(ownerToken,ownerUsername,storeId);
        } catch (Exception e) {

        }
    }

    @Test
    void searchProductsTest() {
        try {
            Response resp = bridge.searchProduct("TestProduct", -1, -1, "Product", -1, -1);
            Assertions.assertFalse(resp.getError());
            String json = resp.getDataJson();
            Assertions.assertDoesNotThrow(() -> objectMapper.readValue(json, new TypeReference<List<ProductDTO>>() {
            }));
            List<ProductDTO> results = objectMapper.readValue(json, new TypeReference<List<ProductDTO>>() {
            });
            Assertions.assertEquals(2, results.size());
            Assertions.assertEquals(productId, results.get(0).getProductID());
            Assertions.assertEquals("TestProduct", results.get(0).getProductName());
        } catch (Exception e) {

        }
    }

    @Test
    void searchAndFilterProductsTest() {
        try {
            Response resp = bridge.searchProduct(null, 100, 105, "Product", -1, -1);
            Assertions.assertFalse(resp.getError());
            String json = resp.getDataJson();
            Assertions.assertDoesNotThrow(() -> objectMapper.readValue(json, new TypeReference<List<ProductDTO>>() {
            }));
            List<ProductDTO> results = objectMapper.readValue(json, new TypeReference<List<ProductDTO>>() {
            });
            Assertions.assertEquals(1, results.size());
            Assertions.assertEquals(productId, results.get(0).getProductID());
            Assertions.assertEquals("Product", results.get(0).getProductCategory());
        } catch (Exception e) {

        }
    }

    @Test
    void searchAndFilterProductsNoResultsTest() {
        try {
            Response resp = bridge.searchProduct(null, 501, 505, "Product", -1, -1);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals("No results for products were found", resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void searchProductsInStoreTest() {
        try {
            Response resp = bridge.searchProductInStore(storeId, "TestProduct", -1, -1, "Product", -1);
            Assertions.assertFalse(resp.getError());
            String json = resp.getDataJson();
            Assertions.assertDoesNotThrow(
                    () -> objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {
                    }));
            Map<String, Integer> results = objectMapper.readValue(json,
                    new TypeReference<Map<String, Integer>>() {
                    });
            Assertions.assertEquals(1, results.keySet().size());
            ProductDTO productDTO = objectMapper.readValue(results.keySet().iterator().next(), ProductDTO.class);
            Assertions.assertEquals(productId, productDTO.getProductID());
            Assertions.assertEquals("TestProduct", productDTO.getProductName());
        } catch (Exception e) {

        }
    }

    @Test
    void searchAndFilterProductsInStoreTest() {
        try {
            Response resp = bridge.searchProductInStore(storeId, null, 100, 105, "Product", -1);
            Assertions.assertFalse(resp.getError());
            String json = resp.getDataJson();
            Assertions.assertDoesNotThrow(
                    () -> objectMapper.readValue(json, new TypeReference<Map<String, Integer>>() {
                    }));
            Map<String, Integer> results = objectMapper.readValue(json,
                    new TypeReference<Map<String, Integer>>() {
                    });
            Assertions.assertEquals(1, results.keySet().size());
            ProductDTO productDTO = objectMapper.readValue(results.keySet().iterator().next(), ProductDTO.class);
            Assertions.assertEquals(productId, productDTO.getProductID());
            Assertions.assertEquals("Product", productDTO.getProductCategory());
        } catch (Exception e) {

        }
    }

    @Test
    void searchAndFilterProductsInStoreNoResultsTest() {
        try {
            Response resp = bridge.searchProductInStore(storeId, "TestProduct", 50, 50, "Product", -1);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals("No products found", resp.getErrorString());
        } catch (Exception e) {

        }
    }

    @Test
    void searchAndFilterProductsInStoreDoesntExistTest() {
        try {
            Response resp = bridge.searchProductInStore(Integer.MAX_VALUE, "TestProduct", 500, 505, "Product", -1);
            Assertions.assertTrue(resp.getError());
            Assertions.assertEquals(Error.makeStoreNoStoreWithIdError(Integer.MAX_VALUE), resp.getErrorString());
        } catch (Exception e) {

        }
    }
}
