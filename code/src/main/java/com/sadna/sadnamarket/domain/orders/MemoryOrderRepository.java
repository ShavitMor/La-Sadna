
package com.sadna.sadnamarket.domain.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.MemoryProductRepository;
import com.sadna.sadnamarket.service.Error;
import org.apache.logging.log4j.LogManager;
import org.apache.logging.log4j.Logger;

import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.HashMap;
import java.util.LinkedList;
import java.util.List;
import java.util.Map;

public class MemoryOrderRepository implements IOrderRepository {
    private Map<Integer, PurchaseRecord> orders;
    private int nextOrderId;
    private static final Logger logger = LogManager.getLogger(MemoryOrderRepository.class);
    public MemoryOrderRepository() {
        orders=new HashMap<>();
        this.nextOrderId = 0;
    }
    @Override
    public synchronized int createOrder(Map<Integer, OrderDTO> storeOrdersDTO,String memberName){
        if (storeOrdersDTO == null) {
            throw new IllegalArgumentException(Error.makeOrderNullError());
        }
        if (storeOrdersDTO.isEmpty()) {
            throw new IllegalArgumentException(Error.makeOrderEmptyError());
        }
        for (Map.Entry<Integer, OrderDTO> entry : storeOrdersDTO.entrySet()) {
            if (entry.getValue() == null) {
                throw new IllegalArgumentException(Error.makeOrderStoreNullError(entry.getKey()));
            }
            if(entry.getValue().getProductAmounts().isEmpty()||entry.getValue().getOrderProductsJsons().isEmpty()){
                throw new IllegalArgumentException(Error.makeOrderStoreNoProductsError(entry.getKey()));
            }

        }
        Map<Integer, Order> storeOrders = new HashMap<>();
        for (Map.Entry<Integer, OrderDTO> entry : storeOrdersDTO.entrySet()) {
            Order order = DTOToOrder(entry.getValue());
            storeOrders.put(entry.getKey(),order);
        }
        // Get the current date and time
        LocalDateTime now = LocalDateTime.now();

        // Define the format
        DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

        // Format the date and time
        String formattedNow = now.format(formatter);
        PurchaseRecord purchaseRecord=new PurchaseRecord(memberName,formattedNow,storeOrders);
        orders.put(nextOrderId,purchaseRecord);
        nextOrderId++;
        return nextOrderId-1;
    }

    private Order DTOToOrder( OrderDTO ordersDTO) {
        String memberName=ordersDTO.getMemberName();
        String storeNameWhenOrdered = ordersDTO.getStoreNameWhenOrdered();
        Map<Integer, Integer> copiedProductAmounts=new HashMap<>();
        copiedProductAmounts.putAll(ordersDTO.getProductAmounts());
        Map<Integer, String> copiedProductsJsons = new HashMap<>();
        copiedProductsJsons.putAll(ordersDTO.getOrderProductsJsons());
        Order order = new Order(memberName,storeNameWhenOrdered,copiedProductAmounts,copiedProductsJsons);
        return order;
    }

    @Override
    public List<ProductDataPrice> getOrders(int storeId) {
        List<ProductDataPrice> productDataPrices=new LinkedList<>();
        for (int orderId : orders.keySet()) {
            if(orders.get(orderId).getOrders().containsKey(storeId)){
                Order order = orders.get(orderId).getOrders().get(storeId);
                Map<Integer, String> orderProductsJsons=order.getOrderProductsJsons();
                for (String productsJsons: orderProductsJsons.values() ) {
                    productDataPrices.add(fromJson(productsJsons));
                }
            }
        }
        if(productDataPrices.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderStoreNoOrdersError(storeId));
        }
        return productDataPrices;
    }

    @Override
    public List<OrderDTO> getOrderHistory(int storeId) {
        List<OrderDTO> ordersList=new LinkedList<>();
        for (int orderId : orders.keySet()) {
            if(orders.get(orderId).getOrders().containsKey(storeId)){
                Order order = orders.get(orderId).getOrders().get(storeId);
                ordersList.add(orderToDTO(order));
            }
        }
        if(ordersList.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderStoreNoOrdersError(storeId));
        }
        return ordersList;
    }

    private OrderDTO orderToDTO(Order order){
        String memberName=order.getMemberName();
        String storeNameWhenOrdered = order.getStoreNameWhenOrdered();
        Map<Integer, Integer> copiedProductAmounts=new HashMap<>();
        copiedProductAmounts.putAll(order.getProductAmounts());
        Map<Integer, String> copiedProductsJsons = new HashMap<>();
        copiedProductsJsons.putAll(order.getOrderProductsJsons());
        OrderDTO orderDTO = new OrderDTO(memberName,storeNameWhenOrdered,copiedProductAmounts,copiedProductsJsons);
        return orderDTO;
    }

    public Map<Integer,OrderDetails> getProductDataPriceByMember(String nameMember) {
        if(nameMember==null){
            throw new IllegalArgumentException(Error.makeOrderNameNullError());
        }
        if(nameMember.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderNameEmptyError());
        }
        Map<Integer,OrderDetails> ans=new HashMap<>();
        for (Map.Entry<Integer, PurchaseRecord> outerEntry : orders.entrySet()) {
            Integer outerKey = outerEntry.getKey();
            List<ProductDataPrice> productDataPrices = new LinkedList<>();
            String date=outerEntry.getValue().getDateOfPurchase();
            if(outerEntry.getValue().getMemberName().equals(nameMember)) {
                Map<Integer, Order> innerMap = outerEntry.getValue().getOrders();
                for (Map.Entry<Integer, Order> innerEntry : innerMap.entrySet()) {
                    Order order = innerEntry.getValue();
                    Map<Integer, String> orderProductsJsons = order.getOrderProductsJsons();
                    for (String productsJsons : orderProductsJsons.values()) {
                        productDataPrices.add(fromJson(productsJsons));
                    }
                }
            }
            if(productDataPrices.size()!=0) {
                OrderDetails OrderDetails=new OrderDetails(productDataPrices,date);
                ans.put(outerKey,OrderDetails);
            }
        }
        if(ans.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderNoOrdersForUserError(nameMember));
        }
        return ans;
    }



    public static ProductDataPrice fromJson(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, ProductDataPrice.class);
        } catch (JsonMappingException e) {
            e.printStackTrace();
            return null;
        } catch (JsonProcessingException e) {
            e.printStackTrace();
            return null;
        }
    }


    public Map<Integer,OrderDTO> getOrderByOrderId(int orderId) {
        Map<Integer,OrderDTO> orderDTOByOrderId = new HashMap<>();
        if(orders.containsKey(orderId)){
            Map<Integer,Order> orderByOrderId = orders.get(orderId).getOrders();
            for (Integer storeId : orderByOrderId.keySet()) {
                OrderDTO orderDTO=orderToDTO(orderByOrderId.get(storeId));
                orderDTOByOrderId.put(storeId,orderDTO);
            }
        }
        else {
            throw new IllegalArgumentException(Error.makeOrderDoesntExistError(orderId));
        }
        return orderDTOByOrderId;
    }

    public List<OrderDTO> getAllOrders(){
        List<OrderDTO> allOrders = new LinkedList<>();
        for (Map.Entry<Integer, PurchaseRecord> outerEntry : orders.entrySet()) {
            Map<Integer, Order> innerMap = outerEntry.getValue().getOrders();
            for (Map.Entry<Integer, Order> innerEntry : innerMap.entrySet()) {
                allOrders.add(orderToDTO(innerEntry.getValue()));
            }
        }
        if(allOrders.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderNoOrdersError());
        }
        return allOrders;
    }

    @Override
    public void clear() {
        orders=new HashMap<>();
        this.nextOrderId = 0;
    }

    public void resetOrders(){
        orders = new HashMap<>();
    }


}
