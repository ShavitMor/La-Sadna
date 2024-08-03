package com.sadna.sadnamarket.api;

import com.sadna.sadnamarket.domain.buyPolicies.BuyType;

import java.util.List;

public class PolicyAgeRequest {
    String category;
    List<BuyType> buyTypes;
    int minAge;
    int maxAge;

    public String getCategory() {
        return category;
    }

    public void setCategory(String category) {
        this.category = category;
    }

    public List<BuyType> getBuyTypes() {
        return buyTypes;
    }

    public void setBuyTypes(List<BuyType> buyTypes) {
        this.buyTypes = buyTypes;
    }

    public int getMinAge() {
        return minAge;
    }

    public void setMinAge(int minAge) {
        this.minAge = minAge;
    }

    public int getMaxAge() {
        return maxAge;
    }

    public void setMaxAge(int maxAge) {
        this.maxAge = maxAge;
    }
}
