package com.sadna.sadnamarket.domain.supply;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SupplyProxy implements SupplyInterface {
    SupplyAdapter real;

    public SupplyProxy(){
        real = new SupplyAdapter();
    }

    @Override
    public boolean canMakeOrder(OrderDetailsDTO orderDetails, AddressDTO address) throws JsonProcessingException {
        if(real.implemented()){
            return real.canMakeOrder(orderDetails, address);
        }
        return true;
    }

    @Override
    public String makeOrder(OrderDetailsDTO orderDetails, AddressDTO address) throws JsonProcessingException {
        if(real.implemented()){
            return real.makeOrder(orderDetails, address);
        }
        return "";
    }

    @Override
    public boolean cancelOrder(String orderCode) throws JsonProcessingException {
        if(real.implemented()){
            return real.cancelOrder(orderCode);
        }
        return true;
    }
}
