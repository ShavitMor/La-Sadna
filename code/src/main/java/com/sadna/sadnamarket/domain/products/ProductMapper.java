package com.sadna.sadnamarket.domain.products;

import java.util.List;
import java.util.stream.Collectors;

public class ProductMapper {

    public static ProductDTO toProductDTO(Product product) {
        return new ProductDTO(
                product.getProductId(),
                product.getProductName(),
                product.getProductPrice(),
                product.getProductCategory(),
                product.getProductRank(),
                product.getProductWeight(),
                product.isActiveProduct(),product.getDescription(),product.getStoreId());
    }

    public static List<ProductDTO> toProductDTOList(List<Product> products) {
        return products.stream()
                .map(ProductMapper::toProductDTO)
                .collect(Collectors.toList());
    }
}
