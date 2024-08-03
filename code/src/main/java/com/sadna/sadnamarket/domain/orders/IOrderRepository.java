package com.sadna.sadnamarket.domain.orders;

import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;

import java.util.List;
import java.util.Map;

public interface IOrderRepository {
    int createOrder(Map<Integer, OrderDTO> storeOrdersDTO,String memberName);
    List<ProductDataPrice> getOrders(int storeId);
    List<OrderDTO> getOrderHistory(int storeId);

    Map<Integer,OrderDetails> getProductDataPriceByMember(String nameMember);
    Map<Integer,OrderDTO> getOrderByOrderId(int orderId);
    List<OrderDTO> getAllOrders();

    public void clear();
}