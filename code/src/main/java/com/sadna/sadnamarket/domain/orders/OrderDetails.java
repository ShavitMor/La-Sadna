package com.sadna.sadnamarket.domain.orders;

import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;

import java.util.List;

public class OrderDetails {
    private List<ProductDataPrice> products;
    private String dateTimeOfPurchase;

    // Constructor
    public OrderDetails(List<ProductDataPrice> products, String dateTimeOfPurchase) {
        this.products = products;
        this.dateTimeOfPurchase = dateTimeOfPurchase;
    }

    // Getters
    public List<ProductDataPrice> getProducts() {
        return products;
    }

    public String getDateTimeOfPurchase() {
        return dateTimeOfPurchase;
    }

    // Setters
    public void setProducts(List<ProductDataPrice> products) {
        this.products = products;
    }

    public void setDateTimeOfPurchase(String dateTimeOfPurchase) {
        this.dateTimeOfPurchase = dateTimeOfPurchase;
    }

}

