package com.sadna.sadnamarket.domain.users;

import java.util.List;

import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;

public class UserOrderDTO {
    private List<ProductDataPrice> productsData;
    private double oldPrice;
    private double newPrice;

    public UserOrderDTO(List<ProductDataPrice> allProudctsData, double oldPrice, double newPrice){
        this.productsData=allProudctsData;
        this.oldPrice=oldPrice;
        this.newPrice=newPrice;
    }

    public List<ProductDataPrice> getProductsData(){
        return productsData;
    }
   public double getOldPrice(){
        return oldPrice;
    }
    public double getNewPrice(){
        return newPrice;
    }

}
