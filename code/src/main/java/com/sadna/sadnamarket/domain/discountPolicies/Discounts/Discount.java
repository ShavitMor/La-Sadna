package com.sadna.sadnamarket.domain.discountPolicies.Discounts;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import com.fasterxml.jackson.annotation.JsonIgnoreProperties;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
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
public abstract class Discount {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    @Column(name = "id")
    protected Integer id;

    @Column(name = "isDefault")
    protected boolean isDefault;
    Discount(int id){
        this.id = id;
        isDefault = false;
    }
    Discount(){
        this.id = null;
        isDefault = false;
    }


    public abstract void giveDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice);
    public abstract void giveDiscountWithoutCondition(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice);
    abstract boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice);

    abstract double giveTotalPriceDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice);

    public void setId(int id) {
        this.id = id;
    }

    public int getId() {
        return id;
    }

    abstract public String description();

    public void setDefault() {
        isDefault = true;
    }
    public boolean isDefault() {
        return isDefault;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        Discount that = (Discount) o;
        return Objects.equals(isDefault, that.isDefault);
    }

    abstract public Query getUniqueQuery(Session session);

}
