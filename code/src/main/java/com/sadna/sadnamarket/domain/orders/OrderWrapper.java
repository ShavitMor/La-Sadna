package com.sadna.sadnamarket.domain.orders;
import com.sadna.sadnamarket.HibernateUtil;
import org.hibernate.Session;
import org.hibernate.Transaction;
import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.util.*;

@Entity
@Table(name = "OrderWrapper")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OrderWrapper {

    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "order_id")
    private Integer id;

    @Column(name = "member_name")
    private String memberName;

    @Column(name = "date_time_purchase")
    private String dateTimeOfPurchase;

    @OneToMany(mappedBy = "orderWrapper", cascade = CascadeType.ALL, orphanRemoval = true)
    private List<Order> orders = new ArrayList<>();

    // Getters and Setters
    public Integer getId() {
        return id;
    }

    public void setId(Integer id) {
        this.id = id;
    }

    public String getMemberName() {
        return memberName;
    }

    public void setMemberName(String memberName) {
        this.memberName = memberName;
    }

    public String getDateTimeOfPurchase() {
        return dateTimeOfPurchase;
    }

    public void setDateTimeOfPurchase(String dateTimeOfPurchase) {
        this.dateTimeOfPurchase = dateTimeOfPurchase;
    }



}
