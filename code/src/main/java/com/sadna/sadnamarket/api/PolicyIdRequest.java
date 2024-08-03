package com.sadna.sadnamarket.api;

public class PolicyIdRequest {
    int policyId1;
    int policyId2;

    int storeId;
    public int getPolicyId1() {
        return policyId1;
    }

    public void setPolicyId1(int policyId1) {
        this.policyId1 = policyId1;
    }

    public int getPolicyId2() {
        return policyId2;
    }

    public void setPolicyId2(int policyId2) {
        this.policyId2 = policyId2;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }
}
