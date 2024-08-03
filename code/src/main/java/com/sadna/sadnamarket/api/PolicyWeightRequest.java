package com.sadna.sadnamarket.api;

import com.sadna.sadnamarket.domain.buyPolicies.BuyType;

import java.util.List;

public class PolicyWeightRequest {
    int productId;
    List<BuyType> buyTypes;
    double minWeight;
    double maxWeight;

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

    public double getMinWeight() {
        return minWeight;
    }

    public void setMinWeight(double minWeight) {
        this.minWeight = minWeight;
    }

    public double getMaxWeight() {
        return maxWeight;
    }

    public void setMaxWeight(double maxWeight) {
        this.maxWeight = maxWeight;
    }
}
