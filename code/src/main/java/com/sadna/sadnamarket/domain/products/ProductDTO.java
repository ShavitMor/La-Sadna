package com.sadna.sadnamarket.domain.products;
import java.util.*;
//import java.util.Objects;
import javax.persistence.*;

//@Entity
//@Table(name = "Products")
public class ProductDTO {
    //@Id
    //@GeneratedValue(strategy = GenerationType.AUTO)
    private int productID;
    //@Column(name = "store_id")
    private int storeId;
    //@Column(name = "product_name")
    private String productName;
    //@Column(name = "product_price")
    private double productPrice;
    //@Column(name = "product_category")
    private String productCategory;
    //@Column(name = "product_rank")
    private double productRank;
    //@Column(name = "isActive")
    private boolean isActive;
    //@Column(name = "product_weight")
    private double productWeight;
    //@Column(name = "description")
    private String description;

    public ProductDTO(int productID, String productName, double productPrice, String productCategory,
            double productRank, double productWeight, boolean isActive, String description, int storeId) {
        this.productID = productID;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.productRank = productRank;
        this.productWeight = productWeight;
        this.isActive = isActive;
        this.description = description;
        this.storeId = storeId;
    }
     public ProductDTO(int productID, String productName, double productPrice, String productCategory,
            double productRank, double productWeight, boolean isActive, int storeId) {
        this.productID = productID;
        this.productName = productName;
        this.productPrice = productPrice;
        this.productCategory = productCategory;
        this.productRank = productRank;
        this.productWeight = productWeight;
        this.isActive = isActive;
        this.storeId = storeId;
        this.description = "";
    }


    public ProductDTO() {
    }

    public int getProductID() {
        return productID;
    }

    public String getProductName() {
        return this.productName;
    }

    public double getProductPrice() {
        return this.productPrice;
    }

    public String getProductCategory() {
        return productCategory;
    }

    public double getProductRank() {
        return this.productRank;
    }

    public boolean isActiveProduct() {
        return isActive;
    }

    public double getProductWeight() { return this.productWeight; }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }

    public void setActiveProduct(boolean active) {
        isActive = active;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDTO that = (ProductDTO) o;
        return storeId == that.storeId && productID == that.productID && Double.compare(that.productPrice, productPrice) == 0 && Double.compare(that.productRank, productRank) == 0 && isActive == that.isActive && Double.compare(that.productWeight, productWeight) == 0 && Objects.equals(productName, that.productName) && Objects.equals(productCategory, that.productCategory) && Objects.equals(description, that.description);
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, productID, productName, productPrice, productCategory, productRank, isActive, productWeight, description);
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public void setProductName(String productName) {
        this.productName = productName;
    }

    public void setProductPrice(double productPrice) {
        this.productPrice = productPrice;
    }

    public void setProductCategory(String productCategory) {
        this.productCategory = productCategory;
    }

    public void setProductRank(double productRank) {
        this.productRank = productRank;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public void setProductWeight(double productWeight) {
        this.productWeight = productWeight;
    }
}
