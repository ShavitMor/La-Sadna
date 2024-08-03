package com.sadna.sadnamarket.api;

import com.sadna.sadnamarket.domain.users.Permission;

import java.util.Set;

public class ManagerPermissionRequest {
    String ManagerUsername;
    int storeId;
    Set<Integer> permission;

    public String getManagerUsername() {
        return ManagerUsername;
    }

    public void setManagerUsername(String ManagerUsername) {
        this.ManagerUsername = ManagerUsername;
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public Set<Integer> getPermission() {
        return permission;
    }

    public void setPermission(Set<Integer> permission) {
        this.permission = permission;
    }
}
