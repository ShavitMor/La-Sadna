package com.sadna.sadnamarket.domain.discountPolicies.Conditions;

import com.sadna.sadnamarket.domain.discountPolicies.ProductDataPrice;
import com.sadna.sadnamarket.domain.products.ProductDTO;
import com.sadna.sadnamarket.domain.users.CartItemDTO;

import java.util.List;
import java.util.Map;
import java.util.Objects;

import com.fasterxml.jackson.annotation.JsonAutoDetect;
import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@JsonAutoDetect(fieldVisibility = JsonAutoDetect.Visibility.ANY)
@Entity
@Table(name = "minbuycondition")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class MinBuyCondition extends Condition{
    @Column(name = "minBuy")
    private int minBuy;

    public MinBuyCondition(int id, int minBuy){
        super(id);
        this.minBuy = minBuy;
    }
    public MinBuyCondition(int minBuy){
        super();
        this.minBuy = minBuy;
    }
    public MinBuyCondition(){}

    @Override
    public boolean checkCond(Map<Integer, ProductDTO> productDTOMap, List<ProductDataPrice> ListProductsPrice) {
        int itemID;
        double itemPrice;
        int amount;
        double total = 0;
        //computing the total
        for(ProductDataPrice item : ListProductsPrice){
            itemID = item.getId();
            itemPrice = productDTOMap.get(itemID).getProductPrice();
            amount = item.getAmount();
            total = total + amount*itemPrice;
        }
        return minBuy <= total;
    }

    public String description() {
        return "the cart original cost (before discounts) is at least " + minBuy;
    }



    @Override
    public Query getUniqueQuery(Session session) {
        Query query = session.createQuery("SELECT A FROM MinBuyCondition A " +
                "WHERE A.minBuy = :minBuy " );
        query.setParameter("minBuy", minBuy);
        return query;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        MinBuyCondition that = (MinBuyCondition) o;
        return Objects.equals(minBuy, that.minBuy);
    }

    @Override
    public int hashCode() {
        return Objects.hash(minBuy);
    }
}
