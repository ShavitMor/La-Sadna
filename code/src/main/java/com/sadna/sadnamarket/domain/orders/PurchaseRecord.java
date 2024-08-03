package com.sadna.sadnamarket.domain.orders;
import java.util.Map;

public class PurchaseRecord {
    private String memberName;
    private String dateTimeOfPurchase;
    private Map<Integer, Order> orders;


    public PurchaseRecord(String memberName, String dateTimeOfPurchase, Map<Integer, Order> orders) {
        this.memberName = memberName;
        this.dateTimeOfPurchase = dateTimeOfPurchase;
        this.orders = orders;
    }

    public String getMemberName() {
        return memberName;
    }


    public Map<Integer, Order> getOrders() {
        return orders;
    }

    public void setOrders(Map<Integer, Order> orders) {
        this.orders = orders;
    }

    public String getDateOfPurchase() {
        return dateTimeOfPurchase;
    }


}

