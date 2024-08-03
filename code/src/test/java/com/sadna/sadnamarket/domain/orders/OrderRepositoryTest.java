package com.sadna.sadnamarket.domain.orders;

import com.sadna.sadnamarket.domain.products.HibernateProductRepository;
import com.sadna.sadnamarket.domain.products.Product;
import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.Test;
import org.springframework.data.relational.core.sql.In;

import java.util.HashMap;
import java.util.Map;

import static org.junit.jupiter.api.Assertions.assertEquals;
import static org.junit.jupiter.api.Assertions.assertNotNull;

public class OrderRepositoryTest{

    private HibernateOrderRepository repository;
    @BeforeEach
    public void setUp() {
        repository = new HibernateOrderRepository();
        repository.clear();
    }
    @AfterEach
    public void clean() {
        repository.clear();
    }

    @Test
    public void given_ValidOrderDetails_When_AddOrder_Then_OrderIsAdded() {
        String memberName="matan";
        Map<Integer,Integer> productAmounts=new HashMap<>();
        Map<Integer,String>orderProductsJsons=new HashMap<>();
        productAmounts.put(3,5);
        orderProductsJsons.put(3,"test");
        OrderDTO orderDTO=new OrderDTO(memberName,"ramiLevi",productAmounts,orderProductsJsons);
        Map<Integer, OrderDTO> storeOrdersDTO=new HashMap<>();
        storeOrdersDTO.put(5,orderDTO);
        int orderId = repository.createOrder(storeOrdersDTO,memberName);
        assertNotNull(orderId);
        Map<Integer, OrderDTO>  order = repository.getOrderByOrderId(orderId);
        OrderDTO ans = order.get(5);
        assertEquals(1, order.size());
        assertEquals("matan", ans.getMemberName());
        assertEquals("ramiLevi", ans.getStoreNameWhenOrdered());

    }
}
