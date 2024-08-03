package com.sadna.sadnamarket.domain.orders;

import java.util.*;

import javax.persistence.*;

//@Entity
//@Table(name = "Orders")
public class OrderDTO {
//    @Id
//    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;

//    @Column(name = "member_name")
    private String memberName;

//    @Column(name = "store_id")
    private Integer storeId;

    //private int orderId;
//    @Column(name = "store_name_when_ordered")
    private String storeNameWhenOrdered;
//    @ElementCollection
//    @CollectionTable(name = "Order_Product_Amounts", joinColumns = @JoinColumn(name = "order_id"))
//    @MapKeyColumn(name = "product_id")
//    @Column(name = "amount")
    private Map<Integer, Integer> productAmounts;

//    @ElementCollection
//    @CollectionTable(name = "Order_Products_Jsons", joinColumns = @JoinColumn(name = "order_id"))
//    @MapKeyColumn(name = "product_id")
//    @Column(name = "json")
    private Map<Integer, String> orderProductsJsons;

//    @ManyToOne
//    @JoinColumn(name = "order_wrapper_id")
    private OrderWrapper orderWrapper;

//    @Column(name = "order_wrapper_id")
//    private Integer orderWrapperId;

    public OrderDTO() {
    }

    public OrderDTO(String memberName,String storeNameWhenOrdered,Map<Integer, Integer> productAmounts,Map<Integer, String> orderProductsJsons){
        this.memberName=memberName;
        this.storeNameWhenOrdered=storeNameWhenOrdered;
        this.productAmounts=productAmounts;
        this.orderProductsJsons=orderProductsJsons;
    }
    public Integer getId() {
        return id;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMemberName() {
        return memberName;
    }

    public String getStoreNameWhenOrdered() {
        return storeNameWhenOrdered;
    }

    public Map<Integer, Integer> getProductAmounts() {
        return productAmounts;
    }

    public Map<Integer, String> getOrderProductsJsons() {
        return orderProductsJsons;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        OrderDTO orderDTO = (OrderDTO) o;
        boolean x1 = Objects.equals(memberName, orderDTO.memberName);
        boolean x2 = Objects.equals(storeNameWhenOrdered, orderDTO.storeNameWhenOrdered);
        boolean x3 = Objects.equals(productAmounts, orderDTO.productAmounts);
        boolean x4 = Objects.equals(orderProductsJsons, orderDTO.orderProductsJsons);
        return x1 && x2 && x3 && x4;
    }

    public OrderWrapper getOrderWrapper() {
        return orderWrapper;
    }

    public void setOrderWrapper(OrderWrapper orderWrapper) {
        this.orderWrapper = orderWrapper;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    @Override
    public int hashCode() {
        return Objects.hash(memberName, storeNameWhenOrdered, productAmounts, orderProductsJsons);
    }
}
