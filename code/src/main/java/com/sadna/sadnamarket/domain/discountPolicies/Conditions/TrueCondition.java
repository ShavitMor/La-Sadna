package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

import com.sadna.sadnamarket.domain.buyPolicies.SimpleBuyPolicy;
import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.Cacheable;
import javax.persistence.Entity;
import javax.persistence.Table;
import java.util.List;
import java.util.Map;
import java.util.Objects;

@Entity
@Table(name = "trueCondition")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class TrueCondition extends Condition{

    public TrueCondition(int id) {
        super(id);
    }
    public TrueCondition() {
        super();
    }

    @Override
    public boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice) {
        return true;
    }

    @Override
    public String description() {
        return "true";
    }

    @Override
    public Query getUniqueQuery(Session session) {
        return session.createQuery("FROM TrueCondition A");
    }
    @Override
    public boolean equals(Object o){
        if (this == o) return true;
        if (!(o instanceof TrueCondition)) return false;
        return  true;
    }

    @Override
    public int hashCode() {
        return Objects.hash();
    }
}
