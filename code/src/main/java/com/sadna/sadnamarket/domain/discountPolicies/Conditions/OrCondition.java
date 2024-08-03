package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

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

@Entity
@Table(name = "orcondition")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class OrCondition extends CompositeCondition{
    public OrCondition(int id, Condition conditionA, Condition conditionB) {
        super(id, conditionA, conditionB);
    }

    public OrCondition(Condition conditionA, Condition conditionB) {
        super(conditionA, conditionB);
    }
    public OrCondition(){}

    @Override
    public boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> listProductsPrice) {
        boolean condAisMet = conditionA.checkCond(productDTOMap, listProductsPrice);
        boolean condBisMet = conditionB.checkCond(productDTOMap, listProductsPrice);
        return condAisMet || condBisMet;
    }

    @Override
    public String description() {
        return conditionA.description() + " or " + conditionB.description();
    }

    @Override
    public Query getUniqueQuery(Session session) {
        Query query = session.createQuery("SELECT A FROM OrCondition A " +
                "WHERE " +
                " (A.conditionA.id = :idA AND A.conditionB.id = :idB) OR (A.conditionA.id = :idB AND A.conditionB.id = :idA)" );
        query.setParameter("idA", conditionA.getId());
        query.setParameter("idB",conditionB.getId());
        return query;
    }
}
