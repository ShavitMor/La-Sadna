package com.sadna.sadnamarket.domain.discountPolicies;


import javax.persistence.Column;
import javax.persistence.Entity;
import javax.persistence.Id;
import javax.persistence.Table;
import java.io.Serializable;
import java.util.Objects;
@Entity
@Table(name = "StoreDiscountPolicyRelation")
public class StoreDiscountPolicyRelation implements Serializable {

        @Id
        @Column(name = "storeId")
        int storeId;

        @Id
        @Column(name = "policyId")
        int policyId;

        public StoreDiscountPolicyRelation(int storeId, int policyId) {
            this.storeId = storeId;
            this.policyId = policyId;
        }

        public StoreDiscountPolicyRelation(){

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
            StoreDiscountPolicyRelation that = (StoreDiscountPolicyRelation) o;
            return storeId == that.storeId && policyId == that.policyId;
        }

        @Override
        public int hashCode() {
            return Objects.hash(storeId, policyId);
        }

}
