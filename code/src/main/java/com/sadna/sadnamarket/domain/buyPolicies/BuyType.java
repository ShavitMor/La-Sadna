package com.sadna.sadnamarket.domain.buyPolicies;

public enum BuyType {
    immidiatePurchase("immidiatePurchase");

    private final String value;

    BuyType(String value) {
        this.value = value;
    }

    public String getValue() {
        return value;
    }

    public static BuyType fromValue(String value) {
        for (BuyType buyType : BuyType.values()) {
            if (buyType.value == value) {
                return buyType;
            }
        }
        throw new IllegalArgumentException("Invalid value: " + value);
    }
}
