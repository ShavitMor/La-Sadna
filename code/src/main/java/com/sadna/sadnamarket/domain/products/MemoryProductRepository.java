package com.sadna.sadnamarket.domain.products;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;
import java.util.stream.Collectors;

import com.sadna.sadnamarket.service.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

public class MemoryProductRepository implements IProductRepository {
    private int nextProductId;
    private Map<Integer, Product> products;

    private static final Logger logger = LogManager.getLogger(MemoryProductRepository.class);

    public MemoryProductRepository() {
        this.nextProductId = 0;
        this.products = new ConcurrentHashMap<>();
    }

    @Override
    public int addProduct(String productName, double productPrice,
                          String productCategory, double productRank, double productWeight, int storeId, String description) {
        synchronized (products) {
            Product createdProduct = new Product(nextProductId, productName, productPrice,
                    productCategory, productRank, productWeight, storeId, description);

            products.put(nextProductId, createdProduct);
            nextProductId++;
            logger.info("product " + createdProduct + " succesfully added");
            return nextProductId - 1;
        }
    }

    @Override
    public Product getProduct(int productId) {
        synchronized (products) {

            if (!isExistProduct(productId)) {
                logger.error(String.format("Product Id %d does not exist.", productId));
                throw new IllegalArgumentException(Error.makeProductDoesntExistError(productId));
            }
            Product product = products.get(productId);
//            if (!product.isActiveProduct()) {
//                logger.error(String.format("Product Id %d was already removed.", productId));
//                throw new IllegalArgumentException(Error.makeProductAlreadyRemovedError(productId));
//            }

            return product;
        }
    }

    @Override
    public Set<Integer> getAllProductIds() {
        return products.keySet();
    }

    @Override
    public List<Product> getAllProducts() {
        synchronized (products) {

            return new ArrayList<>(products.values());
        }
    }

    @Override
    public List<Product> getProducts(List<Integer> productIds) {
        synchronized (products) {

            List<Product> foundProducts = new ArrayList<>();
            for (int productId : productIds) {
                if (isExistProduct(productId))
                    foundProducts.add(products.get(productId));
                else
                    logger.error(String.format("Product Id %d does not exist.", productId));
            }
            return foundProducts;
        }
    }

    @Override
    public void removeProduct(int productId) {
        synchronized (products) {

            if (!isExistProduct(productId)) {
                logger.error(String.format("Product Id %d does not exist.", productId));
                throw new IllegalArgumentException(Error.makeProductDoesntExistError(productId));
            }

            Product product = getProduct(productId);
            if (!product.isActiveProduct()) {
                logger.error(String.format("Product Id %d was already removed.", productId));
                throw new IllegalArgumentException(Error.makeProductAlreadyRemovedError(productId));
            }
            product.disableProduct();
            logger.info(String.format("Product Id %d was succesully removed.", productId));
        }
    }

    @Override
    public boolean isExistProduct(int productId) {
        synchronized (products) {

            return products.containsKey(productId);
        }
    }

    @Override
    public void editProduct(int productId, String newProductName, double newPrice, String newCategory, double newRank, String newDesc) {
        Product productToUpdate = products.get(productId);
        productToUpdate.setProductName(newProductName);
        productToUpdate.setProductPrice(newPrice);
        productToUpdate.setProductCategory(newCategory);
        productToUpdate.setProductRank(newRank);
        productToUpdate.setDescription(newDesc);
    }

    @Override
    public List<Product> filterByName(String productName) {
        synchronized (products) {

            return products.values().stream()
                    .filter(product -> product.getProductName().equalsIgnoreCase(productName)
                            && product.isActiveProduct())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Product> filterByCategory(String category) {
        synchronized (products) {

            return products.values().stream()
                    .filter(product -> product.getProductCategory().equalsIgnoreCase(category)
                            && product.isActiveProduct())
                    .collect(Collectors.toList());
        }
    }

    @Override
    public List<Product> getTopProducts() {
        return products.values().stream().collect(Collectors.toList());
    }

    @Override
    public void clean() {
        this.nextProductId = 0;
        this.products = new ConcurrentHashMap<>();
    }
}
