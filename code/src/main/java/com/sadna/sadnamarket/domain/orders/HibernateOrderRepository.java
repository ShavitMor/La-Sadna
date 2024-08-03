package com.sadna.sadnamarket.domain.orders;

import com.fasterxml.jackson.core.JsonProcessingException;
import com.fasterxml.jackson.databind.JsonMappingException;
import com.fasterxml.jackson.databind.ObjectMapper;
import com.sadna.sadnamarket.HibernateUtil;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.service.Error;
import jakarta.persistence.QueryHint;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.springframework.data.jpa.repository.QueryHints;
import org.springframework.data.relational.core.sql.In;
import org.hibernate.query.Query;
//import javax.persistence.Query;
import java.time.LocalDateTime;
import java.time.format.DateTimeFormatter;
import java.util.*;

public class HibernateOrderRepository implements IOrderRepository{

    @Override
    public int createOrder(Map<Integer, OrderDTO> storeOrdersDTO, String memberName) {
        Transaction transaction = null;
        try(Session session = HibernateUtil.getSessionFactory().openSession()) {
            transaction = session.beginTransaction();
            OrderWrapper orderWrapper = new OrderWrapper();
            orderWrapper.setMemberName(memberName);
            // Get the current date and time
            LocalDateTime now = LocalDateTime.now();

            // Define the format
            DateTimeFormatter formatter = DateTimeFormatter.ofPattern("yyyy-MM-dd HH:mm:ss");

            // Format the date and time
            String formattedNow = now.format(formatter);
            orderWrapper.setDateTimeOfPurchase(formattedNow);
            session.save(orderWrapper);
            int orderID=orderWrapper.getId();

            for (Map.Entry<Integer, OrderDTO> entry : storeOrdersDTO.entrySet()) {
                Order order = DTOToOrder(entry.getValue());
                order.setStoreId(entry.getKey());
                order.setOrderWrapper(orderWrapper);
                session.save(order);
            }
            transaction.commit();
            return orderID;
        }
        catch (Exception e) {
            transaction.rollback();
            throw new IllegalArgumentException(Error.makeDBError());
        }
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<ProductDataPrice> getOrders(int storeId) {
        List<ProductDataPrice> productDataPrices=new LinkedList<>();
        List<Order> orders = null;
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Order WHERE storeId = :storeId";
            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("storeId", storeId);
            orders=query.getResultList();
            for (Order order: orders) {
                Map<Integer, String> orderProductsJsons=order.getOrderProductsJsons();
                for (String productsJsons: orderProductsJsons.values() ) {
                    productDataPrices.add(fromJson(productsJsons));
                }
            }
        }catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }

        if(productDataPrices.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderStoreNoOrdersError(storeId));
        }
        return productDataPrices;
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<OrderDTO> getOrderHistory(int storeId) {
        List<Order> orders = null;
        List<OrderDTO> orderDTOS = new LinkedList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Order WHERE storeId = :storeId";
            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("storeId", storeId);
            orders=query.getResultList();
            for(Order order : orders){
                orderDTOS.add(orderToDTO(order));
            }
        }catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }

        if(orders.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderStoreNoOrdersError(storeId));
        }
        return orderDTOS;
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Map<Integer, OrderDetails> getProductDataPriceByMember(String nameMember) {
        Map<Integer, OrderDetails> ans=new HashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql1 = "SELECT ow.id, ow.dateTimeOfPurchase FROM OrderWrapper ow WHERE ow.memberName = :memberName";
            Query<Object[]> query1 = session.createQuery(hql1, Object[].class);
            query1.setParameter("memberName", nameMember);
            List<Object[]> results = query1.getResultList();
            for (Object[] result : results) {
                List<Order> orders = null;
                String hql = "FROM Order o WHERE o.orderWrapper.id = :orderWrapperId";
                Query<Order> query = session.createQuery(hql, Order.class);
                Integer orderId= (Integer)result[0];
                query.setParameter("orderWrapperId", orderId);
                orders = query.getResultList();
                String dateTime= (String)result[1];
                List<ProductDataPrice> productDataPrices = new LinkedList<>();
                for (Order order:orders) {
                    Map<Integer, String> orderProductsJsons = order.getOrderProductsJsons();
                    for (String productsJsons : orderProductsJsons.values()) {
                        productDataPrices.add(fromJson(productsJsons));
                    }
                }
                if(productDataPrices.size()!=0) {
                    OrderDetails OrderDetails=new OrderDetails(productDataPrices,dateTime);
                    ans.put(orderId,OrderDetails);
                }
            }
        }catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        if(ans.isEmpty()){
            throw new IllegalArgumentException(Error.makeOrderNoOrdersForUserError(nameMember));
        }
        return ans;
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public Map<Integer, OrderDTO> getOrderByOrderId(int orderId) {
        Map<Integer, OrderDTO> ans=new HashMap<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Order o WHERE o.orderWrapper.id = :orderWrapperId";
            Query<Order> query = session.createQuery(hql, Order.class);
            query.setParameter("orderWrapperId", orderId);
            List<Order> orders = query.getResultList();
            for (Order order : orders) {
                ans.put(order.getStoreId(), orderToDTO(order));
            }
        }catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        return ans;
    }

    @Override
    @QueryHints({ @QueryHint(name = "org.hibernate.cacheable", value = "true") })
    public List<OrderDTO> getAllOrders() {
        List<OrderDTO> ans = new ArrayList<>();
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            String hql = "FROM Order";
            Query<Order> query = session.createQuery(hql, Order.class);
            List<Order> orders = query.getResultList();
            for (Order order : orders) {
                ans.add(orderToDTO(order));
            }
        }catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
        return ans;
    }

    @Override
    public void clear() {
        try (Session session = HibernateUtil.getSessionFactory().openSession()) {
            Transaction transaction = session.beginTransaction();
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 0").executeUpdate();
            session.createQuery("DELETE FROM Order").executeUpdate();
            session.createNativeQuery("SET FOREIGN_KEY_CHECKS = 1").executeUpdate();
            transaction.commit();
        } catch (Exception e) {
            throw new IllegalArgumentException("Database error", e);
        }
    }

    public static ProductDataPrice fromJson(String jsonString) {
        ObjectMapper objectMapper = new ObjectMapper();
        try {
            return objectMapper.readValue(jsonString, ProductDataPrice.class);
        } catch (Exception e) {
            throw new IllegalArgumentException(Error.makeDBError());
        }
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

    private OrderDTO orderToDTO(Order order){
        String memberName=order.getMemberName();
        String storeNameWhenOrdered = order.getStoreNameWhenOrdered();
        Map<Integer, Integer> copiedProductAmounts=new HashMap<>();
        copiedProductAmounts.putAll(order.getProductAmounts());
        Map<Integer, String> copiedProductsJsons = new HashMap<>();
        copiedProductsJsons.putAll(order.getOrderProductsJsons());
        OrderDTO orderDTO = new OrderDTO(memberName,storeNameWhenOrdered,copiedProductAmounts,copiedProductsJsons);
        orderDTO.setStoreId(order.getStoreId());
        orderDTO.setId(order.getId());
        return orderDTO;
    }


}
