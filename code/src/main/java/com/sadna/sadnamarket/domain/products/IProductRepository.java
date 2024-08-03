package com.sadna.sadnamarket.domain.products;

import java.util.List;
import java.util.Set;

public interface IProductRepository {

    public Product getProduct(int productId);

    public Set<Integer> getAllProductIds();

    public List<Product> getAllProducts();

    List<Product> getProducts(List<Integer> productIds);

    public void removeProduct(int productId);

    public int addProduct(String productName, double productPrice, String productCategory, double productRank, double productWeight, int storeId, String description);


    public boolean isExistProduct(int productId);
    public void editProduct(int productId, String newProductName, double newPrice, String newCategory,
                            double newRank, String newDesc);

    public List<Product> filterByName(String productName);

    public List<Product> filterByCategory(String category);
    public List <Product> getTopProducts();

    public void clean();
}
