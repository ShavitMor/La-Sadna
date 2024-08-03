package com.sadna.sadnamarket.domain.buyPolicies;

import org.hibernate.Session;
import org.hibernate.annotations.CacheConcurrencyStrategy;
import org.hibernate.query.Query;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.LinkedList;

@Entity
@Table(name = "rangebuypolicies")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class RangedBuyPolicyData extends BuyPolicyData {

    @Column(name = "subject")
    String subject;

    @Column(name = "min")
    Double min;

    @Column(name = "max")
    Double max;

    @Column(name = "type")
    String type;

    public RangedBuyPolicyData(Integer policyId, String subject, Double min, Double max, String type) {
        this.policyId = policyId;
        this.subject = subject;
        this.min = min;
        this.max = max;
        this.type = type;
    }

    public RangedBuyPolicyData(String subject, Double min, Double max, String type) {
        this.subject = subject;
        this.min = min;
        this.max = max;
        this.type = type;
    }

    public RangedBuyPolicyData(){

    }

    @Override
    public Query getUniqueQuery(Session session) {
        Query query = session.createQuery("SELECT P FROM RangedBuyPolicyData P " +
                "WHERE P.subject = :subj " +
                "AND P.min = :min " +
                "AND P.max = :max " +
                "AND P.type = :type");
        query.setParameter("subj",subject);
        query.setParameter("min",min);
        query.setParameter("max",max);
        query.setParameter("type",type);
        return query;
    }

    @Override
    public BuyPolicy toBuyPolicy() {
        PolicySubject policySubject;
        if(subject.startsWith("P")){
            policySubject = new ProductSubject(Integer.parseInt(subject.substring(2)));
        }else{
            policySubject = new CategorySubject(subject.substring(2));
        }
        switch (type){
            case BuyPolicyTypeCodes.AGE:
                return new AgeLimitBuyPolicy(policyId, new LinkedList<>(), policySubject, min.intValue(), max.intValue());
            case BuyPolicyTypeCodes.AMOUNT:
                return new AmountBuyPolicy(policyId, new LinkedList<>(), policySubject, min.intValue(), max.intValue());
            case BuyPolicyTypeCodes.KG:
                return new KgLimitBuyPolicy(policyId, new LinkedList<>(), policySubject, min, max);
            case BuyPolicyTypeCodes.HOUR:
                return new HourLimitBuyPolicy(policyId, new LinkedList<>(), policySubject,
                        LocalTime.MIDNIGHT.plusMinutes((int)(min.doubleValue()*60)),
                        LocalTime.MIDNIGHT.plusMinutes((int)(max.doubleValue()*60)));

        }
        return null;
    }

    @Override
    public boolean isComposite() {
        return false;
    }

    @Override
    public int getId1() {
        return 0;
    }

    @Override
    public int getId2() {
        return 0;
    }

    @Override
    public BuyPolicy toBuyPolicy(BuyPolicy policy1, BuyPolicy policy2) {
        return null;
    }
}
