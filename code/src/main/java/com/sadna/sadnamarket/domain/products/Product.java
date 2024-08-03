package com.sadna.sadnamarket.domain.products;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import java.util.*;
import javax.persistence.*;
import java.text.MessageFormat;
@Entity
@Table(name = "Products")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Product {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private int productId;
    @Column(name = "product_name")
    private String productName;
    @Column(name = "product_price")
    private double productPrice;
    @Column(name = "product_category")
    private String productCategory;
    @Column(name = "description")
    private String description;
    @Column(name = "product_rank")
    private double productRank;
    @Column(name = "isActive")
    private boolean isActive = true;
    @Column(name = "product_weight")
    private double productWeight;
    @Column(name = "store_id")
    private int storeId;

    public Product(int productId, String productName, double productPrice, String productCategory, double productRank, double productWeight,int storeId) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.productRank = productRank;
        this.productWeight = productWeight;
        this.storeId = storeId;
        this.description = "";
    }

    public Product(int productId, String productName, double productPrice, String productCategory, double productRank, double productWeight,int storeId, String description) {
        this.productId = productId;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.productRank = productRank;
        this.productWeight = productWeight;
        this.storeId = storeId;
        this.description = description;
    }
    public Product(){
    }

    public int getProductId() {
        return this.productId;
    }

    public String getProductName() {
        return this.productName;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public double getProductPrice() {
        return this.productPrice;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public String getProductCategory() {
        return this.productCategory;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public double getProductRank() {
        return this.productRank;
    }

    public void setProductRank(double productRank) {
        this.productRank = productRank;
    }

    public double getProductWeight() {
        return this.productWeight;
    }

    public void setProductWeight(double productWeight) {
        this.productWeight = productWeight;
    }

    public boolean isActiveProduct() {
        return this.isActive;
    }

    public void disableProduct() {
        this.isActive = false;
    }

    public ProductDTO getProductDTO() {
        return ProductMapper.toProductDTO(this);
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    @Override
    public String toString() {
        return MessageFormat.format(
                "Product'{'productId={0}, productName=''{1}'', productPrice={2}, productCategory=''{3}'', productRank={4}, isActive={5}'}'",
                productId,
                productName, productPrice, productCategory, productRank, isActive);
    }
    public int getStoreId() {
        return storeId;
    }

    public void setActive(boolean active) {
        isActive = active;
    }


    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
}
