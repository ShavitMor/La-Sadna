package com.sadna.sadnamarket.domain.stores;

import com.fasterxml.jackson.annotation.JsonFilter;
import com.fasterxml.jackson.annotation.JsonIgnore;
import com.sadna.sadnamarket.service.Error;

import javax.persistence.*;
import java.time.LocalTime;
import java.util.*;

public class StoreDTO {
    private Integer storeId;
    private Boolean isActive;
    private String storeName;
    private Double rank;
    private String address;
    private String email;
    private String phoneNumber;
    @JsonIgnore
    private Map<Integer, Integer> productAmounts;
    private String founderUsername;
    @JsonIgnore
    private Set<String> ownerUsernames;
    @JsonIgnore
    private Set<String> managerUsernames;
    @JsonIgnore
    private Set<Integer> orderIds;

    public StoreDTO() {
    }

    public StoreDTO(Store store) {
        this.storeId = store.getStoreId();
        this.isActive = store.getIsActive();
        this.storeName = store.getStoreInfo().getStoreName();
        this.rank = store.getStoreInfo().getRank();
        this.address = store.getStoreInfo().getAddress();
        this.email = store.getStoreInfo().getEmail();
        this.phoneNumber = store.getStoreInfo().getPhoneNumber();
        this.productAmounts = store.getProductAmounts();
        this.founderUsername = store.getFounderUsername();
        this.ownerUsernames = store.getOwnerUsernames();
        this.managerUsernames = store.getManagerUsernames();
        this.orderIds = store.getOrderIds();
    }

    public StoreDTO(int storeId, boolean isActive, String storeName, double rank, String address, String email, String phoneNumber, Map<Integer, Integer> productAmounts, String founderUsername, Set<String> ownerUsernames, Set<String> managerUsernames, Set<Integer> orderIds) {
        this.storeId = storeId;
        this.isActive = isActive;
        this.storeName = storeName;
        this.rank = rank;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.productAmounts = productAmounts;
        this.founderUsername = founderUsername;
        this.ownerUsernames = ownerUsernames;
        this.managerUsernames = managerUsernames;
        //this.sellerUsernames = sellerUsernames;
        this.orderIds = orderIds;
    }

    public StoreDTO(String storeName, String address, String email, String phoneNumber, String founderUsername) {
        this.isActive = true;
        this.storeName = storeName;
        this.rank = 3.0;
        this.address = address;
        this.email = email;
        this.phoneNumber = phoneNumber;
        this.productAmounts = new HashMap<>();
        this.founderUsername = founderUsername;
        this.ownerUsernames = new HashSet<>();
        ownerUsernames.add(founderUsername);
        this.managerUsernames = new HashSet<>();
        this.orderIds = new HashSet<>();
    }

    public int getStoreId() {
        return storeId;
    }

    public void setStoreId(int storeId) {
        this.storeId = storeId;
    }

    public boolean isActive() {
        return isActive;
    }

    public void setActive(boolean active) {
        isActive = active;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
    }

    public double getRank() {
        return rank;
    }

    public void setRank(double rank) {
        this.rank = rank;
    }

    public String getAddress() {
        return address;
    }

    public void setAddress(String address) {
        this.address = address;
    }

    public String getEmail() {
        return email;
    }

    public void setEmail(String email) {
        this.email = email;
    }

    public String getPhoneNumber() {
        return phoneNumber;
    }

    public void setPhoneNumber(String phoneNumber) {
        this.phoneNumber = phoneNumber;
    }

    public Map<Integer, Integer> getProductAmounts() {
        return productAmounts;
    }

    public void setProductAmounts(Map<Integer, Integer> productAmounts) {
        this.productAmounts = productAmounts;
    }

    public String getFounderUsername() {
        return founderUsername;
    }

    public void setFounderUsername(String founderUsername) {
        this.founderUsername = founderUsername;
    }

    public Set<String> getOwnerUsernames() {
        return ownerUsernames;
    }

    public void setOwnerUsernames(Set<String> ownerUsernames) {
        this.ownerUsernames = ownerUsernames;
    }

    public Set<String> getManagerUsernames() {
        return managerUsernames;
    }

    public void setManagerUsernames(Set<String> managerUsernames) {
        this.managerUsernames = managerUsernames;
    }

    /*public Set<String> getSellerUsernames() {
        return sellerUsernames;
    }

    public void setSellerUsernames(Set<String> sellerUsernames) {
        this.sellerUsernames = sellerUsernames;
    }*/

    public Set<Integer> getOrderIds() {
        return orderIds;
    }

    public void setOrderIds(Set<Integer> orderIds) {
        this.orderIds = orderIds;
    }

    @Override
    public boolean equals(Object o) {
        if (this == o) return true;
        if (o == null || getClass() != o.getClass()) return false;
        StoreDTO storeDTO = (StoreDTO) o;
        return Objects.equals(storeId, storeDTO.storeId) && Objects.equals(isActive, storeDTO.isActive) && Objects.equals(storeName, storeDTO.storeName) && Objects.equals(rank, storeDTO.rank) && Objects.equals(address, storeDTO.address) && Objects.equals(email, storeDTO.email) && Objects.equals(phoneNumber, storeDTO.phoneNumber) && Objects.equals(founderUsername, storeDTO.founderUsername) && equalStringSets(ownerUsernames, storeDTO.ownerUsernames) && equalStringSets(managerUsernames, storeDTO.managerUsernames) && equalIntegerSets(orderIds, storeDTO.orderIds);
    }

    private boolean equalStringSets(Set<String> s1, Set<String> s2) {
        for(String s : s1) {
            if(!s2.contains(s)) {
                return false;
            }
        }
        for(String s : s2) {
            if(!s1.contains(s)) {
                return false;
            }
        }
        return true;
    }

    private boolean equalIntegerSets(Set<Integer> s1, Set<Integer> s2) {
        for(Integer s : s1) {
            if(!s2.contains(s)) {
                return false;
            }
        }
        for(Integer s : s2) {
            if(!s1.contains(s)) {
                return false;
            }
        }
        return true;
    }


    @Override
    public int hashCode() {
        return Objects.hash(storeId, isActive, storeName, rank, address, email, phoneNumber, founderUsername, ownerUsernames, managerUsernames, orderIds);
    }
}
