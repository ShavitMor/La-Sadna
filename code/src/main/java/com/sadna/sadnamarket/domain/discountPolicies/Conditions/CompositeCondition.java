package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

import com.sadna.sadnamarket.domain.discountPolicies.Discounts.CompositeDiscount;
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
public abstract class CompositeCondition extends Condition{
    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "conditionA_id", referencedColumnName = "id")
    protected Condition conditionA;

    @ManyToOne(cascade = CascadeType.ALL)
    @JoinColumn(name = "conditionB_id", referencedColumnName = "id")
    protected Condition conditionB;

    public CompositeCondition(int id, Condition conditionA, Condition conditionB){
        super(id);
        this.conditionA = conditionA;
        this.conditionB = conditionB;

    }
    public CompositeCondition(Condition conditionA, Condition conditionB){
        this.conditionA = conditionA;
        this.conditionB = conditionB;
    }

    public CompositeCondition(){}

    @Override
    public abstract boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice);
    @Override
    public abstract Query getUniqueQuery(Session session);

    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        CompositeCondition that = (CompositeCondition) o;
        boolean one = Objects.equals(conditionA, that.conditionA) && Objects.equals(conditionB, that.conditionB);
        boolean two = Objects.equals(conditionA, that.conditionB) && Objects.equals(conditionB, that.conditionA);
        return (one || two);
    }

    @Override
    public int hashCode() {
        return Objects.hash(conditionA, conditionB);
    }

    @Override
    public boolean isComposite(){
        return true;
    }

}
