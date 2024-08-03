package com.sadna.sadnamarket.domain.orders;


import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.annotations.OnDelete;
import org.hibernate.annotations.OnDeleteAction;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Map;

@Entity
@Table(name = "Orders")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class Order implements Serializable {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Integer id;
    @Column(name = "member_name")
    private String memberName;

    @Id
    @Column(name = "store_id")
    private Integer storeId;
    @Column(name = "store_name_when_ordered")
    private String storeNameWhenOrdered;
    @ElementCollection
    @CollectionTable(name = "Order_Product_Amounts")
    @MapKeyColumn(name = "product_id")
    @Column(name = "amount")
    private Map<Integer, Integer> productAmounts;
    @ElementCollection
    @CollectionTable(name = "Order_Products_Jsons")
    @MapKeyColumn(name = "product_id")
    @Column(name = "json")
    private Map<Integer, String> orderProductsJsons;


    @ManyToOne
    @JoinColumn(name = "order_wrapper_id")
    private OrderWrapper orderWrapper;

    public Order(){

    }

    public Order(String memberName,String storeNameWhenOrdered,Map<Integer, Integer> productAmounts,Map<Integer, String> orderProductsJsons){
        this.memberName=memberName;
        this.storeNameWhenOrdered=storeNameWhenOrdered;
        this.productAmounts=productAmounts;
        this.orderProductsJsons=orderProductsJsons;
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

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public void setStoreId(Integer storeId) {
        this.storeId = storeId;
    }

    public void setStoreNameWhenOrdered(String storeNameWhenOrdered) {
        this.storeNameWhenOrdered = storeNameWhenOrdered;
    }

    public void setProductAmounts(Map<Integer, Integer> productAmounts) {
        this.productAmounts = productAmounts;
    }

    public void setOrderProductsJsons(Map<Integer, String> orderProductsJsons) {
        this.orderProductsJsons = orderProductsJsons;
    }

    public void setOrderWrapper(OrderWrapper orderWrapper) {
        this.orderWrapper = orderWrapper;
    }

    public Integer getStoreId() {
        return storeId;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public Integer getId() {
        return id;
    }
}

