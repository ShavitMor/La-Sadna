package com.sadna.sadnamarket.domain.supply;

import com.fasterxml.jackson.core.JsonProcessingException;

public interface SupplyInterface {
    public boolean canMakeOrder(OrderDetailsDTO orderDetails, AddressDTO address) throws JsonProcessingException;
    public String makeOrder(OrderDetailsDTO orderDetails, AddressDTO address) throws JsonProcessingException;
    public boolean cancelOrder(String orderCode) throws JsonProcessingException;
}
