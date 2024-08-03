package com.sadna.sadnamarket.domain.buyPolicies;

import org.hibernate.annotations.CacheConcurrencyStrategy;

import javax.persistence.*;
import java.io.Serializable;
import java.util.Objects;

@Entity
@Table(name = "storebuypolicies")
@Cacheable
@org.hibernate.annotations.Cache(usage = CacheConcurrencyStrategy.READ_WRITE)
public class StoreBuyPolicyRelation implements Serializable {
    @Id
    @Column(name = "store")
    int storeId;

    @Id
    @Column(name = "policy")
    int policyId;

    @Column(name = "legal")
    boolean legal;

    public StoreBuyPolicyRelation(int storeId, int policyId, boolean legal) {
        this.storeId = storeId;
        this.policyId = policyId;
        this.legal = legal;
    }

    public StoreBuyPolicyRelation(){

    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreBuyPolicyRelation that = (StoreBuyPolicyRelation) o;
        return storeId == that.storeId && policyId == that.policyId && legal == that.legal;
    }

    @Override
    public int hashCode() {
        return Objects.hash(storeId, policyId, legal);
    }
}
