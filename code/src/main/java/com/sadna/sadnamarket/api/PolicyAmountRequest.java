package com.sadna.sadnamarket.api;

import com.sadna.sadnamarket.domain.buyPolicies.BuyType;

import java.time.LocalTime;
import java.util.List;

public class PolicyAmountRequest {
    int productId;
    List<BuyType> buyTypes;
    int minAmount;
    int maxAmount;
    String categoryName;

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public List<BuyType> getBuyTypes() {
        return buyTypes;
    }

    public void setBuyTypes(List<BuyType> buyTypes) {
        this.buyTypes = buyTypes;
    }

    public int getMinAmount() {
        return minAmount;
    }

    public void setMinAmount(int minAmount) {
        this.minAmount = minAmount;
    }

    public int getMaxAmount() {
        return maxAmount;
    }

    public void setMaxAmount(int maxAmount) {
        this.maxAmount = maxAmount;
    }

    public String getCategoryName() {
        return categoryName;
    }

    public void setCategoryName(String categoryName) {
        this.categoryName = categoryName;
    }

}
