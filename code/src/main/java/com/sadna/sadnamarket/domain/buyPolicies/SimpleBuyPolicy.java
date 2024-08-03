package com.sadna.sadnamarket.domain.buyPolicies;

import javax.persistence.*;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;
import java.util.Set;

public abstract class SimpleBuyPolicy extends BuyPolicy{
    protected PolicySubject policySubject;

    protected List<BuyType> buytypes;

    SimpleBuyPolicy(int id, List<BuyType> buytypes, PolicySubject subject) {
        super(id);
        this.buytypes = buytypes;
        this.policySubject = subject;
    }

    SimpleBuyPolicy(List<BuyType> buytypes, PolicySubject subject) {
        super();
        this.buytypes = buytypes;
        this.policySubject = subject;
    }

    public SimpleBuyPolicy() {
    }

    public PolicySubject getPolicySubject() {
        return policySubject;
    }

    public void setPolicySubject(PolicySubject policySubject) {
        this.policySubject = policySubject;
    }

    public List<BuyType> getBuytypes() {
        return buytypes;
    }

    public void setBuytypes(List<BuyType> buytypes) {
        this.buytypes = buytypes;
    }

    public Set<Integer> getPolicyProductIds() {
        Set<Integer> ids = new HashSet<>();
        ids.add(policySubject.getProductId());
        return ids;
    }

    public boolean equals(Object o) {
        if (this == o) return true;
        if (!(o instanceof SimpleBuyPolicy)) return false;
        SimpleBuyPolicy that = (SimpleBuyPolicy) o;
        return buytypes.equals(that.buytypes) && policySubject.equals(that.policySubject);
    }

}
