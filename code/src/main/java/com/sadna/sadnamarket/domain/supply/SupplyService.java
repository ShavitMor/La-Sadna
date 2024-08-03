package com.sadna.sadnamarket.domain.supply;

import com.fasterxml.jackson.core.JsonProcessingException;

public class SupplyService {
    private static SupplyService instance = null;
    private SupplyInterface controller;
    public static SupplyService getInstance(){
        if(instance == null){
            instance = new SupplyService();
        }
        return instance;
    }

    public SupplyService(){
        this.controller = new SupplyProxy();
    }

    public boolean canMakeOrder(OrderDetailsDTO orderDetails, AddressDTO address) {
        try{
            return controller.canMakeOrder(orderDetails, address);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public String makeOrder(OrderDetailsDTO orderDetails, AddressDTO address) throws JsonProcessingException {
        return controller.makeOrder(orderDetails, address);
    }

    public boolean cancelOrder(String orderCode) {
        try{
            return controller.cancelOrder(orderCode);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
        }
        return false;
    }

    public void setController(SupplyInterface impl){
        this.controller = impl;
    }
}
