package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sadna.sadnamarket.domain.discountPolicies.Discounts.Discount;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@JsonIgnoreProperties(value = { "id" })
@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class Condition{
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    protected Integer id;
    Condition(int id){
        this.id = id;
    }
    Condition(){
        this.id = null;
    }

    abstract public boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice);

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    abstract public String description();

    abstract public Query getUniqueQuery(Session session);

    @Override
    public abstract boolean equals(Object o);

    public boolean isComposite(){
        return false;
    }

}
