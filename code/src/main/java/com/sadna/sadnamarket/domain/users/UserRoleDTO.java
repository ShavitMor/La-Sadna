package com.sadna.sadnamarket.domain.users;

public class UserRoleDTO {
    private int storeId;
    private String storeName;
    private String role;

    public UserRoleDTO(int storeId, String storeName, String role) {
        this.storeId = storeId;
        this.storeName = storeName;
        this.role = role;
    }
    public UserRoleDTO(int storeId, String role) {
        this.storeId = storeId;
        this.role = role;
        this.storeName = null;
    }
    public String getRole() {
        return role;
    }
    public int getStoreId() {
        return storeId;
    }
    public String getStoreName() {
        return storeName;
    }
    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }
}
