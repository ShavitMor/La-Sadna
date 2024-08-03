package com.sadna.sadnamarket.domain.buyPolicies;

import org.hibernate.Session;
import org.hibernate.annotations.Cache;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.Cacheable;
import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Table;

@Entity
@Table(name = "compositebuypolicies")
@Cacheable
@Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class CompositeBuyPolicyData extends BuyPolicyData {
    @Column(name = "id1")
    Integer id1;
    @Column(name = "id2")
    Integer id2;
    @Column(name = "logic")
    String logic;

    public CompositeBuyPolicyData(){

    }
    public CompositeBuyPolicyData(Integer policyId, Integer id1, Integer id2, String logic) {
        this.policyId = policyId;
        this.id1 = id1;
        this.id2 = id2;
        this.logic = logic;
    }

    public CompositeBuyPolicyData(Integer id1, Integer id2, String logic) {
        this.id1 = id1;
        this.id2 = id2;
        this.logic = logic;
    }

    @Override
    public Query getUniqueQuery(Session session) {
        Query query = session.createQuery("SELECT P FROM CompositeBuyPolicyData P " +
                "WHERE P.id1 = :id1 " +
                "AND P.id2 = :id2 " +
                "AND P.logic = :logic ");
        query.setParameter("id1",id1);
        query.setParameter("id2",id2);
        query.setParameter("logic",logic);
        return query;
    }

    @Override
    public BuyPolicy toBuyPolicy() {
        return null;
    }

    @Override
    public boolean isComposite() {
        return true;
    }

    @Override
    public int getId1() {
        return id1;
    }

    @Override
    public int getId2() {
        return id2;
    }

    @Override
    public BuyPolicy toBuyPolicy(BuyPolicy policy1, BuyPolicy policy2) {
        switch (logic){
            case BuyPolicyTypeCodes.AND:
                return new AndBuyPolicy(policyId, policy1, policy2);
            case BuyPolicyTypeCodes.OR:
                return new OrBuyPolicy(policyId, policy1, policy2);
            case BuyPolicyTypeCodes.CONDITION:
                return new ConditioningBuyPolicy(policyId, policy1, policy2);

        }
        return null;
    }
}
