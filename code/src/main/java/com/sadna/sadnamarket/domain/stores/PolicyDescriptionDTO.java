package com.sadna.sadnamarket.domain.stores;

public class PolicyDescriptionDTO {
    private int policyId;
    private String description;

    public PolicyDescriptionDTO(int policyId, String description) {
        this.policyId = policyId;
        this.description = description;
    }

    public PolicyDescriptionDTO() {
    }

    public int getPolicyId() {
        return policyId;
    }

    public void setPolicyId(int policyId) {
        this.policyId = policyId;
    }

    public String getDescription() {
        return description;
    }

    public void setDescription(String description) {
        this.description = description;
    }
}
