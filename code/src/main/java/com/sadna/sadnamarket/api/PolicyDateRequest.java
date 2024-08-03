package com.sadna.sadnamarket.api;

import com.sadna.sadnamarket.domain.buyPolicies.BuyType;

import java.util.List;

public class PolicyDateRequest {
    String category;
    List<BuyType> buyTypes;
    int day;
    int month;
    int year;

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


    public int getDay() {
        return day;
    }

    public void setDay(int day) {
        this.day = day;
    }

    public int getMonth() {
        return month;
    }

    public void setMonth(int month) {
        this.month = month;
    }

    public int getYear() {
        return year;
    }

    public void setYear(int year) {
        this.year = year;
    }
}
