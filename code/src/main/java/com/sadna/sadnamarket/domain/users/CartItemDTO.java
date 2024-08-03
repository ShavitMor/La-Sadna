package com.sadna.sadnamarket.domain.users;

public class CartItemDTO {
    private int storeId;
    private int productId;
    private int amount;

    public CartItemDTO(int storeId, int productId, int amount) {
        this.storeId = storeId;
        this.productId = productId;
        this.amount = amount;
    }

    // Getters and setters (optional)
    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getProductId() {
        return productId;
    }

    public void setProductId(int productId) {
        this.productId = productId;
    }

    public int getAmount() {
        return amount;
    }

    public void setAmount(int amount) {
        this.amount = amount;
    }
}
