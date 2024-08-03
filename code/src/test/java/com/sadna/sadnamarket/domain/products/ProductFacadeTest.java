package com.sadna.sadnamarket.domain.products;

import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;

import java.util.Arrays;
import java.util.List;

import static org.junit.jupiter.api.Assertions.*;
import static org.mockito.ArgumentMatchers.anyDouble;
import static org.mockito.ArgumentMatchers.anyInt;
import static org.mockito.ArgumentMatchers.anyList;
import static org.mockito.ArgumentMatchers.anyString;
import static org.mockito.Mockito.*;

public class ProductFacadeTest {
    private ProductFacade productFacade;
    private IProductRepository productRepositoryMock;

    @BeforeEach
    public void setUp() {
        productRepositoryMock = mock(IProductRepository.class);
        productFacade = new ProductFacade(productRepositoryMock);
    }

    @Test
    public void given_ValidProductDetailsAndStoreId_When_AddProduct_Then_ProductIsAdded() {
        when(productRepositoryMock.addProduct(anyString(), anyDouble(), anyString(), anyDouble(), anyDouble(),anyInt(),anyString())).thenReturn(1);
        when(productRepositoryMock.getProduct(1)).thenReturn(new Product(1, "Test Product", 100.0, "Category1", 4.5, 2,1));

        int productId = productFacade.addProduct(1, "Test Product", 100.0, "Category1", 4.5, 2,"");
        assertEquals(1, productId);
        verify(productRepositoryMock, times(1)).addProduct("Test Product", 100.0, "Category1", 4.5, 2,1,"");
    }

    @Test
    public void given_InvalidStoreId_When_AddProduct_Then_ThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> productFacade.addProduct(-1, "Test Product", 100.0, "Category1", 4.5, 7,""));
    }

    @Test
    public void given_ValidStoreIdAndProductId_When_RemoveProduct_Then_ProductIsRemoved() {
        when(productRepositoryMock.isExistProduct(1)).thenReturn(true);
        Product product = new Product(1, "Test Product", 100.0, "Category1", 4.5, 2,1);
        when(productRepositoryMock.getProduct(1)).thenReturn(product);

        productFacade.removeProduct(1, 1);
        verify(productRepositoryMock, times(1)).removeProduct(1);
    }

    @Test
    public void given_InvalidStoreId_When_RemoveProduct_Then_ThrowException() {
        assertThrows(IllegalArgumentException.class, () -> productFacade.removeProduct(-1, 1));
    }

    @Test
    public void given_InvalidStoreId_When_UpdateProduct_Then_ThrowException() {
        assertThrows(IllegalArgumentException.class,
                () -> productFacade.updateProduct(-1, 1, "Updated Product", 150.0, "Updated Category", 4.8));
    }

    @Test
    public void given_ValidStoreProductIdsAndFilters_When_GetFilteredProducts_Then_ReturnFilteredProducts() {
        Product product1 = new Product(1, "Product1", 50.0, "Category1", 3.5, 2,1);
        Product product2 = new Product(2, "Product2", 100.0, "Category2", 4.5, 1.5,1);
        when(productRepositoryMock.getProducts(anyList())).thenReturn(Arrays.asList(product1, product2));

        List<ProductDTO> filteredProducts = productFacade.getFilteredProducts(Arrays.asList(1, 2), "Product1", 100.0,
                "Category1", 3.0);

        assertEquals(1, filteredProducts.size());
        verify(productRepositoryMock, times(1)).getProducts(anyList());
    }

    @Test
    public void given_ValidFilters_When_GetAllFilteredProducts_Then_ReturnAllFilteredProducts() {
        Product product1 = new Product(1, "Product1", 50.0, "Category1", 3.5, 2,1);
        Product product2 = new Product(2, "Product2", 100.0, "Category2", 4.5, 1.5,1);
        when(productRepositoryMock.getAllProducts()).thenReturn(Arrays.asList(product1, product2));

        List<ProductDTO> filteredProducts = productFacade.getAllFilteredProducts("Product1", 50.0, 100.0, "Category1",
                3.0);

        assertEquals(1, filteredProducts.size());
        assertEquals("Product1", filteredProducts.get(0).getProductName());
        verify(productRepositoryMock, times(1)).getAllProducts();
    }

    @Test
    public void given_ProductsExist_When_GetAllProducts_Then_ReturnAllProducts() {
        Product product1 = new Product(1, "Product1", 50.0, "Category1", 3.5, 2,1);
        Product product2 = new Product(2, "Product2", 100.0, "Category2", 4.5, 1.5,1);
        when(productRepositoryMock.getAllProducts()).thenReturn(Arrays.asList(product1, product2));

        List<ProductDTO> allProducts = productFacade.getAllProducts();

        assertEquals(2, allProducts.size());
        verify(productRepositoryMock, times(1)).getAllProducts();
    }

    @Test
    public void given_ProductsExistByName_When_GetAllProductsByName_Then_ReturnProductsByName() {
        Product product1 = new Product(1, "Product1", 50.0, "Category1", 3.5, 2,1);
        when(productRepositoryMock.filterByName("Product1")).thenReturn(Arrays.asList(product1));

        List<ProductDTO> productsByName = productFacade.getAllProductsByName("Product1");

        assertEquals(1, productsByName.size());
        assertEquals("Product1", productsByName.get(0).getProductName());
        verify(productRepositoryMock, times(1)).filterByName("Product1");
    }

    @Test
    public void given_ProductsExistByCategory_When_GetAllProductsByCategory_Then_ReturnProductsByCategory() {
        Product product1 = new Product(1, "Product1", 50.0, "Category1", 3.5, 2,1);
        when(productRepositoryMock.filterByCategory("Category1")).thenReturn(Arrays.asList(product1));

        List<ProductDTO> productsByCategory = productFacade.getAllProductsByCategory("Category1");

        assertEquals(1, productsByCategory.size());
        assertEquals("Category1", productsByCategory.get(0).getProductCategory());
        verify(productRepositoryMock, times(1)).filterByCategory("Category1");
    }

    @Test
    public void given_ValidProductId_When_GetProductDTO_Then_ReturnProductDTO() {
        Product product = new Product(1, "Product1", 50.0, "Category1", 3.5, 2,1);
        when(productRepositoryMock.getProduct(1)).thenReturn(product);

        ProductDTO productDTO = productFacade.getProductDTO(1);

        assertNotNull(productDTO);
        assertEquals("Product1", productDTO.getProductName());
        verify(productRepositoryMock, times(1)).getProduct(1);
    }
}
