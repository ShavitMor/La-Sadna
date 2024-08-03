package com.sadna.sadnamarket.api;

import com.sadna.sadnamarket.domain.buyPolicies.BuyType;

import java.time.LocalTime;
import java.util.List;

public class PolicyHourRequest {
    String category;
    List<BuyType> buyTypes;
    LocalTime fromHour;
    LocalTime toHour;

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

    public LocalTime getFromHour() {
        return fromHour;
    }

    public void setFromHour(LocalTime fromHour) {
        this.fromHour = fromHour;
    }

    public LocalTime getToHour() {
        return toHour;
    }

    public void setToHour(LocalTime toHour) {
        this.toHour = toHour;
    }
}
