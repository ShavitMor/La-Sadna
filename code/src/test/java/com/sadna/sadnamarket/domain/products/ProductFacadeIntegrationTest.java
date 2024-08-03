package com.sadna.sadnamarket.domain.products;

import com.sadna.sadnamarket.service.Error;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.Mockito.*;

public class ProductFacadeIntegrationTest {
    private ProductFacade productFacade;
    private IProductRepository productRepository;

    @BeforeEach
    public void setUp() {
        productRepository = new MemoryProductRepository(); // Utilisez une implémentation réelle ou un stub pour les
        // tests d'intégration
        productFacade = new ProductFacade(productRepository);

        // Ajout de produits communs pour les tests
        productFacade.addProduct(1, "Product1", 50.0, "Category1", 3.5, 2,"");
        productFacade.addProduct(1, "Product2", 100.0, "Category2", 4.5, 1.5,"");
    }

    @Test
    public void given_ValidProductDetailsAndStoreId_When_AddProduct_Then_ProductIsAdded() {
        // Assuming you have added a product in a setup method or a previous test
        int productId = productFacade.addProduct(1, "To Be Added", 50.0, "Category1", 3.5, 2,"");
        assertTrue(productId != -1);
        Product addedProduct = productRepository.getProduct(productId);

        assertEquals("To Be Added", addedProduct.getProductName());
        assertEquals(1, addedProduct.getStoreId());
        assertEquals(50.0, addedProduct.getProductPrice());
        assertEquals("Category1", addedProduct.getProductCategory());
        assertEquals(3.5, addedProduct.getProductRank());
        assertEquals(2, addedProduct.getProductWeight());
    }

    @Test
    public void given_InvalidStoreId_When_AddProduct_Then_ThrowException() {
        IllegalArgumentException expected = assertThrows(IllegalArgumentException.class,
                () -> productFacade.addProduct(-1, "Test Product", 100.0, "Category1", 4.5, 7,""));

        String expectedMessage = Error.makeProductStoreIdInvalidError(-1);
        assertEquals(expectedMessage, expected.getMessage());
    }

    @Test
    public void given_ValidStoreIdAndProductId_When_RemoveProduct_Then_ProductIsRemoved() {
        // Assuming you have added a product in a setup method or a previous test
        int productId = productFacade.addProduct(1, "To Be Removed", 50.0, "Category1", 3.5, 2,"");
        assertTrue(productId != -1);

        productFacade.removeProduct(1, productId);

        assertFalse(productRepository.getProduct(productId).isActiveProduct());
    }

    @Test
    public void given_ValidDetails_When_UpdateProduct_Then_ProductIsUpdated() {
        int productId = productFacade.addProduct(1, "Test Product", 100.0, "Category1", 4.5, 2,"");

        productFacade.updateProduct(1, productId, "Updated Product", 150.0, "Updated Category", 4.8);

        Product updatedProduct = productRepository.getProduct(productId);
        assertNotNull(updatedProduct);
        assertEquals("Updated Product", updatedProduct.getProductName());
        assertEquals(150.0, updatedProduct.getProductPrice());
        assertEquals("Updated Category", updatedProduct.getProductCategory());
        assertEquals(4.8, updatedProduct.getProductRank());
    }

    @Test
    public void given_ValidStoreProductIdsAndFilters_When_GetFilteredProducts_Then_ReturnFilteredProducts() {
        List<ProductDTO> filteredProducts = productFacade.getFilteredProducts(Arrays.asList(0, 1), "Product1", 100.0,
                "Category1", 3.0);

        assertEquals(1, filteredProducts.size());
        assertEquals("Product1", filteredProducts.get(0).getProductName());
    }

    @Test
    public void given_ValidFilters_When_GetAllFilteredProducts_Then_ReturnAllFilteredProducts() {
        List<ProductDTO> filteredProducts = productFacade.getAllFilteredProducts("Product1", 50.0, 100.0, "Category1",
                3.0);

        assertEquals(1, filteredProducts.size());
        assertEquals("Product1", filteredProducts.get(0).getProductName());
    }

    @Test
    public void given_ProductsExist_When_GetAllProducts_Then_ReturnAllProducts() {
        List<ProductDTO> allProducts = productFacade.getAllProducts();

        assertEquals(2, allProducts.size());
    }

    @Test
    public void given_ProductsExistByName_When_GetAllProductsByName_Then_ReturnProductsByName() {
        List<ProductDTO> productsByName = productFacade.getAllProductsByName("Product1");

        assertEquals(1, productsByName.size());
        assertEquals("Product1", productsByName.get(0).getProductName());
    }

    @Test
    public void given_ProductsExistByCategory_When_GetAllProductsByCategory_Then_ReturnProductsByCategory() {
        List<ProductDTO> productsByCategory = productFacade.getAllProductsByCategory("Category1");

        assertEquals(1, productsByCategory.size());
        assertEquals("Category1", productsByCategory.get(0).getProductCategory());
        assertEquals(1, productsByCategory.get(0).getStoreId());
    }

    @Test
    public void given_ValidProductId_When_GetProductDTO_Then_ReturnProductDTO() {
        int productId = productFacade.addProduct(1, "Product1", 50.0, "Category1", 3.5, 2,"");

        ProductDTO productDTO = productFacade.getProductDTO(productId);

        assertNotNull(productDTO);
        assertEquals("Product1", productDTO.getProductName());
        assertEquals(1, productDTO.getStoreId());
    }
}
