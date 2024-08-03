package com.sadna.sadnamarket.domain.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.stores.StoreFacade;

import java.util.*;

public class OrderFacade {
    private IOrderRepository orderRepository;
    private StoreFacade storeFacade;

    public OrderFacade(IOrderRepository orderRepository) {
        this.orderRepository = orderRepository;
    }
    public void setStoreFacade(StoreFacade storeFacade) {
        this.storeFacade = storeFacade;
    }

    public int createOrder(Map<Integer, List<ProductDataPrice>> storeBag,String memberName){
        Map<Integer, OrderDTO>ordersStore = new HashMap<>();
        for (Map.Entry<Integer, List<ProductDataPrice>> bag : storeBag.entrySet()) {
            String storeName = storeFacade.getStoreInfo(bag.getKey()).getStoreName();
            OrderDTO orderDTO=productDataPriceToOrderDTO(bag.getValue(),storeName,memberName);
            ordersStore.put(bag.getKey(),orderDTO);
        }
        int orderId = orderRepository.createOrder(ordersStore,memberName);
        for (Map.Entry<Integer, List<ProductDataPrice>> bag : storeBag.entrySet()) {
            storeFacade.addOrderId(bag.getKey(), orderId);
        }
        return orderId;
    }

    private OrderDTO productDataPriceToOrderDTO(List<ProductDataPrice> storeBag,String storeName,String memberName) {
        Map<Integer, Integer> productAmounts=new HashMap<>();
        Map<Integer, String> orderProductsJsons= new HashMap<>();
        for (ProductDataPrice product:storeBag) {
            productAmounts.put(product.getId(),product.getAmount());
            orderProductsJsons.put(product.getId(),toJson(product));
        }
        return new OrderDTO(memberName,storeName,productAmounts,orderProductsJsons);
    }

    private String toJson(ProductDataPrice productDataPrice) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.writeValueAsString(productDataPrice);
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }

    public List<ProductDataPrice> getOrders(int storeId) {
        return orderRepository.getOrders(storeId);
    }

    public List<OrderDTO> getOrderHistory(int storeId) {
        List<OrderDTO> orderList = orderRepository.getOrderHistory(storeId);
        return orderList;
    }


    public Map<Integer,OrderDetails> getProductDataPriceByMember(String nameMember){
        return orderRepository.getProductDataPriceByMember(nameMember);
    }
    public Map<Integer,OrderDTO> getOrderByOrderId(int orderId) {
        return orderRepository.getOrderByOrderId(orderId);
    }

    public List<OrderDTO> getAllOrders(){
        return orderRepository.getAllOrders();
    }


    public void clear(){
        orderRepository.clear();
    }

}