package com.sadna.sadnamarket.domain.products;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import static org.junit.jupiter.api.Assertions.*;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.ConcurrentHashMap;

public class MemoryProductRepositoryTest {
    private MemoryProductRepository repository;
    private Map<Integer, Product> productMap;

    @BeforeEach
    public void setUp() {
        repository = new MemoryProductRepository();
        productMap = new ConcurrentHashMap<>();
    }

    @Test
    public void given_ValidProductDetails_When_AddProduct_Then_ProductIsAdded() {
        int productId = repository.addProduct("Test Product", 100.0, "Category1", 4.5, 2, 1, "");
        assertNotNull(productId);
        Product product = repository.getProduct(productId);
        assertEquals("Test Product", product.getProductName());
        assertEquals(100.0, product.getProductPrice());
        assertEquals("Category1", product.getProductCategory());
        assertEquals(4.5, product.getProductRank());
    }

    @Test
    public void given_ExistingProductId_When_GetProduct_Then_ProductIsReturned() {
        int productId = repository.addProduct("Test Product", 100.0, "Category1", 4.5, 2, 1,"");
        Product product = repository.getProduct(productId);
        assertNotNull(product);
        assertEquals("Test Product", product.getProductName());
    }

    @Test
    public void given_MultipleProducts_When_GetAllProducts_Then_AllProductsAreReturned() {
        repository.addProduct("Product1", 50.0, "Category1", 3.5, 6, 1,"");
        repository.addProduct("Product2", 150.0, "Category2", 4.0, 3.7, 1,"");
        List<Product> products = repository.getAllProducts();
        assertEquals(2, products.size());
    }

    // @Test
//    public void given_ExistingProductId_When_RemoveProduct_Then_ProductIsRemoved() {
//        int productId = repository.addProduct("Product1", 50.0, "Category1", 3.5, 1,1);
//        repository.removeProduct(productId);
//        Product product = repository.getProduct(productId);
//        assertFalse(product.isActiveProduct());
//    }

    @Test
    public void given_ExistingProductName_When_FilterByName_Then_ReturnProductsWithThatName() {
        repository.addProduct("Product1", 50.0, "Category1", 3.5, 1, 1,"");
        repository.addProduct("Product2", 150.0, "Category2", 4.0, 12, 1,"");
        List<Product> products = repository.filterByName("Product1");
        assertEquals(1, products.size());
        assertEquals("Product1", products.get(0).getProductName());
    }

    @Test
    public void given_ExistingCategory_When_FilterByCategory_Then_ReturnProductsWithThatCategory() {
        repository.addProduct("Product1", 50.0, "Category1", 3.5, 2, 1,"");
        repository.addProduct("Product2", 150.0, "Category2", 4.0, 3, 1,"");
        List<Product> products = repository.filterByCategory("Category1");
        assertEquals(1, products.size());
        assertEquals("Category1", products.get(0).getProductCategory());
    }

    @Test
    public void given_ListOfProductIds_When_GetProducts_Then_ReturnProducts() {
        int productId1 = repository.addProduct("Product1", 50.0, "Category1", 3.5, 4, 1,"");
        int productId2 = repository.addProduct("Product2", 150.0, "Category2", 4.0, 1.2, 1,"");
        List<Product> products = repository.getProducts(List.of(productId1, productId2));
        assertEquals(2, products.size());
    }

    @Test
    public void given_ProductsAdded_When_GetAllProductIds_Then_ReturnAllProductIds() {
        int productId1 = repository.addProduct("Product1", 50.0, "Category1", 3.5, 3, 1,"");
        int productId2 = repository.addProduct("Product2", 150.0, "Category2", 4.0, 1, 1,"");
        Set<Integer> productIds = repository.getAllProductIds();
        assertEquals(2, productIds.size());
        assertTrue(productIds.contains(productId1));
        assertTrue(productIds.contains(productId2));
    }

    @Test
    public void given_ExistingProductId_When_IsExistProduct_Then_ReturnTrue() {
        int productId = repository.addProduct("Product1", 50.0, "Category1", 3.5, 4, 1,"");
        assertTrue(repository.isExistProduct(productId));
    }

    @Test
    public void given_NonExistentProductId_When_RemoveProduct_Then_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> repository.removeProduct(999));
    }

    @Test
    public void given_NonExistentProductId_When_GetProduct_Then_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> repository.getProduct(999));
    }
}
