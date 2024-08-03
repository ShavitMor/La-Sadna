package com.sadna.sadnamarket.api;

public class CreateStoreRequest {
    String founderUsername;
    String storeName;
    String address;
    String email;
    String phoneNumber;

    public String getFounderUsername() {
        return founderUsername;
    }

    public void setFounderUsername(String founderUsername) {
        this.founderUsername = founderUsername;
    }

    public String getStoreName() {
        return storeName;
    }

    public void setStoreName(String storeName) {
        this.storeName = storeName;
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

}
