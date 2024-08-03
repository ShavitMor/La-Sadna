package com.sadna.sadnamarket.domain.discountPolicies;

import java.util.Objects;

public class ProductDataPrice {
    int id;
    int storeId;
    String name;
    int amount;
    double oldPrice;
    double newPrice;

    public ProductDataPrice() {
    }
    public ProductDataPrice(int id,int storeId, String name, int amount, double oldPrice, double newPrice) {
        this.id = id;
        this.storeId=storeId;
        this.name = name;
        this.amount = amount;
        this.oldPrice = oldPrice;
        this.newPrice = newPrice;
    }

    // Getter for id
    public int getId() {
        return id;
    }

    // Getter for amount
    public int getAmount() {
        return amount;
    }

    // Getter for oldPrice
    public double getOldPrice() {
        return oldPrice;
    }

    // Getter for newPrice
    public double getNewPrice() {
        return newPrice;
    }
    public int getStoreId() {
        return storeId;
    }

    public String getName() {
        return name;
    }

    public void setName(String name) {
        this.name = name;
    }

    public void setNewPrice(double newPrice) {
        if(newPrice < 0){
            this.newPrice = 0;
        }
        else{
            this.newPrice = newPrice;
        }
    }

    public ProductDataPrice deepCopy(){
        return new ProductDataPrice(this.id,this.storeId, this.name, this.amount,this.oldPrice,this.newPrice);
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        ProductDataPrice that = (ProductDataPrice) o;
        return id == that.id && storeId == that.storeId && amount == that.amount && Double.compare(that.oldPrice, oldPrice) == 0 && Double.compare(that.newPrice, newPrice) == 0 && Objects.equals(name, that.name);
    }

    @Override
    public int hashCode() {
        return Objects.hash(id, storeId, name, amount, oldPrice, newPrice);
    }
}
