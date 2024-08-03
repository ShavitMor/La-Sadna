package com.sadna.sadnamarket.domain.products;

import com.sadna.sadnamarket.service.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.util.*;
import java.util.stream.Collectors;

public class ProductFacade {
    private static ProductFacade instance;
    private IProductRepository productRepository;
    private static final Logger logger = LogManager.getLogger(ProductFacade.class);

    // will be private
    public ProductFacade() {
        productRepository = new MemoryProductRepository();
    }

    public ProductFacade(IProductRepository productRepository) {
        this.productRepository = productRepository;
    }

    public static ProductFacade getInstance() {
        if (instance == null) {
            instance = new ProductFacade();
            logger.info("ProductFacade instance created");
        }
        return instance;
    }

    public int addProduct(int storeId, String productName, double productPrice, String productCategory,
            double productRank, double productWeight, String description) {
        logger.info("Adding product with name: {}, price: {}, category: {}, rank: {} to store ID: {}", productName,
                productPrice, productCategory, productRank, storeId);
        if (storeId < 0) {
            String errorMessage = Error.makeProductStoreIdInvalidError(storeId);
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            checkProductAttributes(productName, productPrice, productCategory, productRank);
            int productId = productRepository.addProduct(productName, productPrice, productCategory, productRank, productWeight,storeId, description);
            //Product createdProduct = productRepository.getProduct(productId);
            logger.info("Product added with ID: {}", productId);
            return productId;
        } catch (Exception e) {
            logger.error("Error adding product: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void removeProduct(int storeId, int productId) {
        logger.info("Removing product with ID: {} from store ID: {}", productId, storeId);
        if (storeId < 0) {
            String errorMessage = Error.makeProductStoreIdInvalidError(storeId);
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            if (!isProductExistInStore(storeId, productId)) {
                String errorMessage = Error.makeProductDoesntExistInStoreError(storeId, productId);
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }
            productRepository.removeProduct(productId);
            logger.info("Product removed with ID: {}", productId);
        } catch (Exception e) {
            logger.error("Error removing product: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void updateProduct(int storeId, int productId, String newProductName, double newPrice, String newCategory,
            double newRank, String newDesc) {
        logger.info("Updating product with ID: {} in store ID: {}", productId, storeId);
        if (storeId < 0) {
            String errorMessage = Error.makeProductStoreIdInvalidError(storeId);
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            if (!isProductExistInStore(storeId, productId)) {
                String errorMessage = Error.makeProductDoesntExistInStoreError(storeId, productId);
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            checkProductAttributes(newProductName, newPrice, newCategory, newRank);
            Product productToUpdate = productRepository.getProduct(productId);

            if (!productToUpdate.isActiveProduct()) {
                String errorMessage = Error.makeProductAlreadyDeletedFromStoreError(storeId, productId);
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            productRepository.editProduct(productId,newProductName,newPrice,newCategory,newRank,newDesc);
            logger.info("Product updated with ID: {}", productId);
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage(), e);
            throw e;
        }
    }

    public void updateProduct(int storeId, int productId, String newProductName, double newPrice, String newCategory,
                              double newRank) {
        logger.info("Updating product with ID: {} in store ID: {}", productId, storeId);
        if (storeId < 0) {
            String errorMessage = Error.makeProductStoreIdInvalidError(storeId);
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }

        try {
            if (!isProductExistInStore(storeId, productId)) {
                String errorMessage = Error.makeProductDoesntExistInStoreError(storeId, productId);
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            checkProductAttributes(newProductName, newPrice, newCategory, newRank);
            Product productToUpdate = productRepository.getProduct(productId);

            if (!productToUpdate.isActiveProduct()) {
                String errorMessage = Error.makeProductAlreadyDeletedFromStoreError(storeId, productId);
                logger.error(errorMessage);
                throw new IllegalArgumentException(errorMessage);
            }

            productRepository.editProduct(productId,newProductName,newPrice,newCategory,newRank,"");
            logger.info("Product updated with ID: {}", productId);
        } catch (Exception e) {
            logger.error("Error updating product: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ProductDTO> getFilteredProducts(List<Integer> storeProductIds, String productName,
            double maxProductPrice, String productCategory, double minProductRank) {
        logger.info("Filtering products with product name: {}, max price: {}, category: {}, min rank: {}", productName,
                maxProductPrice, productCategory, minProductRank);
        try {
            List<Product> storeProducts = productRepository.getProducts(storeProductIds);

            if (productName != null &&!productName.equals("")&& isValidProductName(productName)) {
                storeProducts = storeProducts.stream()
                        .filter(product -> product.getProductName().contains(productName))
                        .collect(Collectors.toList());
            }

            if (productCategory != null && isValidProductCategory(productCategory)) {
                storeProducts = storeProducts.stream()
                        .filter(product -> product.getProductCategory().equals(productCategory))
                        .collect(Collectors.toList());
            }

            if (maxProductPrice != -1 && isValidProductPrice(maxProductPrice)) {
                storeProducts = storeProducts.stream()
                        .filter(product -> product.getProductPrice() <= maxProductPrice)
                        .collect(Collectors.toList());
            }

            if (minProductRank != -1 && isValidProductRank(minProductRank)) {
                storeProducts = storeProducts.stream()
                        .filter(product -> product.getProductPrice() >= minProductRank)
                        .collect(Collectors.toList());
            }

            List<ProductDTO> filteredProducts = ProductMapper.toProductDTOList(storeProducts);
            logger.info("Filtered products retrieved: {}", filteredProducts.size());
            return filteredProducts;
        } catch (Exception e) {
            logger.error("Error filtering products: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ProductDTO> getAllFilteredProducts(String productName, double minProductPrice, double maxProductPrice,
            String productCategory, double minProductRank) {
        logger.info(
                "Filtering all products with product name: {}, min price: {}, max price: {}, category: {}, min rank: {}",
                productName, minProductPrice, maxProductPrice, productCategory, minProductRank);
        try {
            List<Product> products = productRepository.getAllProducts();    
            

            if (productName != null&&!productName.equals("")) {
                if (isValidProductName(productName)) {
                    products = products.stream()
                            .filter(product -> product.getProductName().equals(productName))
                            .collect(Collectors.toList());
                } else {
                    throw new IllegalArgumentException(Error.makeProductAspectCannotBeNullOrEmptyError("name"));
                }
            }
           
            if (productCategory != null) {
                if (isValidProductName(productCategory)) {
                    products = products.stream()
                            .filter(product -> product.getProductCategory().equals(productCategory))
                            .collect(Collectors.toList());
                } else {
                    throw new IllegalArgumentException(Error.makeProductAspectCannotBeNullOrEmptyError("category"));
                }
            }

            if (minProductPrice != -1 && maxProductPrice != -1) {
                if (minProductPrice > maxProductPrice) {
                    throw new IllegalArgumentException(Error.makeProductMinimumPriceMustBeBelowMaximumError());
                }
            }
               
                 System.out.println("ah sheli"  );
            System.out.println(products);
            if (minProductPrice != -1) {
                if (isValidProductPrice(minProductPrice)) {
                    products = products.stream()
                            .filter(product -> product.getProductPrice() >= minProductPrice)
                            .collect(Collectors.toList());
                } else {
                    throw new IllegalArgumentException(Error.makeProductValuePriceCannotBeNegativeError("Min"));
                }
            }
              
            if (maxProductPrice != -1) {
                if (isValidProductPrice(maxProductPrice)) {
                    products = products.stream()
                            .filter(product -> product.getProductPrice() <= maxProductPrice)
                            .collect(Collectors.toList());
                } else {
                    throw new IllegalArgumentException(Error.makeProductValuePriceCannotBeNegativeError("Max"));
                }
            }

            if (minProductRank != -1) {
                if (isValidProductRank(minProductRank)) {
                    products = products.stream()
                            .filter(product -> product.getProductRank() >= minProductRank)
                            .collect(Collectors.toList());
                } else {
                    throw new IllegalArgumentException(Error.makeProductRankHasToBeBetweenError());
                }
            }

            List<ProductDTO> filteredProducts = ProductMapper.toProductDTOList(products);
            logger.info("Filtered products retrieved: {}", filteredProducts.size());
            return filteredProducts;
        } catch (Exception e) {
            logger.error("Error filtering all products: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ProductDTO> getAllProducts() {
        logger.info("Retrieving all products");
        try {
            List<ProductDTO> allProducts = ProductMapper.toProductDTOList(productRepository.getAllProducts());
            logger.info("All products retrieved: {}", allProducts.size());
            return allProducts;
        } catch (Exception e) {
            logger.error("Error retrieving all products: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ProductDTO> getAllProductsByName(String productName) {
        logger.info("Retrieving all products with name: {}", productName);
        try {
            List<ProductDTO> productsByName = ProductMapper
                    .toProductDTOList(productRepository.filterByName(productName));
            logger.info("Products retrieved by name: {}", productsByName.size());
            return productsByName;
        } catch (Exception e) {
            logger.error("Error retrieving products by name: {}", e.getMessage(), e);
            throw e;
        }
    }

    public List<ProductDTO> getAllProductsByCategory(String category) {
        logger.info("Retrieving all products with category: {}", category);
        try {
            List<ProductDTO> productsByCategory = ProductMapper
                    .toProductDTOList(productRepository.filterByCategory(category));
            logger.info("Products retrieved by category: {}", productsByCategory.size());
            return productsByCategory;
        } catch (Exception e) {
            logger.error("Error retrieving products by category: {}", e.getMessage(), e);
            throw e;
        }
    }

    private boolean isValidProductName(String name) {
        return name != null && !name.trim().isEmpty();
    }

    private boolean isValidProductRank(double productRank) {
        return productRank >= 0 && productRank <= 5;
    }

    private boolean isValidProductPrice(double productPrice) {
        return productPrice >= 0;
    }

    private boolean isValidProductCategory(String category) {
        return category != null && !category.trim().isEmpty();
    }

    private void checkProductAttributes(String newProductName, double newPrice, String newCategory, double newRank) {
        boolean isValidProductName = isValidProductName(newProductName);
        boolean isValidProductPrice = isValidProductPrice(newPrice);
        boolean isValidProductCategory = isValidProductCategory(newCategory);
        boolean isValidProductRank = isValidProductRank(newRank);

        boolean isAllValid = isValidProductName && isValidProductPrice && isValidProductCategory && isValidProductRank;

        if (!isAllValid) {
            StringBuilder result = new StringBuilder("Product information is invalid: ");

            if (!isValidProductName) {
                result.append("Product name cannot be null or empty. ");
            }
            if (!isValidProductPrice) {
                result.append("Product price cannot be negative. ");
            }
            if (!isValidProductCategory) {
                result.append("Product category cannot be null or empty. ");
            }
            if (!isValidProductRank) {
                result.append("Product rank must be between 0 and 5. ");
            }

            String errorMessage = result.toString();
            logger.error(errorMessage);
            throw new IllegalArgumentException(errorMessage);
        }
    }

    private boolean isProductExistInStore(int storeId, int productId) {
        // checked in store facade
        return true;
    }

    public ProductDTO getProductDTO(int productId) {
        logger.info("Retrieving product DTO for product ID: {}", productId);
        try {
            Product result = productRepository.getProduct(productId);
            ProductDTO productDTO = result.getProductDTO();
            logger.info("Product DTO retrieved for product ID: {}", productId);
            return productDTO;
        } catch (Exception e) {
            logger.error("Error retrieving product DTO: {}", e.getMessage(), e);
            throw e;
        }
    }
    public List<ProductDTO> getTopProducts(){
        return ProductMapper.toProductDTOList(productRepository.getTopProducts());
    }

    public void clear(){
        productRepository.clean();
    }

    public boolean productExists(int productId) {
        return productRepository.isExistProduct(productId);
    }
}
