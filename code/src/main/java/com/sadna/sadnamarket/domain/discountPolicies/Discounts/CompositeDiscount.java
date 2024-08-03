package com.sadna.sadnamarket.domain.discountPolicies.Discounts;

import com.sadna.sadnamarket.domain.buyPolicies.CompositeBuyPolicy;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.*;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Inheritance(strategy = InheritanceType.TABLE_PER_CLASS)
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public abstract class CompositeDiscount extends Discount {
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "discountA_id", referencedColumnName = "id")
    protected Discount discountA;
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "discountB_id", referencedColumnName = "id")
    protected Discount discountB;
    public CompositeDiscount(int id, Discount discountA, Discount discountB){
        super(id);
        this.discountA = discountA;
        this.discountB = discountB;
    }
    public CompositeDiscount(Discount discountA, Discount discountB){
        super();
        this.discountA = discountA;
        this.discountB = discountB;
    }
    // Default constructor required by JPA
    protected CompositeDiscount() {
    }

    @Override
    public abstract void giveDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice);

    @Override
    public abstract boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice);
    @Override
    public abstract double giveTotalPriceDiscount(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice);

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeDiscount that = (CompositeDiscount) o;
        boolean one = Objects.equals(discountA, that.discountA) && Objects.equals(discountB, that.discountB);
        boolean two = Objects.equals(discountA, that.discountB) && Objects.equals(discountB, that.discountA);
        return (one || two) && super.equals(o);
    }

    @Override
    public int hashCode() {
        return Objects.hash(discountA, discountB);
    }

    @Override
    public Query getUniqueQuery(Session session) {
        Query query = session.createQuery("SELECT A FROM CompositeDiscount A " +
                "WHERE " +
                "((A.discountA.id = :idA AND A.discountB.id = :idB) OR ( A.discountA.id = :idB AND A.discountB.id = :idA))" +
                "And (A.isDefault = :isDefault)");
        query.setParameter("idA", discountA.getId());
        query.setParameter("idB",discountB.getId());
        query.setParameter("isDefault", isDefault);
        return query;
    }



}
